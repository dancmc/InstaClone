package io.replicants.instaclone.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.subfragments.filter.EditPhotoSubFragment
import io.replicants.instaclone.subfragments.GallerySubFragment
import io.replicants.instaclone.subfragments.GetPhotoSubFragment
import io.replicants.instaclone.utilities.Prefs
import org.jetbrains.anko.toast
import java.io.File

// Same as Instagram's Upload Photo activity
class AddPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        deleteExtraPhotos()

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        val getPhotoFragment = GetPhotoSubFragment()
        getPhotoFragment.photoTakenListener = object : GetPhotoSubFragment.PhotoObtainedListener {
            override fun photoObtained(filename: String) {
                val tx = supportFragmentManager.beginTransaction()
                val filterFrag = EditPhotoSubFragment.newInstance(filename)
                tx.replace(R.id.add_photo_container, filterFrag)
                tx.addToBackStack(null)
                tx.commit()
                toast("Move to filter fragment")
            }
        }
        transaction.add(R.id.add_photo_container, getPhotoFragment, "getPhoto")
        transaction.commit()


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {

            Prefs.CAMERA_REQUEST_CODE -> {
                if (permissions.size == 1 && permissions[0] == Manifest.permission.CAMERA) {
                    val getPhotoFragment = supportFragmentManager.findFragmentByTag("getPhoto") as GetPhotoSubFragment?
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        if(getPhotoFragment!=null){
                            (getPhotoFragment.adapter.getItem(1) as GallerySubFragment).permissionGranted()
                        }
                        toast("Camera permission granted")
                    } else {
                        // check if user checked never show again
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Prefs.getInstance().writeBoolean(Prefs.CAMERA_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                        }

                        if(getPhotoFragment!=null){
                            (getPhotoFragment.adapter.getItem(1) as GallerySubFragment).permissionDenied()
                        }

                        toast("Camera permission not granted")
                    }
                }
            }
            Prefs.EXTERNAL_STORAGE_CODE->{
                if (permissions.size == 1 && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    val getPhotoFragment = supportFragmentManager.findFragmentByTag("getPhoto") as GetPhotoSubFragment?

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Prefs.EXTERNAL_STORAGE_CODE)

                        if(getPhotoFragment!=null){
                            (getPhotoFragment.adapter.getItem(0) as GallerySubFragment).permissionGranted()
                        }

                        toast("Storage permission granted")
                    } else {
                        // check if user checked never show again
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Prefs.getInstance().writeBoolean(Prefs.EXTERNAL_STORAGE_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                        }
                        if(getPhotoFragment!=null){
                            (getPhotoFragment.adapter.getItem(0) as GallerySubFragment).permissionDenied()
                        }

                        toast("Storage permission not granted")
                    }
                }
            }
        }
    }

    private fun deleteExtraPhotos(){
        if (ContextCompat.checkSelfPermission(this as AppCompatActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val savedSet = HashSet<String>()
            Realm.getDefaultInstance().where(SavedPhoto::class.java).findAll().forEach {
                savedSet.add(it.photoFile)
            }
            val folder = File(filesDir, "photos")
            if(folder.exists()){
                folder.listFiles().forEach {
                    if(!it.isDirectory && it.absolutePath !in savedSet && it.absolutePath.endsWith(".jpg")){
                        it.delete()
                    }
                }
            }
        }

    }

    override fun onBackPressed() {
        var passToSuper = true
        val frag = supportFragmentManager.findFragmentById(R.id.add_photo_container)
        if(frag!=null && frag is EditPhotoSubFragment){
            passToSuper = !frag.cancelCurrentEdit()
        }
        if(passToSuper) {
            super.onBackPressed()
        }
    }
}