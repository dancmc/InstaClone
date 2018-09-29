package io.replicants.instaclone.subfragments.filter

import android.os.Bundle
import android.view.*
import android.widget.TextView
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_edit_cancel_done.view.*
import kotlinx.android.synthetic.main.subfragment_rotate.view.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onScrollChange
import org.jetbrains.anko.sdk27.coroutines.onTouch

class RotateSubFragment:BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(currentRotation:Float): RotateSubFragment {
            val myFragment = RotateSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putFloat("currentRotation",currentRotation )

            return myFragment
        }
    }

    lateinit var layout :View
    var isResetting = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_rotate, container, false)

        // convert to range -50 to 50
        var currentRotation = arguments?.getFloat("currentRotation", 0f) ?: 0f
        if(currentRotation>300f){
            currentRotation -=360f
        }

        layout.subfragment_rotate_amount.text = "${Utils.floatFormat.format(currentRotation)}°"

        var listener :ViewTreeObserver.OnGlobalLayoutListener? = null

        listener = ViewTreeObserver.OnGlobalLayoutListener{
            val scrollViewWidth = layout.subfragment_rotate_ratchet.width
            val ratchetWidth = layout.subfragment_rotate_ratchet.getChildAt(0).width

            val centreScroll = ratchetWidth/2 - scrollViewWidth/2
            val maxScroll = ratchetWidth-scrollViewWidth

            val startScroll = (currentRotation/50f * (maxScroll-centreScroll) + centreScroll).toInt()
            layout.subfragment_rotate_ratchet.scrollTo( startScroll, 0)


            layout.subfragment_rotate_ratchet.onScrollChange { _, scrollX, _, _, _ ->
                (parentFragment as ImageAdjust).onRotateRatchetScrolled(centreScroll.toFloat(), scrollX.toFloat(), maxScroll.toFloat(), layout.subfragment_rotate_amount)
                if(isResetting){
                    if(scrollX==centreScroll){
                        isResetting = false
                        (parentFragment as ImageAdjust).onRotateFinished()
                    }
                }
            }

            layout.subfragment_rotate_amount_reset.onClick {
                layout.subfragment_rotate_ratchet.smoothScrollTo(centreScroll, 0)
                // default animation time is 250ms, very hacky way of doing this but there is no clean way of telling
                isResetting = true
            }

            layout.subfragment_rotate_rotateleft.onClick {
                (parentFragment as ImageAdjust).onRotateRightAngle(-90f)
                (parentFragment as ImageAdjust).onRotateFinished()
            }

            layout.subfragment_rotate_rotateright.onClick {
                (parentFragment as ImageAdjust).onRotateRightAngle(90f)
                (parentFragment as ImageAdjust).onRotateFinished()
            }

            layout.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }

        layout.viewTreeObserver.addOnGlobalLayoutListener(listener)


        layout.subfragment_rotate_ratchet.onTouch { v, event ->
            if(event.actionMasked == MotionEvent.ACTION_UP){
                // theoretically this isn't needed with noflingscrollview but just to be safe
                launch {
                    delay(50)
                    (parentFragment as ImageAdjust).onRotateFinished()
                }
            }
        }

        layout.subfragment_edit_photo_canceledit.onClick { (parentFragment as ImageAdjust).cancelCurrentEdit()}
        layout.subfragment_edit_photo_doneedit.onClick { (parentFragment as ImageAdjust).done()}

        return layout
    }

    interface ImageAdjust{
        fun onRotateRatchetScrolled(centre:Float, current:Float, max:Float, display:TextView)

        fun onRotateRightAngle(deg:Float)

        fun onRotateFinished()

        fun cancelCurrentEdit():Boolean

        fun done()
    }


}