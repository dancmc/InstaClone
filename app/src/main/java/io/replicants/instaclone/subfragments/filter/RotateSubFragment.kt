package io.replicants.instaclone.subfragments.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import kotlinx.android.synthetic.main.subfragment_rotate.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onScrollChange

class RotateSubFragment:BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): RotateSubFragment {
            val myFragment = RotateSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var layout :View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_rotate, container, false)


        var listener :ViewTreeObserver.OnGlobalLayoutListener? = null
        listener = ViewTreeObserver.OnGlobalLayoutListener{
            val scrollViewWidth = layout.subfragment_rotate_scroller.width
            val ratchetWidth = layout.subfragment_rotate_scroller.getChildAt(0).width

            val centreScroll = ratchetWidth/2 - scrollViewWidth/2
            val maxScroll = ratchetWidth-scrollViewWidth

            layout.subfragment_rotate_scroller.scrollTo( centreScroll, 0)


            layout.subfragment_rotate_scroller.onScrollChange { _, scrollX, _, _, _ ->
                (parentFragment as RotateImage).onRotateRatchetScrolled(centreScroll.toFloat(), scrollX.toFloat(), maxScroll.toFloat(), layout.subfragment_rotate_amount)
            }

            layout.subfragment_rotate_amount_reset.onClick {

                layout.subfragment_rotate_scroller.smoothScrollTo(centreScroll, 0)
            }

            layout.subfragment_rotate_rotateleft.onClick {
                (parentFragment as RotateImage).onRotateRightAngle(-90f)
            }

            layout.subfragment_rotate_rotateright.onClick {
                (parentFragment as RotateImage).onRotateRightAngle(90f)
            }

            layout.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }

        layout.viewTreeObserver.addOnGlobalLayoutListener(listener)



        return layout
    }

    interface RotateImage{
        fun onRotateRatchetScrolled(centre:Float, current:Float, max:Float, display:TextView)

        fun onRotateRightAngle(deg:Float)
    }


}