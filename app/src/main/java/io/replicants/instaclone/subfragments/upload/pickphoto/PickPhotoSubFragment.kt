package io.replicants.instaclone.subfragments.upload.pickphoto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.replicants.instaclone.R
import kotlinx.android.synthetic.main.subfragment_pick_photo.view.*
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import io.replicants.instaclone.subfragments.BaseSubFragment


class PickPhotoSubFragment: BaseSubFragment() {

    lateinit var layout:View
    var photoTakenListener: PhotoObtainedListener? = null
    lateinit var adapter : MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_pick_photo, container, false)

        adapter = MyAdapter(childFragmentManager, photoTakenListener)
        layout.subfragment_pick_photo_viewpager.adapter = adapter
        (layout.subfragment_pick_photo_tabs as TabLayout).setupWithViewPager(layout.subfragment_pick_photo_viewpager)
        layout.subfragment_pick_photo_viewpager.currentItem = 1

        return layout
    }

    class MyAdapter(fm:FragmentManager, var photoObtainedListener: PhotoObtainedListener?) : FragmentStatePagerAdapter(fm) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {

            if(position==0){
                val galleryFragment = GalleryPagerFragment()
                galleryFragment.photoObtainedListener = photoObtainedListener
                return galleryFragment
            } else {
                val camFragment = CameraPagerFragment()
                camFragment.photoObtainedListener = photoObtainedListener
                return camFragment
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if(position==0){
                "Gallery"
            } else {
                "Camera"
            }
        }
    }

    fun permissionGranted(){
        try {
            (adapter.getItem(1) as CameraPagerFragment).permissionGranted()
        }catch (e:ClassCastException){
            Log.d("GetPhotoFragment", e.message)
        }
    }

    fun permissionDenied(){
        try {
            (adapter.getItem(1) as CameraPagerFragment).permissionDenied()
        }catch (e:ClassCastException){
            Log.d("GetPhotoFragment", e.message)
        }
    }

    interface PhotoObtainedListener {
        fun photoObtained(photoID:String, filename: String)
    }
}