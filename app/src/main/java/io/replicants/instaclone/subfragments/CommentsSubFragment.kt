package io.replicants.instaclone.subfragments

import android.os.Bundle
import androidx.fragment.app.Fragment

class CommentsSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(photoID:String): CommentsSubFragment {
            val myFragment = CommentsSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("photoID", photoID)

            return myFragment
        }
    }

}