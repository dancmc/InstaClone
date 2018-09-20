package io.replicants.instaclone.subfragments

import android.os.Bundle
import androidx.fragment.app.Fragment

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

}