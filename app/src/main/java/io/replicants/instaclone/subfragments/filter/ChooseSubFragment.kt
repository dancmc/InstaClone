package io.replicants.instaclone.subfragments.filter

import android.os.Bundle
import android.view.*
import android.widget.TextView
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import kotlinx.android.synthetic.main.subfragment_choose.view.*
import kotlinx.android.synthetic.main.subfragment_rotate.view.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onScrollChange
import org.jetbrains.anko.sdk27.coroutines.onTouch

class ChooseSubFragment:BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): ChooseSubFragment {
            val myFragment = ChooseSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    var onChooseListener:OnChooseListener?=null
    lateinit var layout :View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_choose, container, false)

        layout.subfragment_choose_adjust.onClick { onChooseListener?.goToAdjust() }
        layout.subfragment_choose_filter.onClick { onChooseListener?.goToFilter() }
        layout.subfragment_choose_brightness.onClick { onChooseListener?.goToBrightness() }
        layout.subfragment_choose_contrast.onClick { onChooseListener?.goToContrast() }

        return layout
    }

    interface OnChooseListener{
        fun goToFilter()

        fun goToAdjust()

        fun goToBrightness()

        fun goToContrast()
    }


}