package io.replicants.instaclone.subfragments

import android.os.Bundle
import androidx.fragment.app.Fragment

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
}