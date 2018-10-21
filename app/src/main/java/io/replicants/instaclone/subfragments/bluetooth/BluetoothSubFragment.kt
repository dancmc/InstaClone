package io.replicants.instaclone.subfragments.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.activities.BluetoothActivity
import io.replicants.instaclone.adapters.BluetoothListAdapter
import io.replicants.instaclone.adapters.BluetoothGalleryCursorAdapter
import io.replicants.instaclone.pojos.BluetoothItem
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.GalleryPagerFragment
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.utilities.Utils.Companion.getDirectoryCursor
import io.replicants.instaclone.views.FlingView
import kotlinx.android.synthetic.main.subfragment_bluetooth.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.*


const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

const val MESSAGE_CONNECTED: Int = 3
const val MESSAGE_CONNECTING: Int = 4
const val MESSAGE_DISCONNECTED: Int = 5
const val MESSAGE_PHOTO_RECEIVED: Int = 6
const val MESSAGE_SEND_FAILED: Int = 7
const val MESSAGE_SEND_SUCCEEDED: Int = 8


const val ACK_FAIL="ACK_FAIL"
const val ACK_SUCCESS="ACK_SUCCESS"


private const val TAG = "TAG_BLUETOOTH"

class BluetoothSubFragment : BaseSubFragment(), BluetoothListAdapter.Listener, BluetoothGalleryCursorAdapter.Listener {

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
    lateinit var bluetoothRecycler: RecyclerView
    lateinit var bluetoothRecyclerAdapter: BluetoothListAdapter

    lateinit var galleryRecycler: RecyclerView
    lateinit var galleryRecyclerAdapter: BluetoothGalleryCursorAdapter
    private var directoryList = ArrayList<GalleryPagerFragment.ImageDirectory>()
    lateinit var gridManager: GridLayoutManager

    val REQUEST_ENABLE_BT = 288
    val REQUEST_ENABLE_DISCOVERABLE = 289
    var galleryTopY = 0f


    private var bluetoothAdapter: BluetoothAdapter? = null
    lateinit var parentActivity :BluetoothActivity


    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address

