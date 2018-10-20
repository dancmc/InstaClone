package io.replicants.instaclone.subfragments.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import io.replicants.instaclone.utilities.Prefs.INSTACLONE_UUID
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.IOException

private const val TAG = "TAG_BLUETOOTH"

class ClientThread(private val handler: Handler, val device: BluetoothDevice) : Thread() {

    public override fun run() {
        handler.obtainMessage(MESSAGE_CONNECTING, device).sendToTarget()
        var newSocket: BluetoothSocket?=null
        try {
            newSocket = device.createRfcommSocketToServiceRecord(INSTACLONE_UUID)
            launch {
                delay(60000)
                if (this@ClientThread.isAlive) {
                    newSocket?.close()
                }
            }
            newSocket?.connect()
            manageMyConnectedSocket(newSocket)
        } catch (e: IOException) {
            Log.e(TAG, e.message?:"")
            newSocket?.close()
            handler.obtainMessage(MESSAGE_DISCONNECTED, device).sendToTarget()
        }
        sleep(20000)
    }

    fun manageMyConnectedSocket(socket: BluetoothSocket?) {

        if (socket == null) {
            handler.obtainMessage(MESSAGE_DISCONNECTED, device).sendToTarget()
        } else {
            handler.obtainMessage(MESSAGE_CONNECTED, socket).sendToTarget()
        }
    }

}