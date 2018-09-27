package io.replicants.instaclone.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.annotation.Nullable
import io.replicants.instaclone.utilities.Easing
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.lang.Math.cos
import java.lang.Math.sin
import java.util.concurrent.atomic.AtomicBoolean


class ZoomRotateImageView : ImageView {

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
    private var snapBackAnimator: ValueAnimator = ValueAnimator()

    private var haveRecordedFocus = false
    private var initialFocusXReal: Float = 0f
    private var initialFocusYReal: Float = 0f

    private val CLICK_TIME_THRESHOLD = 200
    private val CLICK_DISTANCE_THRESHOLD = 200f

    var bitmap: Bitmap? = null

    //These constants specify the mode that we're in
    private var mode = 0
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private val matVal = FloatArray(9)
    private val offsets = FloatArray(4)

    private var dragged = false
    private var flinging = false
    private var pointers = 0

    private var originalWidth = 0
    private var originalHeight = 0

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private var startX = 0f
    private var startY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var previousRotationCentre = CoordinateHolder(0f, 0f)
    private var coordinateHolder = CoordinateHolder(0f, 0f)

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
    val inZoomRefractoryPeriod = AtomicBoolean(false)

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

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        originalHeight = bm?.height ?: 0
        originalWidth = bm?.width ?: 0
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

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

            if (oldScaleFactor != scaleFactor) {
                val scale = scaleFactor / oldScaleFactor
                imageMatrix = imageMatrix.apply {
                    postScale(scale, scale, initialFocusXReal, initialFocusYReal)
                }
            }

            oldScaleFactor = scaleFactor

