package io.replicants.instaclone.activities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.Photo
import io.replicants.instaclone.subfragments.bluetooth.BluetoothActivityInterface
import io.replicants.instaclone.subfragments.bluetooth.BluetoothCropSubFragment
import io.replicants.instaclone.subfragments.bluetooth.BluetoothSubFragment
import io.replicants.instaclone.subfragments.upload.edit.EditPhotoSubFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.PickPhotoSubFragment
import io.replicants.instaclone.subfragments.upload.post.PostPhotoSubFragment
import io.replicants.instaclone.utilities.MyApplication
import io.replicants.instaclone.utilities.Prefs
import org.jetbrains.anko.toast
import java.util.*

// Same as Instagram's Upload Photo activity
class BluetoothActivity : AppCompatActivity(), EditPhotoSubFragment.PhotoEditListener,BluetoothActivityInterface{

    val photoID = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)


        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        val bluetoothFragment = BluetoothSubFragment.newInstance()

        transaction.add(R.id.bluetooth_container, bluetoothFragment, "bluetooth")
        transaction.commit()

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
//        val tx = supportFragmentManager.beginTransaction()
//
//        val postFrag = PostPhotoSubFragment.newInstance
//        tx.replace(R.id.bluetooth_container, postFrag)
//        tx.addToBackStack("photoEdited")
//        tx.commit()
        toast("moving to send fragment")
    }



    override fun editCancelled() {
        supportFragmentManager.popBackStack("photoObtained", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun sendPhoto(photo: Photo, filepath: String) {

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


}