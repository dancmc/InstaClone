package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import io.replicants.instaclone.R
import kotlinx.android.synthetic.main.subfragment_filter.view.*

class FilterSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(tempFileName: String): FilterSubFragment {
            val myFragment = FilterSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("fileName", tempFileName)

            return myFragment
        }
    }

    lateinit var layout: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_filter, container, false)
        val filename = arguments!!.getString("fileName")
        Glide.with(context!!).load(filename).into(layout.subfragment_filter_preview)


        return layout
    }
}