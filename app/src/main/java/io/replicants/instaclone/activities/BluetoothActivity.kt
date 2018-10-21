package io.replicants.instaclone.activities

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.BluetoothItem
import io.replicants.instaclone.pojos.InRangePhoto
import io.replicants.instaclone.subfragments.bluetooth.*
import io.replicants.instaclone.subfragments.upload.edit.EditPhotoSubFragment
import kotlinx.coroutines.experimental.launch
import org.apache.commons.io.FileUtils
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*


class BluetoothActivity : AppCompatActivity(), EditPhotoSubFragment.PhotoEditListener,BluetoothActivityInterface{

    val photoID = UUID.randomUUID().toString()

    var bluetoothList = Collections.synchronizedList(ArrayList<BluetoothItem>())
    var bluetoothMap = Collections.synchronizedMap(HashMap<String, ConnectedThread>())
    var sendTo: String? = null

    // can have many client and socket threads at same time, but only 1 server thread
    var serverThread: ServerThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)


        val manager = supportFragmentManager
        if(supportFragmentManager.findFragmentById(R.id.bluetooth_container)==null){
            val transaction = manager.beginTransaction()
            val bluetoothFragment = BluetoothSubFragment.newInstance()

            transaction.add(R.id.bluetooth_container, bluetoothFragment, "bluetooth")
            transaction.commit()
        }
    }


    override fun photoObtained(filename: String) {
        val tx = supportFragmentManager.beginTransaction()
        val editFrag = BluetoothCropSubFragment.newInstance(photoID, filename)

        tx.replace(R.id.bluetooth_container, editFrag)
        tx.addToBackStack("photoObtained")
        tx.commit()
    }

    override fun photoCropped(filename: String) {
        val tx = supportFragmentManager.beginTransaction()
        val editFrag = EditPhotoSubFragment.newInstance(photoID, filename)
        editFrag.listener = this
        tx.replace(R.id.bluetooth_container, editFrag)
        tx.addToBackStack("photoCropped")
        tx.commit()
    }


    override fun photoEdited(photoID: String, postFilepath: String) {
        val tx = supportFragmentManager.beginTransaction()

        val sendFrag = BluetoothSendSubFragment.newInstance(postFilepath)
        tx.replace(R.id.bluetooth_container, sendFrag)
        tx.addToBackStack("photoEdited")
        tx.commit()
    }



    override fun editCancelled() {
        supportFragmentManager.popBackStack("photoCropped", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun sendPhoto(photo: InRangePhoto, filepath: String) {

        if (sendTo==null){
            toast("Recipient no longer connected!")
            supportFragmentManager.popBackStack("photoObtained", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            return
        }

        launch {
            val base64Photo = Base64.encodeToString(FileUtils.readFileToByteArray(File(filepath)), Base64.DEFAULT)

            val profilePhoto = File(filesDir, "profile.jpg")
            val base64Profile = if (profilePhoto.exists()) {
                Base64.encodeToString(FileUtils.readFileToByteArray(profilePhoto), Base64.DEFAULT)
            } else {
                ""
            }

            val json = JSONObject()
            json.put("photo", base64Photo)
            json.put("profile_photo", base64Profile)
            json.put("caption", photo.caption)
            json.put("location_name", photo.locationName)
            json.put("display_name", photo.displayName)
            json.put("latitude", photo.latitude)
            json.put("longitude", photo.longitude)
            json.put("timestamp", System.currentTimeMillis())
            json.put("width", photo.regularWidth)
            json.put("height", photo.regularHeight)

            bluetoothMap[sendTo].let { thread ->
                thread?.writeJson(json.toString())
            }
        }
    }

    override fun receivedPhoto(name:String, address:String, json: String) {
        val folder = File(filesDir, "inRange")

        try {
            val jsonObject = JSONObject(json)
            val photo = InRangePhoto()
            val photoID = UUID.randomUUID().toString()
            val profileID = "$photoID-profile"
            photo.photoID = photoID
            photo.profileImageID = profileID
            photo.caption = jsonObject.optString("caption")
            photo.locationName = jsonObject.optString("location_name")
            photo.displayName = jsonObject.optString("display_name")
            photo.latitude = jsonObject.optDouble("latitude", 999.0)
            photo.longitude = jsonObject.optDouble("longitude", 999.0)
            photo.timestamp = jsonObject.optLong("timestamp")
            photo.regularWidth = jsonObject.optInt("width")
            photo.regularHeight = jsonObject.optInt("height")

            val photoString= jsonObject.optString("photo")
            val photoByteArray = Base64.decode(photoString, Base64.DEFAULT)
            val photoFile = File(folder, "$photoID.jpg")
            FileUtils.writeByteArrayToFile(photoFile, photoByteArray)


            val profilePhotoString = jsonObject.optString("profile_photo")
            if(profilePhotoString.isNotBlank()){
                val profileByteArray = Base64.decode(profilePhotoString, Base64.DEFAULT)
                val profileFile = File(folder, "$profileID.jpg")
                FileUtils.writeByteArrayToFile(profileFile, profileByteArray)
            }

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(photo)
            realm.commitTransaction()

            bluetoothMap[address]?.write(ACK_SUCCESS)

            when{
                name.isNotBlank()->toast("Received photo from in-range friend : $name")
                address.isNotBlank()->toast("Received photo from in-range friend : $address")
                else-> toast("Received photo from in-range friend")
            }

        }catch (e:JSONException){
            toast("Received corrupted transmission")
            bluetoothMap[address]?.write(ACK_FAIL)
        } catch(e:IOException){
            toast("Failed to save received photo")
            bluetoothMap[address]?.write(ACK_FAIL)
        }

    }

    override fun handleSendSuccess() {
        toast("Photo sent!")
        val frag = supportFragmentManager.findFragmentById(R.id.bluetooth_container)
        (frag as? BluetoothSendSubFragment)?.apply {
            dismissProgress()
        }
        supportFragmentManager.popBackStack("photoObtained", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun handleSendError() {
        val frag = supportFragmentManager.findFragmentById(R.id.bluetooth_container)
        (frag as? BluetoothSendSubFragment)?.apply {
            dismissProgress()
        }
    }

    override fun errorAndGoBack(message: String) {
        supportFragmentManager.popBackStack()
        toast(message)
    }

    override fun goBack() {
        supportFragmentManager.popBackStack()
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.bluetooth_container)
        when{
            frag != null && frag is EditPhotoSubFragment->frag.cancelCurrentEdit(false)
            else->super.onBackPressed()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothMap.entries.forEach {
            it.value.cancel()
        }
        bluetoothMap.clear()
        serverThread?.cancel()
        serverThread = null
    }



}