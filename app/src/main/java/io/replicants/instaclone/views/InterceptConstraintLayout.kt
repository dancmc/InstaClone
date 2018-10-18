package io.replicants.instaclone.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import io.replicants.instaclone.subfragments.bluetooth.FlingView

class InterceptConstraintLayout:ConstraintLayout{
    constructor(context: Context) : super(context) {}

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, @Nullable attrs: AttributeSet, intDefStyle:Int) : super(context, attrs, intDefStyle) {}


    var lastX = 0f
    var lastY = 0f

    var flingView :FlingView?=null


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {


        when(ev?.actionMasked){
            MotionEvent.ACTION_DOWN->{
                lastX = ev.x
                lastY = ev.y

            }
            MotionEvent.ACTION_UP->{

            }
        }

        if (flingView?.inLongClickMode == true) {
            return flingView?.dispatchTouchEvent(ev) ?: super.dispatchTouchEvent(ev)
        }else{
            return super.dispatchTouchEvent(ev)
        }
    }
}