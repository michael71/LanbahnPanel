package de.blankedv.lanbahnpanel.railroad

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.elements.PanelElement
import de.blankedv.lanbahnpanel.model.*
import de.blankedv.lanbahnpanel.util.Utils.threadSleep
import org.jetbrains.anko.toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

/** generic class which implements a connection to any TCP socket server which emits and
 * receives ASCII messages, can be SXNET or LbServer (or SRCP) */
open class RRConnectionThread(private var context: Context?, private val ip: String,
                              private val port: Int, private val rxHandler: Handler) : Thread() {
    // threading und BlockingQueue siehe http://www.javamex.com/tutorials/blockingqueue_example.shtml

    @Volatile
    protected var shutdownFlag: Boolean = false

    private var countNoResponse = 0
    private var connectionActive = false  // determined with time out counter
    private var timeElapsed: Long = 0

    // TODO implement connectionActive Test for Loconet


    private var myClient: GenericClient? = null

    override fun run() {
        if (DEBUG) Log.d(TAG, "SXnetClientThread run.")
        shutdownFlag = false
        connectionActive = false
        val (result, connResult) = connect(ip, port)

        if (result) {
            // connected !!
            connString = connResult
            if (DEBUG) Log.d(TAG, "connected to: " + connResult)
            if (connResult.toUpperCase().contains("SXNET")) {
                myClient = SXnetClient()
            } else if (connResult.toUpperCase().contains("LBSERVER")) {
                // myClient = LbServerClient()  // TODO
                context?.toast("ERROR connection to Fremo LbServer not yet implemented")
            } else {
                context?.toast("ERROR - unknown type of RR server (neither SXNET nor LBSERVER)")
                return
            }
            connectionActive = true
        } else { // the connection could not be established, send Error Message to UI
            connString = "NOT CONNECTED"
            val m = Message.obtain()
            m.what = TYPE_ERROR_MSG
            m.obj = connResult
            rxHandler?.sendMessage(m)  // send data to UI Thread via Message
            return
        }


        while (shutdownFlag == false && !Thread.currentThread().isInterrupted && context != null) {
            try {
                if (`in` != null && `in`!!.ready()) {
                    val in1 = `in`!!.readLine()
                    if (DEBUG) Log.d(TAG, "read: $in1")
                    myClient?.handleReceive(in1.toUpperCase(), rxHandler)
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
                var comm = ""
                try {
                    comm = sendQ.take()
                    if (comm.length > 0) immediateSend(comm)
                } catch (e: Exception) {
                    Log.e(TAG, "could not take command from sendQ")
                }
            }

            // send a command at least every 10 secs
            if (System.currentTimeMillis() - timeElapsed > LIFECHECK_SECONDS * 1000) {
                if (isConnected()) {
                    if (myClient is SXnetClient) {
                        readChannel(POWER_CHANNEL) //read power channel
                    } else {
                        // TODO implement similar "lifecheck" for loconet
                    }
                    countNoResponse++
                }
                timeElapsed = System.currentTimeMillis()  // reset
                if (countNoResponse > 2) {
                    Log.e(TAG, "SXnetClientThread - connection lost?")
                    countNoResponse = 0
                    connectionActive = false

                }
            }
            threadSleep(10)
        }

        socket?.close()
        Log.e(TAG, "RRConnectionThread - socket closed")
    }

    fun readChannel(addr: Int, peClass: Class<ActivePanelElement>) {
        if (addr == INVALID_INT) return
        var cmd = myClient?.readChannel(addr, peClass) ?: return

        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "readChannel failed, queue full")
        }
    }

    fun readChannel(addr: Int) {
        if (addr == INVALID_INT) return
        var cmd = myClient?.readChannel(addr) ?: return
        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "readChannel failed, queue full")
        }
    }

    fun setPower(onoff: Boolean) {
        var cmd = myClient?.setPowerState(onoff) ?: return
        val success = sendQ.offer(cmd)
        if (!success && DEBUG) {
            Log.d(TAG, "setPower failed, queue full")
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
            socket!!.connect(socketAddress, 2000)
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
            Log.e(TAG, "RRConnectionThread.connect " + err)

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

    companion object {
        var socket: Socket? = null
        var out: PrintWriter? = null
        var `in`: BufferedReader? = null
    }
}