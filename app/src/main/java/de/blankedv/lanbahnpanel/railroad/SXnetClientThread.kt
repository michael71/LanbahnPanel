package de.blankedv.lanbahnpanel.railroad

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.model.*

/**
 * communicates with the SX3-PC server program (usually on port 4104)
 *
 * runs on own thread, using a BlockingQueue for queing the commands
 * can be shutdown by calling the shutdown method.
 *
 * @author mblank
 */
class SXnetClientThread(
        private var context: Context?, private val ip: String, private val port: Int,
        private val rxHandler: Handler) : RRConnectionThread(context, ip, port, rxHandler) {
    // threading und BlockingQueue siehe http://www.javamex.com/tutorials/blockingqueue_example.shtml



    private var count_no_response = 0
    private var timeElapsed: Long = 0

    private var socket: Socket? = null
    private var out: PrintWriter? = null
    private var `in`: BufferedReader? = null

    init {
        if (DEBUG) Log.d(TAG, "SXnetClientThread constructor.")
    }

    // see
    // http://stackoverflow.com/questions/969866/java-detect-lost-connection
    override fun isConnected(): Boolean {
        if (socket == null) {
            return false
        } else {
            // TODO add "false" when no response from server (but socket still available)
           return socket!!.isConnected && !socket!!.isClosed
        }
    }

    override fun reconnect() : Boolean {
        // TODO implement
        return true;
    }

    override fun read(addr : Int) {
        // TODO implement
    }

    override fun send(addr : Int, data : Int) : Boolean {
       // TODO implement
        val success = true
        return success
    }

    override fun run() {
        if (DEBUG) Log.d(TAG, "SXnetClientThread run.")
        shutdownFlag = false
        connect()


        while (shutdownFlag == false && !Thread.currentThread().isInterrupted) {
            try {
                if (`in` != null && `in`!!.ready()) {
                    val in1 = `in`!!.readLine()
                    if (DEBUG) Log.d(TAG, "msgFromServer: $in1")
                    handleMsgFromServer(in1.toUpperCase())
                    count_no_response = 0 // reset timeout counter.

                }
            } catch (e: IOException) {
                Log.e(TAG, "ERROR: reading from socket - " + e.message)
            }

            // check send queue
            if (!sendQ.isEmpty()) {

                var comm = ""
                try {
                    comm = sendQ.take()
                    if (comm.length > 0) immediateSend(comm)
                    Thread.sleep(20)  // do not send faster than serial port ...
                } catch (e: InterruptedException) {
                    Log.e(TAG, "could not take command from sendQ")
                }

            }

            // send a command at least every 10 secs
            if (System.currentTimeMillis() - timeElapsed > 10 * 1000) {
                if (socket!!.isConnected) {
                    readChannel(127) //read power channel
                    count_no_response++
                }
                timeElapsed = System.currentTimeMillis()  // reset
                if (count_no_response > 2) {
                    Log.e(TAG, "SXnetClientThread - connection lost.")
                    count_no_response = 0
                }
            }
            Thread.sleep(20)
        }

        socket?.close()
        Log.e(TAG, "SXnetClientThread - socket closed")

    }


    private fun connect() {
        if (DEBUG) Log.d(TAG, "trying conn to - $ip:$port")
        try {
            val socketAddress = InetSocketAddress(ip, port)

            // create a socket
            socket = Socket()
            socket!!.connect(socketAddress, 2000)
            //socket.setSoTimeout(2000);  // set read timeout to 2000 msec

            socket!!.setSoLinger(true, 0)  // force close, dont wait.

            out = PrintWriter(socket!!.getOutputStream(), true)
            `in` = BufferedReader(InputStreamReader(
                    socket!!.getInputStream()))
            connString = `in`!!.readLine()

            if (DEBUG) Log.d(TAG, "connected to: $connString")

        } catch (e: Exception) {
            Log.e(TAG, "SXnetClientThread.connect - Exception: " + e.message)

            val m = Message.obtain()
            m.what = TYPE_ERROR_MSG
            m.obj = e.message
            rxHandler?.sendMessage(m)  // send SX data to UI Thread via Message
        }

    }


    fun readChannel(adr: Int) {
        //if (DEBUG) Log.d(TAG,"readChannel a="+adr+" clientTerm="+clientTerminated);
        if (shutdownFlag  || adr == INVALID_INT || !isConnected()) return
        val success = sendQ.offer("R $adr")
        if (!success && DEBUG) {
            Log.d(TAG, "readChannel failed, queue full")
        }
    }


    private fun immediateSend(command: String) {
        if (shutdownFlag ) return
        if (out == null) {
            if (DEBUG) Log.d(TAG, "out=null, could not send: $command")
        } else {
            try {
                out!!.println(command)
                out!!.flush()
                if (DEBUG) Log.d(TAG, "sent: $command")
            } catch (e: Exception) {
                if (DEBUG) Log.d(TAG, "could not send: $command")
                Log.e(TAG, e.javaClass.name + " " + e.message)
            }

        }
    }


    /**
     * SX Net Protocol (all msg terminated with CR)
     *
     * client sends                           |  SXnetServer Response
     * ---------------------------------------|-------------------
     * R cc    = Read channel cc (0..127)     |  "X" cc dd
     * B cc b  = SetBit Ch. cc Bit b (1..8)   |  "OK" (and later, when changed in CS: X cc dd )
     * C cc b  = Clear Ch cc Bit b (1..8)     |  "OK" (and later, when changed in CS: X cc dd )
     * S cc dd = set channel cc Data dd (<256)|  "OK" (and later, when changed in CS: X cc dd )
     * DSDF 89sf  (i.e. garbage)              |  "ERROR"
     *
     * channel 127 bit 8 == Track Power
     *
     * for a list of channels (which the client has set or read in the past) all changes are
     * transmitted back to the client
     */

    private fun handleMsgFromServer(msg: String) {
        var msg = msg
        // check whether there is an application to send info to -
        // to avoid crash if application has stopped but thread is still running
        if (context == null) return

        var info: Array<String>? = null
        msg = msg.toUpperCase()

        var adr = INVALID_INT
        var data: Int

        // new code: multiple commands in a single message, separated by ';'
        // example: String msg = "S 780 2 ;; S 800 3  ;  S 720 1";
        val allcmds = msg.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (cmd in allcmds) {
            //Log.d(TAG,"single cmd="+cmd);
            if (!cmd.contains("ERROR")) {
                info = cmd.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // all feedback message have the Format "X <adr> <data>"
                // adr range 1 ... 9999, data range 0 ..255
                // adr=127 is the Selectix "Power" channel
                if (info.size >= 3 && info[0] == "X") {
                    adr = getChannelFromString(info[1])
                    data = getDataFromString(info[2])
                    if (adr != INVALID_INT && data != INVALID_INT) {
                        val m = Message.obtain()
                        m.what = TYPE_FEEDBACK_MSG
                        m.arg1 = adr
                        m.arg2 = data
                        rxHandler.sendMessage(m)  // send SX data to UI Thread via Message
                    } else {
                        Log.e(TAG, "range error in rec. msg, cmd=$cmd adr=$adr data=$data")
                    }
                } else {
                    Log.e(TAG, "length error in rec. msg, cmd=" + cmd + "info[0]=" + info[0])
                }
            }
        }

    }

    /** convert data string to integer and
     *  check if data is in valid range for selectrix (8 bit, 0..255)
     */
    private fun getDataFromString(s: String): Int {
        // converts String to integer between 0 and 255 (maximum data range)
        var data: Int? = INVALID_INT
        try {
            data = Integer.parseInt(s)
            if (data < 0 || data > 255) {
                data = INVALID_INT
            }
        } catch (e: Exception) {
            data = INVALID_INT
        }

        return data!!
    }

    /** convert address string to integer and
     * check if address (=channel) is in valid range for selectrix
     */
    private fun getChannelFromString(s: String): Int {

        try {
            var channel = INVALID_INT
            channel = Integer.parseInt(s)
            if ((channel in SXMIN..SXMAX) or (channel == SXPOWER_ADR)){
                return channel
            } else {
                return INVALID_INT
            }
        } catch (e: Exception) {

        }

        return INVALID_INT
    }


}
