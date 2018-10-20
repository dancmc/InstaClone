package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.replicants.instaclone.R

class MapSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): MapSubFragment {
            val myFragment = MapSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var layout:View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(!this::layout.isInitialized) {
            layout = inflater.inflate(R.layout.subfragment_comments, container, false)

        }
        return layout
    }
}