package io.replicants.instaclone.subfragments.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.BTListAdapter
import io.replicants.instaclone.pojos.BluetoothItem
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_bluetooth.view.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap


const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

const val MESSAGE_CONNECTED: Int = 3
const val MESSAGE_CONNECTING: Int = 4
const val MESSAGE_DISCONNECTED: Int = 5


private const val TAG = "TAG_BLUETOOTH"

class BluetoothSubFragment : BaseSubFragment(), BTListAdapter.Listener {

    companion object {

        @JvmStatic
        fun newInstance(): BluetoothSubFragment {
            val myFragment = BluetoothSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var layout: View
    lateinit var recycler: RecyclerView
    lateinit var adapter: BTListAdapter

    val REQUEST_ENABLE_BT = 288
    val REQUEST_ENABLE_DISCOVERABLE = 289
    val INSTACLONE_UUID = UUID.fromString("b7ef1602-d143-11e8-a8d5-f2801f1b9fd1")


    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothList = Collections.synchronizedList(ArrayList<BluetoothItem>())
    private var bluetoothMap = Collections.synchronizedMap(HashMap<String, ConnectedThread>())

    // can have many client and socket threads at same time, but only 1 server thread
    private var serverThread :ServerThread? = null


    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address

                    val item = BluetoothItem(device, deviceHardwareAddress, deviceName?:"")
                    if(item !in bluetoothList){
                        bluetoothList.add(item)
                        adapter.notifyDataSetChanged()
                    }
                }
                BluetoothDevice.ACTION_NAME_CHANGED->{
                    val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    println("Name $deviceName")
                }
            }
        }
    }

    // There is no need to check for Bluetooth and Bluetooth Admin permissions, they are normal permissions granted at install
    // https://developer.android.com/guide/topics/permissions/overview
    val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context?.registerReceiver(mReceiver, filter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }

        layout = inflater.inflate(R.layout.subfragment_bluetooth, container, false)

        layout.subfragment_bluetooth_toolbar_back.onClick { clickListeners?.popBackStack(false) }

        layout.subfragment_bluetooth_scan.onClick {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                startScan()
            }
        }

        adapter = BTListAdapter(context, bluetoothList)
        adapter.listener = this
        recycler = layout.subfragment_bluetooth_list
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            layout.subfragment_bluetooth_none.visibility = View.VISIBLE
            layout.subfragment_bluetooth_scan.visibility = View.GONE
        } else {
            layout.subfragment_bluetooth_none.visibility = View.GONE
            layout.subfragment_bluetooth_scan.visibility = View.VISIBLE
            handlePermissions()
        }

        return layout
    }


    fun setupLayout() {
        layout.subfragment_bluetooth_permissions.visibility = View.GONE
        layout.subfragment_bluetooth_scan.visibility = View.VISIBLE

        // todo initialise gallery part


    }

    // If scan button pressed, start server listen and client search
    // once a connection is established, close server socket, cancel discovery
    fun startScan() {
        bluetoothAdapter?.cancelDiscovery()
        bluetoothList.clear()
        adapter.notifyDataSetChanged()
        bluetoothAdapter?.startDiscovery()

        if(bluetoothAdapter?.scanMode!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60)
            }
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE)
        }
    }


    override fun onClick(bluetoothItem: BluetoothItem) {

        val client = ClientThread(getBluetoothHandler(), bluetoothItem.device)
        client.start()

    }

    fun getBluetoothHandler():Handler{
        return  object:Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                when(msg.what){
                    MESSAGE_CONNECTED->{
                        (msg.obj as? BluetoothSocket)?.apply {
                            handleConnection(this)
                        }
                    }
                    MESSAGE_CONNECTING->{
                        (msg.obj as? BluetoothDevice)?.apply {
                            handleConnecting(this)
                        }
                    }
                    MESSAGE_DISCONNECTED->{
                        (msg.obj as? BluetoothDevice)?.apply {
                            handleDisconnection(this)
                        }
                    }
                }
            }
        }
    }

    fun handleConnection(socket: BluetoothSocket){

    }

    fun handleDisconnection(device:BluetoothDevice){

    }

    fun handleConnecting(device:BluetoothDevice){

    }

    private inner class ConnectedThread(private val handler:Handler,var bsocket: BluetoothSocket?) : Thread() {

        private var inStream: InputStream? = null
        private var outStream: OutputStream? =null
        private val buffer: ByteArray = ByteArray(1024) // buffer store for the stream

        override fun run(){
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

    private inner class ClientThread(private val handler:Handler,val device: BluetoothDevice) : Thread() {

        public override fun run() {
            bluetoothAdapter?.cancelDiscovery()

            try {
                val newSocket = device.createRfcommSocketToServiceRecord(INSTACLONE_UUID)
                launch {
                    delay(6000)
                    if(this@ClientThread.isAlive){
                        newSocket?.close()
                        handler.obtainMessage(MESSAGE_DISCONNECTED, device).sendToTarget()
                    }
                }
                newSocket?.use { socket ->
                    handler.obtainMessage(MESSAGE_CONNECTING, device).sendToTarget()
                    socket.connect()
                    manageMyConnectedSocket(socket)
                }
            }catch (e:IOException){
                Log.e(TAG, e.message)
            }
        }

        fun manageMyConnectedSocket(socket:BluetoothSocket?){
            if(socket==null){
                handler.obtainMessage(MESSAGE_DISCONNECTED, device).sendToTarget()
            }else{
                handler.obtainMessage(MESSAGE_CONNECTED, socket)
            }
        }

    }


    private inner class ServerThread(private val handler: Handler, val timeout:Int) : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("InstaClone", INSTACLONE_UUID)
        }

        override fun run() {

            // timeout
            launch{
                delay(timeout)
                cancel()
            }

            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }?.apply {
                    handler.obtainMessage(MESSAGE_CONNECTED, this)
                }
            }

        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }finally {
                serverThread = null
            }
        }
    }





    // PERMISSIONS SECTION

    fun handlePermissions() {

        val permsNeeded = getPermissionsNeeded()
        if (permsNeeded.size > 0) {
            requestPermissions(permsNeeded.toTypedArray(), Prefs.BLUETOOTH_REQUEST_CODE)
        } else {
            setupLayout()
        }

    }

    fun getPermissionsNeeded(): ArrayList<String> {
        val permsNeeded = ArrayList<String>()
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(context!!, it) != PackageManager.PERMISSION_GRANTED) {
                permsNeeded.add(it)
            }
        }
        return permsNeeded
    }

    fun permissionsDenied() {
        layout.subfragment_bluetooth_permissions.visibility = View.VISIBLE
        layout.subfragment_bluetooth_scan.visibility = View.GONE

        layout.subfragment_bluetooth_permissions.onClick {

            if(getPermissionsNeeded().size>0) {
                val storageDeniedForever = Prefs.getInstance().readBoolean(Prefs.EXTERNAL_STORAGE_DENIED_FOREVER, false)
                val locationDeniedForever = Prefs.getInstance().readBoolean(Prefs.LOCATION_DENIED_FOREVER, false)

                when {
                    storageDeniedForever && locationDeniedForever -> {
                        context?.let { c ->
                            Utils.redirectToSettings(R.string.request_storage_location_title, R.string.request_storage_location_text, c)
                        }
                    }
                    storageDeniedForever -> {
                        context?.let { c ->
                            Utils.redirectToSettings(R.string.request_storage_title, R.string.request_storage_text, c)
                        }
                    }
                    locationDeniedForever -> {
                        context?.let { c ->
                            Utils.redirectToSettings(R.string.request_storage_location_title, R.string.request_location_text, c)
                        }
                    }
                    else-> handlePermissions()

                }
            } else {
                handlePermissions()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Prefs.BLUETOOTH_REQUEST_CODE) {
            permissions.forEachIndexed { index, s ->
                when (s) {

                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        } else {
                            // check if user checked never show again
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Prefs.getInstance().writeBoolean(Prefs.LOCATION_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                            }

                        }
                    }

                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        } else {
                            // check if user checked never show again
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Prefs.getInstance().writeBoolean(Prefs.EXTERNAL_STORAGE_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                            }

                        }
                    }

                }
            }

            if (getPermissionsNeeded().size > 0) {
                permissionsDenied()
            } else {
                setupLayout()
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(mReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            REQUEST_ENABLE_DISCOVERABLE->{
                if(resultCode!=Activity.RESULT_CANCELED){
                    serverThread?.cancel()
                    serverThread = ServerThread(getBluetoothHandler(), resultCode)
                }
            }
            else ->super.onActivityResult(requestCode, resultCode, data)
        }
    }
}