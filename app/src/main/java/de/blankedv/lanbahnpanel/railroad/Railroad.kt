package de.blankedv.lanbahnpanel.railroad

import android.content.Context
import android.os.Handler
import android.os.Message
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.os.Process.setThreadPriority
import android.util.Log
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.Utils.threadSleep
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import de.blankedv.lanbahnpanel.model.LanbahnPanelApplication.Companion.appHandler

/** generic class which implements a connection to any TCP socket server which emits and
 * receives ASCII messages, can be SXNET or LbServer (or SRCP) */
open class Railroad(private val ip: String, private val port: Int) : Thread() {
    // threading und BlockingQueue siehe http://www.javamex.com/tutorials/blockingqueue_example.shtml

    @Volatile
    protected var shutdownFlag: Boolean = false

    private var countNoResponse = 0
    private var connectionActive = false  // determined with time out counter
    private var lastReceived: Long = 0

    private val TAG = "Railroad_Thread"

    override fun run() {
        if (DEBUG) Log.d(TAG, " run.")
        shutdownFlag = false
        connectionActive = false
        setThreadPriority(THREAD_PRIORITY_BACKGROUND)
        val (result, connResult) = connect(ip, port)

        if (result) {
            // connected !!
            connString = connResult
            if (DEBUG) Log.d(TAG, "connected to: " + connResult)
            connectionActive = true
            lastReceived = System.currentTimeMillis()
        } else { // the connection could not be established, send Error Message to UI
            connString = "NOT CONNECTED"
            if (DEBUG) Log.d(TAG, "NOT Connected")
            val m = Message.obtain()
            m.what = TYPE_ERROR_MSG
            m.obj = connResult
            appHandler.sendMessage(m)  // send data to UI Thread via Message
            return
        }


        while ((shutdownFlag == false) && !Thread.currentThread().isInterrupted) {
            try {
                if (`in` != null && `in`!!.ready()) {
                    val in1 = `in`!!.readLine()
                    if (DEBUG) Log.d(TAG, "read: $in1")
                    handleReceive(in1.toUpperCase())
                    lastReceived = System.currentTimeMillis()
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

            // check if we are still receiving messages
            if ((System.currentTimeMillis() - lastReceived) > LIFECHECK_SECONDS * 1000) {
                lastReceived = System.currentTimeMillis()  // reset
                    Log.e(TAG, "Railroad - connection lost?")
                    countNoResponse = 0
                    connectionActive = false
                    sendShutdownMessage()
                    threadSleep(200)
                    shutdownFlag = true
             }
            threadSleep(10)
        }

        socket?.close()
        Log.e(TAG, "Railroad - socket closed")
    }


    private fun sendShutdownMessage() {
        val m = Message.obtain()
        m.what = TYPE_SHUTDOWN_MSG
        m.obj = "no response -> shutting down"
        appHandler.sendMessage(m)  // send data to UI Thread via Message
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

            if (DEBUG) Log.d(TAG, "connect connString=$resString")

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
        Thread.interrupted()
    }

    fun handleReceive(receivedMsg: String): Boolean {

        // check whether there is an application to send info to -
        // to avoid crash if application has stopped but thread is still running
        if (shutdownFlag == true) return false

        var info: Array<String>? = null
        val msg = receivedMsg.toUpperCase()

        // new code: multiple commands in a single message, separated by ';'
        // example: String msg = "S 780 2 ;; S 800 3  ;  S 720 1";
        val allcmds = msg.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (cmd in allcmds) {
            //Log.d(TAG, "single cmd=" + cmd);
            if (!cmd.contains("ERROR")) {
                info = cmd.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // all feedback message have the Format "CMD <adr> <data>" or "CMD <data>"
                when (info.size) {
                    0 -> return false
                    1 -> {
                        if (info[0] == "QUIT") { // server has terminated connection
                            // TODO test
                            val m = Message.obtain()
                            m.what = TYPE_CONNECTION_MSG
                            m.arg2 = 0
                            appHandler.sendMessage(m)  // send to UI Thread via Message
                            Thread.sleep(100)
                            shutdown()
                        } else if (info[0] == "ROUTE_INVALID") {
                            val m = Message.obtain()
                            m.what = TYPE_ROUTE_INVALID_MSG
                            m.arg2 = 0
                            appHandler.sendMessage(m)  // send to UI Thread via Message
                        }
                    }
                    2 -> {
                        if (info[0] == "XPOWER") {
                            val data = extractDataByteFromString(info[1])
                            if (data == INVALID_INT) return false
                            val m = Message.obtain()
                            m.what = TYPE_POWER_MSG
                            m.arg2 = data
                            appHandler.sendMessage(m)  // send to UI Thread via Message
                        } else if (info[0] == "XCONN") {
                            val data = extractDataByteFromString(info[1])
                            if (data == INVALID_INT) return false
                            val m = Message.obtain()
                            m.what = TYPE_CONNECTION_MSG
                            m.arg2 = data
                            appHandler.sendMessage(m)  // send to UI Thread via Message
                        } else if (info[0] == "ROUTING") {
                            val data = extractDataByteFromString(info[1])
                            if (data == INVALID_INT) return false
                            val m = Message.obtain()
                            m.what = TYPE_ROUTING_MSG
                            m.arg2 = data
                            appHandler.sendMessage(m)  // send to UI Thread via Message
                        }
                    }
                    3 -> {
                        val addr = extractChannelFromString(info[1])
                        val data = extractDataFromString(info[2])
                        if ((addr == INVALID_INT) or (data == INVALID_INT)) return false
                        val m = Message.obtain()
                        m.arg1 = addr
                        m.arg2 = data
                        when (info[0]) {
                            "X" -> m.what = TYPE_SX_MSG
                            "XL" -> m.what = TYPE_GENERIC_MSG
                            "XLOCO" -> m.what = TYPE_LOCO_MSG
                            "XTRAIN" -> m.what = TYPE_TRAIN_MSG
                             else -> m.what = INVALID_INT
                        }
                        if (m.what != INVALID_INT) {
                            //Log.d(TAG,"what=${m.what} a=${m.arg1} d=${m.arg2}")
                            appHandler.sendMessage(m)  // send route data to UI Thread via Message
                        }
                    }
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

    /** convert data string to integer and
     *  check if data is in valid range 0..9999 ( train numbers for example
     */
    private fun extractDataFromString(s: String): Int {
        // converts String to integer between 0 and 9999 (maximum data range)
        var data: Int? = INVALID_INT
        try {
            data = Integer.parseInt(s)
            if (data < 0 || data > 9999) {
                data = INVALID_INT
            }
        } catch (e: Exception) {
            data = INVALID_INT
        }

        return data!!
    }
    /** convert address string to integer and
     *  check if address (=channel) is in valid range for selectrix or lanbahn
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
            return ((a >= 0) and (a <= LBMAX))
        }
    }
}