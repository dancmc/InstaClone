package io.replicants.instaclone.subfragments.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.*
import java.lang.StringBuilder
import kotlin.math.min

private const val TAG = "TAG_BLUETOOTH"
private const val START_MARKER = "START_MARKER"
private const val END_MARKER = "END_MARKER"

class ConnectedThread(private val handler: Handler, var bsocket: BluetoothSocket?) : Thread() {

    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null
    private var dataInStream :DataInputStream?= null
    private var dataOutStream :DataOutputStream?= null

    private var stringData = StringBuilder()
    private var timer:Job = launch {  }



    override fun run() {
        inStream = BufferedInputStream(bsocket?.inputStream)
        outStream = BufferedOutputStream(bsocket?.outputStream)
        dataInStream = DataInputStream(inStream)
        dataOutStream = DataOutputStream(outStream)


        while (true) {
            try {
                val string = dataInStream!!.readUTF()

                when(string){
                    ACK_SUCCESS->{
                        handler.obtainMessage(MESSAGE_TOAST, "Photo Sent!").sendToTarget()
                        handler.obtainMessage(MESSAGE_SEND_SUCCEEDED).sendToTarget()
                    }
                    ACK_FAIL->{
                        handler.obtainMessage(MESSAGE_TOAST, "Recipient received corrupted data, try again").sendToTarget()
                        handler.obtainMessage(MESSAGE_SEND_FAILED).sendToTarget()
                    }
                    START_MARKER->{
                        stringData = StringBuilder()
                        timer = launch {
                            delay(10000)
                            stringData  = StringBuilder()
                        }
                    }

                    END_MARKER->{
                        timer.cancel()
                        handler.obtainMessage(MESSAGE_PHOTO_RECEIVED,stringData.toString()).sendToTarget()
                        stringData = StringBuilder()
                    }

                    else->{
                        stringData.append(string)
                    }
                }

            } catch (e: Exception) {
                Log.d(TAG, "Input stream was disconnected", e)
                handler.obtainMessage(MESSAGE_DISCONNECTED, bsocket?.remoteDevice).sendToTarget()
                break
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    fun write(string: String) {
        try {
            dataOutStream!!.writeUTF(string)
            dataOutStream!!.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)

            // Send a failure message back to the activity.
            handler.obtainMessage(MESSAGE_SEND_FAILED).sendToTarget()
            handler.obtainMessage(MESSAGE_TOAST, "Couldn't send data to the other device").sendToTarget()
            handler.obtainMessage(MESSAGE_DISCONNECTED, bsocket?.remoteDevice).sendToTarget()
            return
        }
    }

    fun writeJson(string: String) {
        try {
            dataOutStream!!.writeUTF(START_MARKER)

            val length = string.length
            var start = 0
            while (start<length){
                val end = min(length, start+20000)
                dataOutStream!!.writeUTF(string.substring(start, end))
                start = end
            }

            dataOutStream!!.writeUTF(END_MARKER)
            dataOutStream!!.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)

            // Send a failure message back to the activity.
            handler.obtainMessage(MESSAGE_SEND_FAILED).sendToTarget()
            handler.obtainMessage(MESSAGE_TOAST, "Couldn't send data to the other device").sendToTarget()
            handler.obtainMessage(MESSAGE_DISCONNECTED, bsocket?.remoteDevice).sendToTarget()
            return
        }
    }

    // Call this method from the main activity to shut down the connection.
    fun cancel() {
        try {
            bsocket?.close()
            dataInStream?.close()
            dataOutStream?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }

}