package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.replicants.instaclone.R

class ActivitySelfSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): ActivitySelfSubFragment {
            val myFragment = ActivitySelfSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_activity_self, container, false)




        return layout
    }
}