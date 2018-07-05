package de.blankedv.lanbahnpanel.railroad

import android.content.Context
import android.hardware.Sensor
import android.os.Handler
import android.os.Message
import android.util.Log
import de.blankedv.lanbahnpanel.elements.ActivePanelElement
import de.blankedv.lanbahnpanel.model.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

open class RRConnectionThread(private var context: Context?, private val ip: String, private val port: Int, private val rxHandler: Handler) : Thread() {
    // threading und BlockingQueue siehe http://www.javamex.com/tutorials/blockingqueue_example.shtml

    @Volatile
    protected var shutdownFlag: Boolean = false

    private var count_no_response = 0
    private var timeElapsed: Long = 0

    private var myClient : GenericClient? = null

    override fun run() {
        if (DEBUG) Log.d(TAG, "SXnetClientThread run.")
        shutdownFlag = false
        val connResult = connect(ip, port)

        if (!connResult.contains("ERROR")) {
            // connected !!
           myClient = SXnetClient()  // TODO make type dependent on connection String

        } else { // the connection could not be established, send Error Message to UI
            val m = Message.obtain()
            m.what = TYPE_ERROR_MSG
            m.obj = connResult
            rxHandler?.sendMessage(m)  // send SX data to UI Thread via Message
            return
        }


        while (shutdownFlag == false && !Thread.currentThread().isInterrupted && context != null)  {
            try {
                if (`in` != null && `in`!!.ready()) {
                    val in1 = `in`!!.readLine()
                    if (DEBUG) Log.d(TAG, "msgFromServer: $in1")
                    myClient?.handleReceive(in1.toUpperCase(), rxHandler)
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
                if (isConnected()) {
                    readChannel(POWER_CHANNEL) //read power channel
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


     fun readChannel(addr: Int, peClass : Class<ActivePanelElement>) {

        var cmd = myClient.readChannel(addr, peClass)
        val success = sendQ.offer(cmd)
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

    fun connect(ip: String, port: Int) : String {
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

            return connString;

        } catch (e: Exception) {
            val err = "ERROR: "+e.message
            Log.e(TAG, "SXnetClientThread.connect "  + err)

            return err
        }

    }


    // see
    // http://stackoverflow.com/questions/969866/java-detect-lost-connection
    fun isConnected(): Boolean {
        if (socket == null) {
            return false
        } else {
            // TODO add "false" when no response from server (but socket still available)
            return socket!!.isConnected && !socket!!.isClosed
        }
    }

   /*fun reconnect() : Boolean {
       connect(ip, port)
   } */

    fun shutdown() {
        shutdownFlag = true
    }

    fun disconnectContext() {
        this.context = null
        Log.d(TAG, "lost context, stopping thread")
        shutdown()
    }

    abstract fun read(adr: Int, type: Int)

    fun read(adr: Int) {
        read(adr, TYPE_NONE)
    }

    abstract fun send(adr : Int, data : Int, type : Int) : Boolean

    fun send(adr : Int, data : Int) {
        send (adr, data, TYPE_NONE)
    }

    companion object {
        var socket : Socket? = null
        var out: PrintWriter? = null
        var `in`: BufferedReader? = null
    }
}