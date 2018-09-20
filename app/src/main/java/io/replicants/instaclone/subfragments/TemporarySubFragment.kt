package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.replicants.instaclone.R
import io.replicants.instaclone.activities.MainActivity
import kotlinx.android.synthetic.main.subfragment_temp.view.*

class TemporarySubFragment : BaseSubFragment() {


    companion object {

        fun newInstance(): TemporarySubFragment {
            val myFragment = TemporarySubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_temp, container, false)

        layout.fragment_settings_button_logout.setOnClickListener {
            (activity as MainActivity).logout()
        }


        return layout
    }
}