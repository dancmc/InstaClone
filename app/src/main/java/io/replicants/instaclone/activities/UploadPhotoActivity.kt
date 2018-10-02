package io.replicants.instaclone.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.subfragments.upload.edit.EditPhotoSubFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.CameraPagerFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.GalleryPagerFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.PickPhotoSubFragment
import io.replicants.instaclone.utilities.Prefs
import org.jetbrains.anko.toast
import java.io.File

// Same as Instagram's Upload Photo activity
class UploadPhotoActivity : AppCompatActivity(), PickPhotoSubFragment.PhotoObtainedListener, EditPhotoSubFragment.PhotoEditedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_photo)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        deleteExtraPhotos()

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

    override fun photoEdited(photoID: String, fileName: String) {
        // todo
    }

    override fun editCancelled() {
        supportFragmentManager.popBackStack("photoObtained", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }


    private fun deleteExtraPhotos() {
        val savedSet = HashSet<String>()
        Realm.getDefaultInstance().where(SavedPhoto::class.java).findAll().forEach {
            savedSet.add(it.photoFile)
        }
        val folder = File(filesDir, "photos")
        if (folder.exists()) {
            folder.listFiles().forEach {
                if (!it.isDirectory && it.absolutePath !in savedSet && it.absolutePath.endsWith(".jpg")) {
                    it.delete()
                }
            }
        }

    }

    override fun onBackPressed() {
        var passToSuper = true
        val frag = supportFragmentManager.findFragmentById(R.id.add_photo_container)
        if (frag != null && frag is EditPhotoSubFragment) {
            passToSuper = !frag.cancelCurrentEdit()
        }
        if (passToSuper) {
            super.onBackPressed()
        }
    }
}