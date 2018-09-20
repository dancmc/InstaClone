package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.replicants.instaclone.R

class CommentsSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(photoID: String): CommentsSubFragment {
            val myFragment = CommentsSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("photoID", photoID)

            return myFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_comments, container, false)

        return layout
    }
}