package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.replicants.instaclone.R

class PhotoSpecificSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(photoIDs:ArrayList<String>): PhotoSpecificSubFragment {
            val myFragment = PhotoSpecificSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putStringArrayList("photoIDs", photoIDs)

            return myFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_photo_specific, container, false)

        return layout
    }
}