                    val item = BluetoothItem(device, deviceHardwareAddress, deviceName ?: "")
                    parentActivity.bluetoothList.let { list->
                        if (item !in list) {
                            list.add(item)
                            bluetoothRecyclerAdapter.notifyDataSetChanged()
                        }
                    }
                }
                BluetoothDevice.ACTION_NAME_CHANGED -> {
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }

        parentActivity = activity as BluetoothActivity

        layout = inflater.inflate(R.layout.subfragment_bluetooth, container, false)
        layout.subfragment_bluetooth_root.flingView = layout.subfragment_bluetooth_flingview
        layout.subfragment_bluetooth_root.flingView?.listener = object: FlingView.Listener{
            override fun endMove(y: Int, path: String) {
                if(y<galleryTopY){
                    if(parentActivity.sendTo!=null) {
                        (activity as? BluetoothActivityInterface)?.photoObtained(path)
                    }else {
                        context?.toast("No user selected to send to")
                    }

                }
            }
        }

        layout.subfragment_bluetooth_toolbar_back.onClick { parentActivity.finish() }

        layout.subfragment_bluetooth_scan.onClick {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                startScan()
            }
        }

        bluetoothRecyclerAdapter = BluetoothListAdapter(context, parentActivity.bluetoothList)
        bluetoothRecyclerAdapter.listener = this
        bluetoothRecycler = layout.subfragment_bluetooth_list
        bluetoothRecycler.layoutManager = LinearLayoutManager(context)
        bluetoothRecycler.adapter = bluetoothRecyclerAdapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            layout.subfragment_bluetooth_none.visibility = View.VISIBLE
            layout.subfragment_bluetooth_scan.visibility = View.GONE
        } else {
            layout.subfragment_bluetooth_none.visibility = View.GONE
            layout.subfragment_bluetooth_scan.visibility = View.VISIBLE
            handlePermissions()
        }

        galleryRecycler = layout.subfragment_bluetooth_gallery
        galleryRecycler.setHasFixedSize(true)
        galleryRecycler.setItemViewCacheSize(40)
        galleryRecycler.setDrawingCacheEnabled(true)
        gridManager = GridLayoutManager(activity, 4);
        galleryRecycler.layoutManager = gridManager


        return layout
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context?.registerReceiver(mReceiver, filter)
    }

    fun setupLayout() {
        layout.subfragment_bluetooth_permissions_bluetooth.visibility = View.GONE
        layout.subfragment_bluetooth_scan.visibility = View.VISIBLE


        populateDirectory()
    }

    private fun populateDirectory() {

        doAsync {
            val result = Utils.getImageDirectories(activity)
            uiThread {
                directoryList.clear()
                directoryList.addAll(result)
                // put in spinner

                val dirListString = directoryList.mapTo(ArrayList()) { dir -> dir.albumName }
                val spinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, dirListString).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                layout.subfragment_bluetooth_toolbar_directory_spinner.adapter = spinnerAdapter
                layout.subfragment_bluetooth_toolbar_directory_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        galleryRecyclerAdapter.swapCursor(getDirectoryCursor(context!!, directoryList[position]))
                    }
                }
                val initialCursor = getDirectoryCursor(context!!, directoryList[0])
                galleryRecyclerAdapter = BluetoothGalleryCursorAdapter(context!!, initialCursor, this@BluetoothSubFragment)
                galleryRecycler.adapter = galleryRecyclerAdapter

            }
        }

    }


    override fun onLongClick(v:View, path:String) {


        galleryTopY = layout.subfragment_bluetooth_gallery.top.toFloat()
        layout.subfragment_bluetooth_flingview.inLongClickMode = true

        v.isDrawingCacheEnabled = true
        v.buildDrawingCache()
        val bmp = Bitmap.createBitmap(v.drawingCache)
        v.isDrawingCacheEnabled = false
        layout.subfragment_bluetooth_flingview.load(bmp, path)


        val vibrate = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrate.vibrate(60);
        }

    }


    // If scan button pressed, start server listen and client search
    // once a connection is established, close server socket, cancel discovery
    private fun startScan() {
        bluetoothAdapter?.cancelDiscovery()
        parentActivity.bluetoothList.clear()
        bluetoothRecyclerAdapter.notifyDataSetChanged()
        bluetoothAdapter?.startDiscovery()

        if (bluetoothAdapter?.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180)
            }
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE)
        }
        parentActivity.serverThread?.cancel()
        parentActivity.serverThread = ServerThread(bluetoothAdapter, getBluetoothHandler(), 180000)
        parentActivity.serverThread?.start()
    }


    override fun onClick(bluetoothItem: BluetoothItem) {

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }

        when {
            !bluetoothItem.connecting && !bluetoothItem.connected -> {
                bluetoothAdapter?.cancelDiscovery()
                val client = ClientThread(getBluetoothHandler(), bluetoothItem.device)
                client.start()
            }
            bluetoothItem.connected -> {
                val dialog = AlertDialog.Builder(context!!).apply {
                    val items = arrayOf<CharSequence>("Send To", "Disconnect", "Nothing")
                    setSingleChoiceItems(items, -1) { dialog: DialogInterface?, which: Int ->
                        when (which) {
                            0 -> {
                                parentActivity.sendTo = bluetoothItem.address
                                layout.subfragment_bluetooth_selected.text = if (bluetoothItem.name.isNotBlank()) bluetoothItem.name else bluetoothItem.address
                                dialog?.dismiss()
                            }
                            1 -> {
                                handleDisconnection(bluetoothItem.device)
                                dialog?.dismiss()
                            }
                            2 -> {
                                dialog?.dismiss()
                            }
                        }
                    }

                    setTitle("Choose Action")
                }.show()
            }
            bluetoothItem.connecting -> {

            }
        }

    }

    fun getBluetoothHandler(name:String="", connectedAddress:String=""): Handler {
        return object : Handler(Looper.getMainLooper()){

            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_CONNECTED -> {
                        (msg.obj as? BluetoothSocket)?.apply {
                            handleConnection(this)
                        }
                    }
                    MESSAGE_CONNECTING -> {
                        (msg.obj as? BluetoothDevice)?.apply {
                            handleConnecting(this)
                        }
                    }
                    MESSAGE_DISCONNECTED -> {
                        (msg.obj as? BluetoothDevice)?.apply {
                            if(this.name!=null && this.name.isNotBlank()){
                                activity?.toast("Disconnected from ${this.name}")
                            } else {
                                activity?.toast("Disconnected from ${this.address}")
                            }
                            handleDisconnection(this)
                        }
                    }
                    MESSAGE_TOAST ->{
                        (msg.obj as? String)?.apply {
                            activity?.toast(this)
                        }
                    }
                    MESSAGE_PHOTO_RECEIVED->{
                        (msg.obj as? String)?.apply {
                            parentActivity.receivedPhoto(name, connectedAddress, this)
                        }
                    }

                    MESSAGE_SEND_SUCCEEDED->{
                        parentActivity.handleSendSuccess()
                    }

                    MESSAGE_SEND_FAILED->{
                        parentActivity.handleSendError()
                    }

                }
            }
        }
    }

    fun handleConnection(socket: BluetoothSocket) {
        val address = socket.remoteDevice.address
        val name = socket.remoteDevice.name
        val item = parentActivity.bluetoothList.find { it.address == address }
                ?: BluetoothItem(socket.remoteDevice, address, socket.remoteDevice.name
                        ?: "").apply {
                    parentActivity.bluetoothList.add(this)
                }

        item.connected = true
        item.connecting = false
        bluetoothRecyclerAdapter.notifyDataSetChanged()

        if(parentActivity.sendTo==null){
            parentActivity.sendTo = address
            layout.subfragment_bluetooth_selected.text =
                    if (name!=null && name.isNotBlank()) name else address
        }

        val connectedThread = ConnectedThread(getBluetoothHandler(name = name?:"", connectedAddress= address), socket)
        parentActivity.bluetoothMap[address]?.cancel()
        parentActivity.bluetoothMap[address] = connectedThread
        connectedThread.start()
    }

    fun handleDisconnection(device: BluetoothDevice) {
        val address = device.address
        val item = parentActivity.bluetoothList.find { it.address == address }
        item?.connecting = false
        item?.connected = false
        bluetoothRecyclerAdapter.notifyDataSetChanged()

        parentActivity.bluetoothMap.remove(address)?.cancel()
        if (parentActivity.sendTo == address) {
            parentActivity.sendTo = null
            layout.subfragment_bluetooth_selected.text = ""
        }
    }

    fun handleConnecting(device: BluetoothDevice) {
        val address = device.address
        var item = parentActivity.bluetoothList.find { it.address == address }
                ?: BluetoothItem(device, address, device.name ?: "").apply {
                    parentActivity.bluetoothList.add(this)
                }

        item.connected = false
        item.connecting = true
        bluetoothRecyclerAdapter.notifyDataSetChanged()
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
        layout.subfragment_bluetooth_permissions_bluetooth.visibility = View.VISIBLE
        layout.subfragment_bluetooth_scan.visibility = View.GONE

        layout.subfragment_bluetooth_permissions_bluetooth.onClick {

            if (getPermissionsNeeded().size > 0) {
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
                    else -> handlePermissions()

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

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(mReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ENABLE_DISCOVERABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    startScan()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }


}