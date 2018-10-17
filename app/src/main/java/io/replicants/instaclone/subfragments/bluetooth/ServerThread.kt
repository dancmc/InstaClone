package io.replicants.instaclone.subfragments.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import io.replicants.instaclone.utilities.Prefs.INSTACLONE_UUID
import java.io.IOException

private const val TAG = "TAG_BLUETOOTH"

class ServerThread(private val bluetoothAdapter: BluetoothAdapter?, private val handler: Handler, val timeout: Int) : Thread() {

    private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter?.listenUsingRfcommWithServiceRecord("InstaClone", INSTACLONE_UUID)
    }

    override fun run() {

        // Keep listening until exception occurs or a socket is returned.
        var shouldLoop = true
        while (shouldLoop) {
            val socket: BluetoothSocket? = try {
                mmServerSocket?.accept(timeout)
            } catch (e: IOException) {
                Log.e(TAG, "Socket's accept() method failed", e)
                shouldLoop = false
                null
            }?.apply {
                handler.obtainMessage(MESSAGE_CONNECTED, this).sendToTarget()
            }
        }

    }

    // Closes the connect socket and causes the thread to finish.
    fun cancel() {
        try {
            mmServerSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }
}