package de.blankedv.lanbahnpanel.railroad

import android.content.Context
import android.util.Log
import de.blankedv.lanbahnpanel.model.TAG

abstract class RRConnectionThread(private var context: Context?, private val ip: String, private val port: Int) : Thread() {

    @Volatile
    private var shutdownFlag: Boolean = false

    abstract override fun run()

    abstract fun isConnected(): Boolean

    abstract fun reconnect() : Boolean

    fun shutdown() {
        shutdownFlag = true
    }

    fun disconnectContext() {
        this.context = null
        Log.d(TAG, "lost context, stopping thread")
        shutdown()
    }


    abstract fun read(adr: Int)

    abstract fun send(adr : Int, data : Int, type : Int = 0)


}