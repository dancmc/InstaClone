package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.replicants.instaclone.R
import kotlinx.android.synthetic.main.subfragment_get_photo.view.*
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout


class GetPhotoSubFragment:BaseSubFragment() {

    lateinit var layout:View
    var photoTakenListener: CameraSubFragment.PhotoTakenListener? = null
    lateinit var adapter :MyAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_get_photo, container, false)

        adapter = MyAdapter(childFragmentManager, photoTakenListener)
        layout.subfragment_get_photo_viewpager.adapter = adapter
        (layout.subfragment_get_photo_tabs as TabLayout).setupWithViewPager(layout.subfragment_get_photo_viewpager)
        layout.subfragment_get_photo_viewpager.currentItem = 1

        return layout
    }

    class MyAdapter(fm:FragmentManager, var photoTakenListener: CameraSubFragment.PhotoTakenListener?) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {

            if(position==0){
                return GallerySubFragment()
            } else {
                val camFragment = CameraSubFragment()
                camFragment.photoTakenListener = photoTakenListener
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
            (adapter.getItem(1) as CameraSubFragment).permissionGranted()
        }catch (e:ClassCastException){
            Log.d("GetPhotoFragment", e.message)
        }
    }

    fun permissionDenied(){
        try {
            (adapter.getItem(1) as CameraSubFragment).permissionDenied()
        }catch (e:ClassCastException){
            Log.d("GetPhotoFragment", e.message)
        }
    }
}