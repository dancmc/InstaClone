package io.replicants.instaclone.subfragments.upload.pickphoto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Prefs
import kotlinx.android.synthetic.main.subfragment_pick_photo.view.*


class PickPhotoSubFragment: BaseSubFragment() {

    lateinit var layout:View
    var photoTakenListener: PhotoObtainedListener? = null
    lateinit var adapter : PickPhotoVPAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if(this::layout.isInitialized){
            return layout
        }

        layout = inflater.inflate(R.layout.subfragment_pick_photo, container, false)

        adapter = PickPhotoVPAdapter(childFragmentManager, photoTakenListener)
        layout.subfragment_pick_photo_viewpager.adapter = adapter
        (layout.subfragment_pick_photo_tabs as TabLayout).setupWithViewPager(layout.subfragment_pick_photo_viewpager)
        layout.subfragment_pick_photo_viewpager.currentItem = 1

        getPermissions()

        return layout
    }


    class PickPhotoVPAdapter(fm:FragmentManager, var photoObtainedListener: PhotoObtainedListener?) : FragmentStatePagerAdapter(fm) {

        val fragmentArray = Array<Fragment?>(2){null}

        override fun getItem(position: Int): Fragment {

            return if(position==0){
                fragmentArray[0] ?: GalleryPagerFragment().apply {
                    fragmentArray[0] = this
                    this.photoObtainedListener = this@PickPhotoVPAdapter.photoObtainedListener
                }
            } else {
                fragmentArray[1] ?: CameraPagerFragment().apply {
                    fragmentArray[1] = this
                    this.photoObtainedListener = this@PickPhotoVPAdapter.photoObtainedListener
                }
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if(position==0){
                "Gallery"
            } else {
                "Camera"
            }
        }


    }

    fun getPermissions(){
        val perms = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        val permsNeeded = ArrayList<String>()
        perms.forEach {
            if (ContextCompat.checkSelfPermission(context!!, it)!=PackageManager.PERMISSION_GRANTED){
                permsNeeded.add(it)
            }
        }

        if(permsNeeded.size>0) {
            requestPermissions(permsNeeded.toTypedArray(), Prefs.CAMERA_STORAGE_REQUEST_CODE)
        }
    }


    interface PhotoObtainedListener {
        fun photoObtained(photoID:String, filename: String)
    }

    fun handleBackPressed():Boolean{
        return (adapter.fragmentArray[0] as? GalleryPagerFragment)?.handleBackPressed() ?: false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Prefs.CAMERA_STORAGE_REQUEST_CODE) {
            permissions.forEachIndexed { index, s ->
                when(s){
                    Manifest.permission.CAMERA->{
                        (adapter.fragmentArray[1] as? CameraPagerFragment)?.onRequestPermissionsResult(Prefs.CAMERA_REQUEST_CODE, arrayOf(s), intArrayOf(grantResults[index]))
                    }

                    Manifest.permission.READ_EXTERNAL_STORAGE->{
                        (adapter.fragmentArray[0] as? GalleryPagerFragment)?.onRequestPermissionsResult(Prefs.EXTERNAL_STORAGE_READ_CODE, arrayOf(s), intArrayOf(grantResults[index]))
                    }

                }
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}