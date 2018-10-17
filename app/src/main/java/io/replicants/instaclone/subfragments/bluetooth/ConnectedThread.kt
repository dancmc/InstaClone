package io.replicants.instaclone.subfragments.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val TAG = "TAG_BLUETOOTH"

class ConnectedThread(private val handler: Handler, var bsocket: BluetoothSocket?) : Thread() {

    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null
    private val buffer: ByteArray = ByteArray(1024) // buffer store for the stream

    override fun run() {
        inStream = bsocket?.inputStream
        outStream = bsocket?.outputStream

        var numBytes: Int // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            // Read from the InputStream.
            numBytes = try {
                inStream!!.read(buffer)
            } catch (e: Exception) {
                Log.d(TAG, "Input stream was disconnected", e)
                handler.obtainMessage(MESSAGE_DISCONNECTED, bsocket?.remoteDevice).sendToTarget()
                break
            }

            // Send the obtained bytes to the UI activity.
            val readMsg = handler.obtainMessage(MESSAGE_READ, numBytes, -1, buffer)
            readMsg.sendToTarget()
        }
    }

    // Call this from the main activity to send data to the remote device.
    fun write(bytes: ByteArray) {
        try {
            outStream?.write(bytes)
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)

            // Send a failure message back to the activity.
            val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
            val bundle = Bundle().apply {
                putString("toast", "Couldn't send data to the other device")
            }
            writeErrorMsg.data = bundle
            handler.sendMessage(writeErrorMsg)
            handler.obtainMessage(MESSAGE_DISCONNECTED, bsocket?.remoteDevice).sendToTarget()
            return
        }

        // Share the sent message with the UI activity.
        val writtenMsg = handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
        writtenMsg.sendToTarget()
    }

    // Call this method from the main activity to shut down the connection.
    fun cancel() {
        try {
            bsocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }

}