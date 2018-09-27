package io.replicants.instaclone.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import androidx.annotation.Nullable
import io.replicants.instaclone.utilities.Easing


class ZoomRotateImageView : View {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, @Nullable attrs: AttributeSet, intDefStyle: Int) : super(context, attrs, intDefStyle) {}


    var minZoom = 0.5f
        set(value) {
            scaleFactor = value
            oldScaleFactor = value
            field = value
        }
    var maxZoom = 5f
    private var scaleFactor = 1f
    private var oldScaleFactor = 1f
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    private val flingDetector = GestureDetector(context, FlingListener())
    private var flingAnimator: ValueAnimator = ValueAnimator()
    private var flingWasCancelled = false
    private var snapBackAnimator : ValueAnimator = ValueAnimator()

    private var haveRecordedFocus = false
    private var initialFocusXReal: Float = 0f
    private var initialFocusYReal: Float = 0f

    private val CLICK_TIME_THRESHOLD = 200
    private val CLICK_DISTANCE_THRESHOLD = 200f
    private var downTime = 0L

    var bitmap: Bitmap? = null

    //These constants specify the mode that we're in
    private var mode = 0
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2

    private var dragged = false
    private var flinging = false
    private var pointers = 0

    private var originalWidth = 0
    private var originalHeight = 0

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private var startX = 0f
    private var startY = 0f


    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private var translateX = 0f
    private var translateY = 0f

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private var previousTranslateX = 0f
    private var previousTranslateY = 0f


    var viewWidth: Int = 0
    var viewHeight: Int = 0
    var viewLeft = 0
    var viewTop = 0

    var rotate = 0f

    init {
        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@ZoomRotateImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                viewWidth = this@ZoomRotateImageView.measuredWidth
                viewHeight = this@ZoomRotateImageView.measuredHeight
                viewTop = this@ZoomRotateImageView.top
                viewLeft = this@ZoomRotateImageView.left

                invalidate()
            }
        })
    }

    fun setImageBitmap(bm: Bitmap?) {
        originalHeight = bm?.height ?: 0
        originalWidth = bm?.width ?: 0
        bitmap = bm
//
//        launch {
//            delay(1500)
//            translateX = -1000f/scaleFactor
//            invalidate()
//            delay(1500)
//
//            scaleFactor = 2f
//            invalidate()
//            delay(1000)
//            translateX = 0f
//            invalidate()
//            delay(500)
//            scaleFactor = minZoom
//            invalidate()
//
//        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.scale(scaleFactor, scaleFactor)
        canvas?.rotate(rotate,viewWidth.toFloat()/2f/scaleFactor- translateX,viewHeight.toFloat()/2f/scaleFactor- translateY)
        canvas?.drawBitmap(bitmap, translateX, translateY, null)
        println(rotate)


        oldScaleFactor = scaleFactor

    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {


        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            if (!haveRecordedFocus) {

                initialFocusXReal = detector?.focusX!!
                initialFocusYReal = detector.focusY
                haveRecordedFocus = true
            }


            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(minZoom, Math.min(scaleFactor, maxZoom))
            if (scaleFactor != oldScaleFactor) {
                translateX = getNewOffsetLeft(initialFocusXReal)
                translateY = getNewOffsetTop(initialFocusYReal)
            }

            return true
        }
    }


    private inner class FlingListener : GestureDetector.SimpleOnGestureListener() {


        // constants
        private val SWIPE_MIN_DISTANCE = 120 * 120
        private val SWIPE_THRESHOLD_VELOCITY = 200 * 200
        private val duration = 1000f
        private val speedBase = 0.6

        private var elapsedTime = 0f
        private var originalX = 0f
        private var originalY = 0f
        private var dx = 0f
        private var dy = 0f


        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

            if (Math.pow((e1!!.x - e2!!.x).toDouble(), 2.0) + Math.pow((e1.y - e2.y).toDouble(), 2.0) > SWIPE_MIN_DISTANCE &&
                    Math.pow(velocityX.toDouble(), 2.0) + Math.pow(velocityY.toDouble(), 2.0) > SWIPE_THRESHOLD_VELOCITY) {

                flinging = true
                elapsedTime = 0f

                // calculate duration - exponential for now - constant time
                // ------

                // calculate dx and dy
                // v = ab^t, where say 0.05 = ab^ 2 - means v will become 5% in 2 seconds
                // total distance in x secs = ab^x/ln(b)
                dx = velocityX / 15f * Math.pow(speedBase, duration / 1000.0).toFloat() / -Math.log(speedBase).toFloat() / scaleFactor
                dy = velocityY / 15f * Math.pow(speedBase, duration / 1000.0).toFloat() / -Math.log(speedBase).toFloat() / scaleFactor

//                dx = velocityX* duration/1000f / scaleFactor
//                dy = velocityY * duration/1000f / scaleFactor

                // fix the original coordinates
                originalX = translateX
                originalY = translateY

                // calculate if it would end with a translation where an edge is inside the view
                // if it does, calculate the desired position
                val targetX = originalX + dx
                val targetY = originalY + dy

                var clampedX = targetX
                var clampedY = targetY


                val mostAllowableScaledTranslateX = viewWidth - originalWidth * scaleFactor
                val mostAllowableActualTranslateX = mostAllowableScaledTranslateX / scaleFactor
                if (targetX > 0) {
                    clampedX = 0f
                } else if (targetX < mostAllowableActualTranslateX) {
                    clampedX = mostAllowableActualTranslateX
                }

                val mostAllowableScaledTranslateY = viewHeight - originalHeight * scaleFactor
                val mostAllowableActualTranslateY = mostAllowableScaledTranslateY / scaleFactor
                if (targetY > 0) {
                    clampedY = 0f
                } else if (targetY < mostAllowableActualTranslateY) {
                    clampedY = mostAllowableActualTranslateY
                }

                var mustChange = clampedY != targetY || clampedX != targetX
                println("$dx, $dy")
                println("$originalX, $originalY")
                println("$targetX, $targetY")
                println("$clampedX, $clampedY")
                println(mustChange)
                var hasChanged = false

                val boundsExceeded = fun(): Boolean {
                    return translateX > 0 || translateX < mostAllowableActualTranslateX || translateY > 0 || translateY < mostAllowableActualTranslateY
                }

                var changePointX = 0f
                var changePointY = 0f
                var changeTime = 0f
                var remainingDurationAfterChange = 0f
                var dxToClampX = 0f
                var dyToClampY = 0f

                flingAnimator = ValueAnimator.ofFloat(duration)
                flingAnimator.duration = duration.toLong()
                flingAnimator.interpolator = LinearInterpolator()

                // at 1s, or after it goes past bounds, whichever comes later, change target to clampedTarget
                flingAnimator.addUpdateListener {
                    val animatedValue = it.animatedValue as Float

//                    if(mustChange){
//                        translateX = Easing.easeOutBack(animatedValue, originalX, clampedX-originalX, duration)
//                        translateY = Easing.easeOutBack(animatedValue, originalY, clampedY-originalY, duration)
//                    } else {
//                        translateX = Easing.easeOutQuart(animatedValue, originalX, -dx, duration)
//                        translateY = Easing.easeOutQuart(animatedValue, originalY, -dy, duration)
//                    }
                    if (mustChange && !hasChanged && animatedValue > duration / 2f && boundsExceeded()) {
                        hasChanged = true
                        changePointX = translateX
                        changePointY = translateY
                        if(clampedX!=targetX){

                        }
                        dxToClampX = clampedX - translateX
                        dyToClampY = clampedY - translateY
                        changeTime = animatedValue
                        remainingDurationAfterChange = duration - animatedValue
                    }

                    if (hasChanged) {
                        translateX = Easing.easeOutQuart(animatedValue - changeTime, changePointX, dxToClampX, remainingDurationAfterChange)
                        translateY = Easing.easeOutQuart(animatedValue - changeTime, changePointY, dyToClampY, remainingDurationAfterChange)
                    } else {
                        translateX = Easing.easeOutQuart(animatedValue, originalX, dx, duration)
                        translateY = Easing.easeOutQuart(animatedValue, originalY, dy, duration)
                    }

                    invalidate()
                }

                flingAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        flinging = false
                        previousTranslateX = translateX
                        previousTranslateY = translateY
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        flinging = false
                        previousTranslateX = translateX
                        previousTranslateY = translateY
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

    fun rotateImage(deg: Float){
        rotate = deg

        invalidate()
    }

    val animateSnapBackIfNeeded = fun() {

        var clampedX = translateX
        var clampedY = translateY
        val originalX = translateX
        val originalY = translateY

        val mostAllowableScaledTranslateX = viewWidth - originalWidth * scaleFactor
        val mostAllowableActualTranslateX = mostAllowableScaledTranslateX / scaleFactor
        if (translateX > 0) {
            clampedX = 0f
        } else if (translateX < mostAllowableActualTranslateX) {
            clampedX = mostAllowableActualTranslateX
        }

        val mostAllowableScaledTranslateY = viewHeight - originalHeight * scaleFactor
        val mostAllowableActualTranslateY = mostAllowableScaledTranslateY / scaleFactor
        if (translateY > 0) {
            clampedY = 0f
        } else if (translateY < mostAllowableActualTranslateY) {
            clampedY = mostAllowableActualTranslateY
        }

        val dx = clampedX - translateX
        val dy = clampedY - translateY

        if(clampedX!=translateX || clampedY!=translateY) {
            snapBackAnimator = ValueAnimator.ofFloat(500f)
            snapBackAnimator.duration = 500L
            snapBackAnimator.interpolator = LinearInterpolator()


            snapBackAnimator.addUpdateListener {
                val animatedValue = it.animatedValue as Float

                translateX = Easing.easeOutQuart(animatedValue, originalX, dx, 500f)
                translateY = Easing.easeOutQuart(animatedValue, originalY, dy, 500f)

                invalidate()
            }

            snapBackAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    previousTranslateX = translateX
                    previousTranslateY = translateY
                }

                override fun onAnimationCancel(animation: Animator?) {
                    previousTranslateX = translateX
                    previousTranslateY = translateY
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })
            snapBackAnimator.start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {


        flingDetector.onTouchEvent(event)


        when (event.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                if (flinging) {
                    flingAnimator.cancel()
                    flingWasCancelled = true
                }

                downTime = System.currentTimeMillis()
                pointers = 1

                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_MOVE -> {

                if (mode == DRAG) {
                    translateX = (event.x - startX) / scaleFactor + previousTranslateX
                    translateY = (event.y - startY) / scaleFactor + previousTranslateY

                    dragged = true
                }

            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                pointers++
            }

            MotionEvent.ACTION_UP -> {
                dragged = false
                pointers = 0

                previousTranslateX = translateX;
                previousTranslateY = translateY;

                if(!flinging) {
                    animateSnapBackIfNeeded()
                }


                // this variable prevents a click from registering if touch cancelled a fling
                flingWasCancelled = false

            }

            MotionEvent.ACTION_POINTER_UP -> {
                pointers--

                // this is possible because can only zoom with 2 fingers
                haveRecordedFocus = false

                // leave as pointers == 1
                if (pointers == 1) {
                    // get index of remaining finger and set coordinates
                    // if just use getX/getY, may default to first finger down, even if that finger is gone

                    val releasedFinger = event.actionIndex
                    for (i in 0 until event.pointerCount) {
                        if (i != releasedFinger) {
                            startX = event.getX(i)
                            startY = event.getY(i)
                        }
                    }
                }

                // this is necessary so can transition smoothly from zoom to drag
                previousTranslateX = translateX;
                previousTranslateY = translateY;
            }
        }

        when (pointers) {
            1 -> mode = DRAG
            2 -> {
                mode = ZOOM
                scaleDetector.onTouchEvent(event);
            }
            else -> NONE
        }


        //We redraw the canvas only in the following cases:
        //
        // o The mode is ZOOM
        //        OR
        // o The mode is DRAG and the scale factor is not equal to 1 (meaning we have zoomed) and dragged is
        //   set to true (meaning the finger has actually moved)
        if ((mode == DRAG && dragged) || mode == ZOOM) {
            invalidate();
        }

        return true;
    }

    // base distance (non scaled) from focus point should remain the same
    // so first reverse previous operation to retrieve base distance
    // calculate new scaled distance and offset required
    // figure out scaled translation required to achieve offset
    private fun getNewOffsetLeft(focusX: Float): Float {
        val oldScaledDistance = focusX - translateX * oldScaleFactor
        val baseDistance = oldScaledDistance / oldScaleFactor
        val newScaledDistance = baseDistance * scaleFactor
        val offsetRequired = focusX - newScaledDistance
        val scaledTranslationRequired = offsetRequired / scaleFactor

//        val total = focusX/scaleFactor - (focusX-translateX)/ oldScaleFactor

//        return focusX - (focusX - offsetLeft) * scaleFactor / oldScaleFactor
        return scaledTranslationRequired
    }

    private fun getNewOffsetTop(focusY: Float): Float {
        val oldScaledDistance = focusY - translateY * oldScaleFactor
        val baseDistance = oldScaledDistance / oldScaleFactor
        val newDistance = baseDistance * scaleFactor
        val offsetRequired = focusY - newDistance
        val scaledTranslationRequired = offsetRequired / scaleFactor

//        val total = focusY/scaleFactor - (focusY-translateY)/ oldScaleFactor

//        return focusX - (focusX - offsetLeft) * scaleFactor / oldScaleFactor
        return scaledTranslationRequired
    }


}