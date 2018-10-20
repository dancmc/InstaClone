package io.replicants.instaclone.subfragments.upload.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import kotlinx.android.synthetic.main.subfragment_edit_controls.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class EditControlsSubFragment:BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): EditControlsSubFragment {
            val myFragment = EditControlsSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    var onChooseListener: OnChooseListener?=null
    lateinit var layout :View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_edit_controls, container, false)

        layout.subfragment_edit_controls_adjust.onClick { onChooseListener?.goToAdjust() }
        layout.subfragment_edit_controls_filter.onClick { onChooseListener?.goToFilter() }
        layout.subfragment_edit_controls_brightness.onClick { onChooseListener?.goToBrightness() }
        layout.subfragment_edit_controls_contrast.onClick { onChooseListener?.goToContrast() }

        return layout
    }

    interface OnChooseListener{
        fun goToFilter()

        fun goToAdjust()

        fun goToBrightness()

        fun goToContrast()
    }


}