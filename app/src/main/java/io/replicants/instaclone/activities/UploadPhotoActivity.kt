package io.replicants.instaclone.activities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.subfragments.upload.edit.EditPhotoSubFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.PickPhotoSubFragment
import io.replicants.instaclone.subfragments.upload.post.PostPhotoSubFragment
import io.replicants.instaclone.utilities.MyApplication
import io.replicants.instaclone.utilities.Prefs
import org.jetbrains.anko.toast
import java.io.File

// Same as Instagram's Upload Photo activity
class UploadPhotoActivity : AppCompatActivity(), PickPhotoSubFragment.PhotoObtainedListener, EditPhotoSubFragment.PhotoEditListener, PostPhotoSubFragment.PhotoPostListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        val getPhotoFragment = PickPhotoSubFragment()
        getPhotoFragment.photoTakenListener = this

        transaction.add(R.id.add_photo_container, getPhotoFragment, "getPhoto")
        transaction.commit()


    }


    override fun photoObtained(photoID: String, filename: String) {
        val tx = supportFragmentManager.beginTransaction()
        val editFrag = EditPhotoSubFragment.newInstance(photoID, filename)
        editFrag.listener = this
        tx.replace(R.id.add_photo_container, editFrag)
        tx.addToBackStack("photoObtained")
        tx.commit()
    }

    override fun photoEdited(photoID: String, postFilepath: String) {
        val tx = supportFragmentManager.beginTransaction()
        val postFrag = PostPhotoSubFragment.newInstance(photoID, postFilepath)
        postFrag.listener = this
        tx.replace(R.id.add_photo_container, postFrag)
        tx.addToBackStack("photoEdited")
        tx.commit()
    }

    override fun editCancelled() {
        supportFragmentManager.popBackStack("photoObtained", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun photoPosted() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun goBackToEdit() {
        supportFragmentManager.popBackStack("photoEdited", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }


    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.add_photo_container)
        when{
            frag != null && frag is EditPhotoSubFragment->frag.cancelCurrentEdit()
            frag != null && frag is PickPhotoSubFragment->{
                if(!frag.handleBackPressed()){
                    super.onBackPressed()
                }
            }
            else->super.onBackPressed()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Prefs.LOCATION_REQUEST_CODE) {
            if (permissions.size == 1 && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION ) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyApplication.instance.activateStoredCallback(true, this)
                    toast("Location permission granted")
                }else{
                    // check if user checked never show again
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        Prefs.getInstance().writeBoolean(Prefs.LOCATION_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                    }
                    MyApplication.instance.activateStoredCallback(false, this)
                    toast("Location permission not granted")
                }
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }
}