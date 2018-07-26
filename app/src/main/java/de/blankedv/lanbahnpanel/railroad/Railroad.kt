package de.blankedv.lanbahnpanel.railroad

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.Utils.threadSleep
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

/** generic class which implements a connection to any TCP socket server which emits and
 * receives ASCII messages, can be SXNET or LbServer (or SRCP) */
open class Railroad(private var context: Context?, private val ip: String,
                    private val port: Int, private val rxHandler: Handler) : Thread() {
    // threading und BlockingQueue siehe http://www.javamex.com/tutorials/blockingqueue_example.shtml

    @Volatile
    protected var shutdownFlag: Boolean = false

    private var countNoResponse = 0
    private var connectionActive = false  // determined with time out counter
    private var timeElapsed: Long = 0


    override fun run() {
        if (DEBUG) Log.d(TAG, "Railroad run.")
        shutdownFlag = false
        connectionActive = false
        val (result, connResult) = connect(ip, port)

        if (result) {
            // connected !!
            connString = connResult
            if (DEBUG) Log.d(TAG, "connected to: " + connResult)
            connectionActive = true
        } else { // the connection could not be established, send Error Message to UI
            connString = "NOT CONNECTED"
            val m = Message.obtain()
            m.what = TYPE_ERROR_MSG
            m.obj = connResult
            rxHandler.sendMessage(m)  // send data to UI Thread via Message
            return
        }


        while (shutdownFlag == false && !Thread.currentThread().isInterrupted && context != null) {
            try {
                if (`in` != null && `in`!!.ready()) {
                    val in1 = `in`!!.readLine()
                    if (DEBUG) Log.d(TAG, "read: $in1")
                    handleReceive(in1.toUpperCase(), rxHandler)
                    countNoResponse = 0 // reset timeout counter.
                    connectionActive = true
                }
            } catch (e: IOException) {
                Log.e(TAG, "ERROR: reading from socket - " + e.message)
            } catch (e: InterruptedException) {
                Log.e(TAG, "Client thread interrupted - " + e.message)
            }

            // check send queue
            if (!sendQ.isEmpty()) {
                try {
                    val comm = sendQ.take()
                    if (comm.length > 0) immediateSend(comm)
                } catch (e: Exception) {
                    Log.e(TAG, "could not take command from sendQ")
                }
            }

            // send a command at least every 10 secs
            if (System.currentTimeMillis() - timeElapsed > LIFECHECK_SECONDS * 1000) {
                if (isConnected()) {

                    readPower()

                    countNoResponse++
                }
                timeElapsed = System.currentTimeMillis()  // reset
                if (countNoResponse > 2) {
                    Log.e(TAG, "Railroad - connection lost?")
                    countNoResponse = 0
                    connectionActive = false

                }
            }
            threadSleep(10)
        }

        socket?.close()
        Log.e(TAG, "Railroad - socket closed")
    }

    fun readChannel(addr: Int, peClass: Class<*> = Object::class.java) {
        if (addr == INVALID_INT) return
        var cmd = "READ $addr"
        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "readChannel failed, queue full")
        }
    }


    fun setChannel(addr: Int, data: Int, peClass: Class<*> = Object::class.java) {
        if ((addr == INVALID_INT) or (data == INVALID_INT)) return
        var cmd = "SET $addr $data"

        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "setChannel failed, queue full")
        }
    }

    fun setPower(state: Int) {
        var cmd = "SETPOWER "
        when (state) {
            POWER_ON -> cmd += "1"
            POWER_OFF -> cmd += "0"
        }

        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "setPower failed, queue full")
        }
    }

    fun readPower() {
        var cmd = "READPOWER "
        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "readPower failed, queue full")
        }
    }


    fun setLocoData(addr : Int, data : Int) {
        var cmd = "SETLOCO $addr $data"

        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "setLocoData failed, queue full")
        }
    }

    fun readLocoData(addr : Int) {
        if (addr == INVALID_INT) return
        var cmd = "READLOCO $addr"
        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "readChannel failed, queue full")
        }
    }

    private fun immediateSend(command: String) {
        if (shutdownFlag) return
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

    private fun connect(ip: String, port: Int): Pair<Boolean, String> {
        if (DEBUG) Log.d(TAG, "trying conn to - $ip:$port")
        try {
            val socketAddress = InetSocketAddress(ip, port)

            // create a socket
            socket = Socket()
            socket!!.connect(socketAddress, 5000)
            //socket.setSoTimeout(2000);  // set read timeout to 2000 msec

            socket!!.setSoLinger(true, 0)  // force close, dont wait.

            out = PrintWriter(socket!!.getOutputStream(), true)
            `in` = BufferedReader(InputStreamReader(
                    socket!!.getInputStream()))
            val resString = `in`!!.readLine()

            if (DEBUG) Log.d(TAG, "connected to: $connString")

            return Pair(true, resString)

        } catch (e: Exception) {
            val err = "ERROR: " + e.message
            Log.e(TAG, "Railroad.connect " + err)

            return Pair(false, err)
        }

    }


    // see
    // http://stackoverflow.com/questions/969866/java-detect-lost-connection
    fun isConnected(): Boolean {
        if ((socket == null) || (connectionActive == false)) {
            return false
        } else {
            return socket!!.isConnected && !socket!!.isClosed
        }
    }

    fun shutdown() {
        shutdownFlag = true
    }

    fun handleReceive(receivedMsg: String, recHandler: Handler): Boolean {

        // check whether there is an application to send info to -
        // to avoid crash if application has stopped but thread is still running
        if (shutdownFlag == true) return false

        var info: Array<String>? = null
        val msg = receivedMsg.toUpperCase()

        // new code: multiple commands in a single message, separated by ';'
        // example: String msg = "S 780 2 ;; S 800 3  ;  S 720 1";
        val allcmds = msg.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (cmd in allcmds) {
            //Log.d(TAG,"single cmd="+cmd);
            if (!cmd.contains("ERROR")) {
                info = cmd.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // all feedback message have the Format "CMD <adr> <data>"

                if (info.size < 3) return false

                // check validity of numbers
                val addr = extractChannelFromString(info[1])
                val data = extractDataByteFromString(info[2])

                if ((addr == INVALID_INT) or (data == INVALID_INT)) return false

                val m = Message.obtain()
                m.arg1 = addr
                m.arg2 = data

                when (info[0]) {
                    "X" -> m.what = TYPE_GENERIC_MSG
                    "XPOWER" -> m.what = TYPE_POWER_MSG
                    "RT" -> m.what = TYPE_ROUTE_MSG
                    else -> m.what = INVALID_INT
                }

                if (m.what != INVALID_INT) {
                    recHandler.sendMessage(m)  // send route data to UI Thread via Message
                }
            }

        }
        return true
    }

    /** convert data string to integer and
     *  check if data is in valid range for selectrix (8 bit, 0..255)
     */
    private fun extractDataByteFromString(s: String): Int {
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
     *  check if address (=channel) is in valid range for selectrix
     */
    private fun extractChannelFromString(s: String): Int {

        try {
            var addr = Integer.parseInt(s)
            if (isValidAddress(addr)) {
                return addr
            }
        } catch (e: NumberFormatException) {
        }
        return INVALID_INT

    }


    companion object {
        var socket: Socket? = null
        var out: PrintWriter? = null
        var `in`: BufferedReader? = null

        fun isValidAddress(a: Int): Boolean {
            //TODO
            return true;
        }
    }
}