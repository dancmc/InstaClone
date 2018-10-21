package io.replicants.instaclone.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.Nullable
import io.replicants.instaclone.utilities.Easing

class FlingView : View {

    var lastX = 0f
    var lastY = 0f
    var moveX = 0f
    var moveY = 0f

    var inLongClickMode = false
        set(value) {
            field = value

            // have to generate fake down after long click event for fling to work properly
            flingDetector.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, lastX, lastY, 0))
        }

    var offsetX = 0f
    var offsetY = 0f
    private var bitmap: Bitmap? = null
    private var path = ""

    private val flingDetector = GestureDetector(context, FlingListener())
    private var flinging = false
    private var flingWasCancelled = false
    private var flingAnimator: ValueAnimator = ValueAnimator()
    var listener: Listener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, @Nullable attrs: AttributeSet, intDefStyle: Int) : super(context, attrs, intDefStyle) {}

    fun load(bitmap: Bitmap?, path: String) {
        this.bitmap = bitmap
        offsetX = (bitmap?.width?.toFloat() ?: 0f) / 2f
        offsetY = (bitmap?.height?.toFloat() ?: 0f) / 2f
        this.path = path
    }

    override fun onDraw(canvas: Canvas?) {
        if (inLongClickMode || bitmap != null) {
            bitmap?.let {
                canvas?.drawBitmap(bitmap, moveX - offsetX, moveY - offsetY, null)
            }

        } else {
            super.onDraw(canvas)
        }
    }

    var dragged = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        flingDetector.onTouchEvent(event)

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                moveX = event.x
                lastY = event.y
                moveY = event.y

                if (flinging) {
                    flingAnimator.cancel()
                }
            }
            MotionEvent.ACTION_UP -> {
                dragged = false
                if (!flinging) {
                    end()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                dragged = true
                moveX = event.x
                moveY = event.y
//                println("move, $inLongClickMode, ${event.x}, ${event.y}")

            }
        }
        invalidate()

        return if (!inLongClickMode) super.onTouchEvent(event) else true

    }


    private inner class FlingListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        // constants
        private val SWIPE_MIN_DISTANCE = 80 * 80
        private val SWIPE_THRESHOLD_VELOCITY = 150 * 150
        private val duration = 250f
        private val speedBase = 0.6

        private var dx = 0f
        private var dy = 0f


        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {


            if (Math.pow((e1!!.x - e2!!.x).toDouble(), 2.0) + Math.pow((e1.y - e2.y).toDouble(), 2.0) > SWIPE_MIN_DISTANCE &&
                    Math.pow(velocityX.toDouble(), 2.0) + Math.pow(velocityY.toDouble(), 2.0) > SWIPE_THRESHOLD_VELOCITY) {

                flinging = true
                flingWasCancelled = false
                println("fling")

                // calculate duration - exponential for now - constant time
                // ------

                // calculate dx and dy
                // v = ab^t, where say 0.05 = ab^ 2 - means v will become 5% in 2 seconds
                // total distance in x secs = ab^x/ln(b)
                dx = velocityX / 10f * Math.pow(speedBase, 1.0).toFloat() / -Math.log(speedBase).toFloat()
                dy = velocityY / 10f * Math.pow(speedBase, 1.0).toFloat() / -Math.log(speedBase).toFloat()


                flingAnimator = ValueAnimator.ofFloat(duration)
                flingAnimator.duration = duration.toLong()
                flingAnimator.interpolator = LinearInterpolator()

                var cumulativeTranslationX = 0f
                var cumulativeTranslationY = 0f

                // at 1s, or after it goes past bounds, whichever comes later, change target to clampedTarget
                flingAnimator.addUpdateListener {
                    val animatedValue = it.animatedValue as Float

                    val targetTranslationXAtTime = Easing.easeOutQuart(animatedValue, 0f, dx, duration)
                    val targetTranslationYAtTime = Easing.easeOutQuart(animatedValue, 0f, dy, duration)
                    val translateXStep = targetTranslationXAtTime - cumulativeTranslationX
                    val translateYStep = targetTranslationYAtTime - cumulativeTranslationY
                    cumulativeTranslationX = targetTranslationXAtTime
                    cumulativeTranslationY = targetTranslationYAtTime

                    moveX += translateXStep
                    moveY += translateYStep

                    invalidate()
                }

                flingAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        println("END")
                        if (!flingWasCancelled) {
                            end()
                        }
                        invalidate()
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        println("CANCEL")
                        flinging = false
                        flingWasCancelled = true
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationStart(animation: Animator?) {
                    }
                })
                flingAnimator.start()
                return true
            }

            return false;
        }

    }

    fun end() {
        inLongClickMode = false
        flinging = false
        bitmap = null
        val prevPath = path
        path = ""

        val bitmapTop = moveY-offsetY
        listener?.endMove(bitmapTop.toInt(), prevPath)
    }

    interface Listener {
        fun endMove(y: Int, path: String)
    }


}