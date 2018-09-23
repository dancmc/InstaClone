package io.replicants.instaclone.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.CameraSubFragment
import io.replicants.instaclone.subfragments.GallerySubFragment
import io.replicants.instaclone.subfragments.GetPhotoSubFragment
import io.replicants.instaclone.utilities.Prefs
import org.jetbrains.anko.toast

// Same as Instagram's Upload Photo activity
class AddPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        val getPhotoFragment = GetPhotoSubFragment()
        getPhotoFragment.photoTakenListener = object : CameraSubFragment.PhotoTakenListener {
            override fun photoTaken(filename: String) {
                //TODO
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
}