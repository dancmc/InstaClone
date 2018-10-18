package io.replicants.instaclone.subfragments.upload.edit

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_adjust.view.*
import kotlinx.android.synthetic.main.subfragment_edit_photo_cancel_done.view.*
import kotlinx.android.synthetic.main.subfragment_filter.view.*
import kotlinx.android.synthetic.main.subfragment_seekbar.view.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onScrollChange
import org.jetbrains.anko.sdk27.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.sdk27.coroutines.onTouch
import org.jetbrains.anko.toast

class FilterSubFragment:BaseSubFragment() {

    companion object {

        @JvmField
        val ORIGINAL = "ORIGINAL"

        @JvmField
        val SEPIA = "SEPIA"
        @JvmField
        val GREYSCALE = "GREYSCALE"
        @JvmField
        val ANTWERP = "ANTWERP"
        @JvmField
        val MYSTIC = "MYSTIC"

        @JvmStatic
        fun newInstance(filter:String): FilterSubFragment {
            val myFragment = FilterSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("filter",filter )

            return myFragment
        }
    }

    lateinit var layout :View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_filter, container, false)

        val startFilter = arguments?.getString("filter") ?: SEPIA

        layout.subfragment_filter_original.onClick {
            (parentFragment as? ImageFilter)?.putFilter(ORIGINAL)
        }

        layout.subfragment_filter_sepia.onClick {
            (parentFragment as? ImageFilter)?.putFilter(SEPIA)
        }

        layout.subfragment_filter_greyscale.onClick {
            (parentFragment as? ImageFilter)?.putFilter(GREYSCALE)
        }

        layout.subfragment_filter_antwerp.onClick {
            (parentFragment as? ImageFilter)?.putFilter(ANTWERP)
        }

        layout.subfragment_edit_controls_mystic.onClick {
            (parentFragment as? ImageFilter)?.putFilter(MYSTIC)
        }

        layout.subfragment_edit_photo_canceledit.onClick { (parentFragment as? ImageFilter)?.cancelCurrentEdit(true)}
        layout.subfragment_edit_photo_doneedit.onClick { (parentFragment as? ImageFilter)?.done()}

        return layout
    }

    interface ImageFilter {
        fun putFilter(filter: String)

        fun cancelCurrentEdit(withSaveDialog:Boolean):Boolean

        fun done()
    }


}