            return true
        }
    }


    private inner class FlingListener : GestureDetector.SimpleOnGestureListener() {


        // constants
        private val SWIPE_MIN_DISTANCE = 120 * 120
        private val SWIPE_THRESHOLD_VELOCITY = 200 * 200
        private val duration = 250f
        private val speedBase = 0.6

        private var dx = 0f
        private var dy = 0f


        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

            if (Math.pow((e1!!.x - e2!!.x).toDouble(), 2.0) + Math.pow((e1.y - e2.y).toDouble(), 2.0) > SWIPE_MIN_DISTANCE &&
                    Math.pow(velocityX.toDouble(), 2.0) + Math.pow(velocityY.toDouble(), 2.0) > SWIPE_THRESHOLD_VELOCITY) {

                println("fling")
                flinging = true

                // calculate duration - exponential for now - constant time
                // ------

                // calculate dx and dy
                // v = ab^t, where say 0.05 = ab^ 2 - means v will become 5% in 2 seconds
                // total distance in x secs = ab^x/ln(b)
                dx = velocityX / 22f * Math.pow(speedBase, duration / 1000.0).toFloat() / -Math.log(speedBase).toFloat() * scaleFactor
                dy = velocityY / 22f * Math.pow(speedBase, duration / 1000.0).toFloat() / -Math.log(speedBase).toFloat() *scaleFactor


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
                    cumulativeTranslationY= targetTranslationYAtTime

                    imageMatrix = imageMatrix.apply {
                        postTranslate(translateXStep, translateYStep)
                    }

                    invalidate()
                }

                flingAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: Animator?) {
                        flinging = false
                        animateSnapBackIfNeeded()
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        flinging = false
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

    fun rotateImage(deg: Float) {
        rotate = deg

        imageMatrix = imageMatrix
                .apply {
                    postRotate(deg, viewWidth / 2f, viewHeight/ 2f)
                }


        invalidate()
    }

    private fun animateSnapBackIfNeeded() {

        getOffsets()

        var targetTranslationX = 0f
        var targetTranslationY = 0f
        var targetCorrectionZoom = 1f
        val duration = 500f

        if (scaleFactor < minZoom || scaleFactor > maxZoom) {
            targetCorrectionZoom = minZoom / scaleFactor
            // the target translation is figuring out the bounds after unshrinking, then translating accordingly
            val unzoomedLeftOffset = initialFocusXReal - (initialFocusXReal - offsets[0]) * targetCorrectionZoom
            val unzoomedTopOffset = initialFocusYReal - (initialFocusYReal - offsets[1]) * targetCorrectionZoom
//            val unzoomedRightOffset = initialFocusXReal + (viewWidth + offsets[2] - initialFocusXReal) * targetCorrectionZoom - viewWidth
//            val unzoomedBottomOffset = initialFocusYReal + (viewHeight + offsets[3] - initialFocusYReal) * targetCorrectionZoom - viewHeight

            targetTranslationX = -unzoomedLeftOffset
            targetTranslationY = -unzoomedTopOffset

        } else if (scaleFactor > maxZoom) {

            targetCorrectionZoom = maxZoom / scaleFactor

            val unzoomedLeftOffset = initialFocusXReal - (initialFocusXReal - offsets[0]) * targetCorrectionZoom
            val unzoomedTopOffset = initialFocusYReal - (initialFocusYReal - offsets[1]) * targetCorrectionZoom
            val unzoomedRightOffset = initialFocusXReal + (viewWidth + offsets[2] - initialFocusXReal) * targetCorrectionZoom - viewWidth
            val unzoomedBottomOffset = initialFocusYReal + (viewHeight + offsets[3] - initialFocusYReal) * targetCorrectionZoom - viewHeight

            if (unzoomedLeftOffset > 0) {
                targetTranslationX = -unzoomedLeftOffset
            }
            if (unzoomedTopOffset > 0) {
                targetTranslationY = -unzoomedTopOffset
            }
            if (unzoomedRightOffset < 0) {
                targetTranslationX = -unzoomedRightOffset
            }
            if (unzoomedBottomOffset < 0) {
                targetTranslationY = -unzoomedBottomOffset
            }

        } else {
            if (offsets[0] > 0) {
                targetTranslationX = -offsets[0]
            }
            if (offsets[1] > 0) {
                targetTranslationY = -offsets[1]
            }
            if (offsets[2] < 0) {
                targetTranslationX = -offsets[2]
            }
            if (offsets[3] < 0) {
                targetTranslationY = -offsets[3]
            }
        }

        var focusXSoFar = initialFocusXReal
        var focusYSoFar = initialFocusYReal
        var relativeZoomSoFar = 1f
        var cumulativeTranslateX = 0f
        var cumulativeTranslateY = 0f


        if (targetCorrectionZoom != 1f || targetTranslationX != 0f || targetTranslationY != 0f) {
            snapBackAnimator = ValueAnimator.ofFloat(duration)
            snapBackAnimator.duration = duration.toLong()
            snapBackAnimator.interpolator = LinearInterpolator()


            snapBackAnimator.addUpdateListener {
                val animatedValue = it.animatedValue as Float

                val zoomAtTime = (targetCorrectionZoom - 1f) * it.animatedFraction + 1f
                val zoomStep = zoomAtTime / relativeZoomSoFar
                relativeZoomSoFar = zoomAtTime
                val targetTranslationXAtTime = Easing.easeOutQuart(animatedValue, 0f, targetTranslationX, duration)
                val targetTranslationYAtTime = Easing.easeOutQuart(animatedValue, 0f, targetTranslationY, duration)
                val translateXStep = targetTranslationXAtTime - cumulativeTranslateX
                val translateYStep = targetTranslationYAtTime - cumulativeTranslateY
                cumulativeTranslateX = targetTranslationXAtTime
                cumulativeTranslateY = targetTranslationYAtTime

                oldScaleFactor = scaleFactor
                imageMatrix = imageMatrix.apply {
                    postScale(zoomStep, zoomStep, focusXSoFar, focusYSoFar)
                    postTranslate(translateXStep, translateYStep)
                }
                scaleFactor *= zoomStep
                invalidate()

                focusXSoFar += translateXStep
                focusYSoFar += translateYStep

            }

            snapBackAnimator.start()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {


        if (pointers < 2 && !inZoomRefractoryPeriod.get()) {
            flingDetector.onTouchEvent(event)
        }


        when (event.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                if (flinging) {
                    flingAnimator.cancel()
                    flingWasCancelled = true
                }
                snapBackAnimator.cancel()

                pointers = 1

                startX = event.x
                startY = event.y
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_MOVE -> {

                if (mode == DRAG) {

                    if(!inZoomRefractoryPeriod.get()) {
                        imageMatrix = imageMatrix.apply {
                            postTranslate(event.x - lastTouchX, event.y - lastTouchY)
                        }
                        println("move")
                    }


                    dragged = true
                }
                lastTouchX = event.x
                lastTouchY = event.y

            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                pointers++
            }

            MotionEvent.ACTION_UP -> {
                dragged = false
                pointers = 0

                previousTranslateX = translateX;
                previousTranslateY = translateY;

                if (!flinging) {
                    animateSnapBackIfNeeded()
                }


                // this variable prevents a click from registering if touch cancelled a fling
                flingWasCancelled = false

            }

            MotionEvent.ACTION_POINTER_UP -> {
                pointers--

                // leave as pointers == 1
                if (pointers == 1) {

                    inZoomRefractoryPeriod.set(true)
                    launch{
                        delay(300)
                        inZoomRefractoryPeriod.set(false)
                    }
                    // this is possible because can only zoom with 2 fingers
                    haveRecordedFocus = false
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

            }
        }

        when (pointers) {
            1 -> mode = DRAG
            2 -> {
                mode = ZOOM
                scaleDetector.onTouchEvent(event);
            }
            else -> {
            }
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

    private fun getOffsets() {
        imageMatrix.getValues(matVal)
        offsets[0] = matVal[Matrix.MTRANS_X]
        offsets[1] = matVal[Matrix.MTRANS_Y]
        offsets[2] = offsets[0] + originalWidth * scaleFactor - viewWidth
        offsets[3] = offsets[1] + originalHeight * scaleFactor - viewHeight
    }


    data class CoordinateHolder(var x: Float, var y: Float)

}