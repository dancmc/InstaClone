package io.replicants.instaclone.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.annotation.Nullable
import io.replicants.instaclone.utilities.Easing
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 2018 Daniel Chan
 */

class ZoomRotateImageView : ImageView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, @Nullable attrs: AttributeSet, intDefStyle: Int) : super(context, attrs, intDefStyle) {}

    @JvmField
    var minZoom = 0.5f
    var maxZoom = 6f
    var scaleFactor = 1f
    private var oldScaleFactor = 1f
    var extraScaleForRotate = 1f
    var rotate = 0f
    var canTouch = false

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    private val flingDetector = GestureDetector(context, FlingListener())
    private var flingAnimator: ValueAnimator = ValueAnimator()
    private var flingWasCancelled = false
    private var snapBackAnimator: ValueAnimator = ValueAnimator()

    private var haveRecordedFocus = false
    private var initialFocusXReal: Float = 0f
    private var initialFocusYReal: Float = 0f

    //These constants specify the mode that we're in
    private var mode = 0
    private val DRAG = 1
    private val ZOOM = 2
    private val matVal = FloatArray(9)
//    private val offsets = FloatArray(4)

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

    var viewWidth: Int = 0
    var viewHeight: Int = 0

    val inZoomRefractoryPeriod = AtomicBoolean(false)

    init {
        this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                this@ZoomRotateImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                viewWidth = this@ZoomRotateImageView.measuredWidth
                viewHeight = this@ZoomRotateImageView.measuredHeight

                vp1.x = viewWidth.toFloat()
                vp2.x = viewWidth.toFloat()
                vp2.y = viewHeight.toFloat()
                vp3.y = viewHeight.toFloat()

                invalidate()
            }
        })
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        originalHeight = bm?.height ?: 0
        originalWidth = bm?.width ?: 0
    }


    fun setMinZoom(min: Float) {
        scaleFactor = min
        oldScaleFactor = min
        minZoom = min
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
//            scaleFactor = Math.max(minZoom, Math.min(scaleFactor, maxZoom))

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

                flinging = true

                // calculate duration - exponential for now - constant time
                // ------

                // calculate dx and dy
                // v = ab^t, where say 0.05 = ab^ 2 - means v will become 5% in 2 seconds
                // total distance in x secs = ab^x/ln(b)
                dx = velocityX / 22f * Math.pow(speedBase, duration / 1000.0).toFloat() / -Math.log(speedBase).toFloat() * scaleFactor
                dy = velocityY / 22f * Math.pow(speedBase, duration / 1000.0).toFloat() / -Math.log(speedBase).toFloat() * scaleFactor


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

        // TODO consider implementing doubletap to unzoom to minzoom
    }



    fun rotateImage(deg: Float) {

        val forPostRotate = deg - rotate
        rotate = deg

        //

        // formula to scale past bounding box
        // https://math.stackexchange.com/questions/438567/whats-the-formula-for-the-amount-to-scale-up-an-image-during-rotation-to-not-see

        val newDeg = when (rotate) {
            in 0f..90f -> rotate
            in 90f..180f -> 180f - rotate
            in 180f..270f -> rotate - 180f
            in 270f..360f -> 360f - rotate
            else -> rotate
        }
        val rad = Math.toRadians(newDeg.toDouble())
        var hwRatio = viewHeight / viewWidth.toDouble()
        if (hwRatio < 1) {
            hwRatio = 1 / hwRatio
        }
        val requiredExtraScale = (Math.cos(rad) + hwRatio * Math.sin(rad)).toFloat()
        val forPostScale = requiredExtraScale / extraScaleForRotate
        extraScaleForRotate = requiredExtraScale
        minZoom *= forPostScale
        oldScaleFactor *= forPostScale
        scaleFactor *= forPostScale

        imageMatrix = imageMatrix.apply {
            postRotate(forPostRotate, viewWidth / 2f, viewHeight / 2f)
            postScale(forPostScale, forPostScale, viewWidth / 2f, viewHeight / 2f)
        }

        val a = FloatArray(2)
        imageMatrix.mapPoints(a)

        invalidate()
    }


    fun getScaleFromMatrix(matrix: Matrix): Float {
        matrix.getValues(matVal)

        // calculate real scale
        val scalex = matVal[Matrix.MSCALE_X]
        val skewy = matVal[Matrix.MSKEW_Y]
        return Math.sqrt((scalex * scalex + skewy * skewy).toDouble()).toFloat()
    }

    // https://judepereira.com/blog/calculate-the-real-scale-factor-and-the-angle-of-rotation-from-an-android-matrix/
    fun getRotationFromMatrix(matrix: Matrix): Float {
        matrix.getValues(matVal)
        var rotation = (Math.atan2(matVal[Matrix.MSKEW_X].toDouble(), matVal[Matrix.MSCALE_X].toDouble()) * (180 / Math.PI)).toFloat()
        if(rotation<0f){
            rotation += 360f
        }
        // calculate the degree of rotation
        return rotation
    }

    fun rotateFinished() {
        animateSnapBackIfNeeded()
    }

    private fun animateSnapBackIfNeeded() {

        snapBackAnimator.cancel()

//        getOffsets()
        var targetTranslationX = 0f
        var targetTranslationY = 0f
        var targetCorrectionZoom = 1f
        val duration = 500f

        if (scaleFactor < minZoom) {
            targetCorrectionZoom = minZoom / scaleFactor
        } else if (scaleFactor > maxZoom) {
            targetCorrectionZoom = maxZoom / scaleFactor
        }

        if (rotate % 90 == 0f) {

            // populate the projected image corner coordinates
            populateFutureImagePoints(targetCorrectionZoom)

            // figure out the height and width of testMatrix
            val targetImageHeight = getImageHeight()
            val targetImageWidth = getImageWidth()

            when (rotate) {
                0f, 360f -> {
                    if(ip0.x > 0)targetTranslationX = -ip0.x
                    if(ip0.x < vp1.x - targetImageWidth) targetTranslationX = vp1.x - (ip0.x + targetImageWidth)
                    if(ip0.y > 0) targetTranslationY = -ip0.y
                    if(ip0.y < vp3.y - targetImageHeight) targetTranslationY = vp3.y - (ip0.y + targetImageHeight)
                }
                90f -> {
                    if(ip0.x < vp1.x ) targetTranslationX = vp1.x - ip0.x
                    if(ip0.x > targetImageHeight ) targetTranslationX = targetImageHeight - ip0.x
                    if(ip0.y > 0 ) targetTranslationY = -ip0.y
                    if(ip0.y < vp2.y - targetImageWidth ) targetTranslationY = vp2.y - (ip0.y + targetImageWidth)
                }
                180f -> {
                    if(ip0.x < vp2.x ) targetTranslationX = vp2.x - ip0.x
                    if(ip0.x > targetImageWidth ) targetTranslationX = targetImageWidth - ip0.x
                    if(ip0.y < vp2.y ) targetTranslationY = vp2.y - ip0.y
                    if(ip0.y > targetImageHeight ) targetTranslationY = targetImageHeight - ip0.y
                }
                270f -> {
                    if(ip0.x > 0 ) targetTranslationX = -ip0.x
                    if(ip0.x < vp2.x - targetImageHeight ) targetTranslationX = vp2.x - (targetImageHeight + ip0.x)
                    if(ip0.y < vp3.y ) targetTranslationY = vp3.y - ip0.y
                    if(ip0.y > targetImageWidth ) targetTranslationY = targetImageWidth - ip0.y
                }
            }


        } else {
            val orphanedViewCorners = getRealOrphanedViewPoints(targetCorrectionZoom)
            val c = getTranslationFromOrphanedViewPoints(orphanedViewCorners, targetCorrectionZoom)
            targetTranslationX = c.x
            targetTranslationY = c.y
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

        if (!canTouch) return true

        if (pointers < 2 && !inZoomRefractoryPeriod.get()) {
            flingDetector.onTouchEvent(event)
        }


        when (event.actionMasked) {

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

                    if (!inZoomRefractoryPeriod.get()) {
                        imageMatrix = imageMatrix.apply {
                            postTranslate(event.x - lastTouchX, event.y - lastTouchY)
                        }
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
                    launch {
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
            invalidate()
        }

        return true
    }



    data class CoordinateHolder(var x: Float, var y: Float, var tag: Int = 0)

    // ip0-4 are corners of image from origin clockwise
    var ip0 = CoordinateHolder(0f, 0f, 0)
    var ip1 = CoordinateHolder(0f, 0f, 1)
    var ip2 = CoordinateHolder(0f, 0f, 2)
    var ip3 = CoordinateHolder(0f, 0f, 3)
    val ipArray = FloatArray(8)
    val drawableRect = Rect()

    var vp0 = CoordinateHolder(0f, 0f)
    var vp1 = CoordinateHolder(0f, 0f)
    var vp2 = CoordinateHolder(0f, 0f)
    var vp3 = CoordinateHolder(0f, 0f)
    val vpArray = arrayListOf(vp0, vp1, vp2, vp3)
    val vpFloatArray = FloatArray(2)

    var pointsList = ArrayList<CoordinateHolder>()
    var testPointsList = ArrayList<CoordinateHolder>()
    var pointArray = Array(2) { CoordinateHolder(0f, 0f) }
    var testMatrix = Matrix()
    var tmpMatrix = Matrix()


    enum class Side {
        TOP, LEFT, RIGHT, BOTTOM
    }

    // enhanced version of static method in Utils to avoid allocating new arrays all the time
    fun populateFutureImagePoints(zoomFix: Float) {
        ipArray[0] = 0f
        ipArray[1] = 0f
        ipArray[2] = originalWidth.toFloat()
        ipArray[3] = 0f
        ipArray[4] = originalWidth.toFloat()
        ipArray[5] = originalHeight.toFloat()
        ipArray[6] = 0f
        ipArray[7] = originalHeight.toFloat()

        testMatrix.set(imageMatrix)
        testMatrix.apply {
            postScale(zoomFix, zoomFix, initialFocusXReal, initialFocusYReal)
        }
        testMatrix.mapPoints(ipArray)
        ip0.x = ipArray[0]
        ip0.y = ipArray[1]
        ip1.x = ipArray[2]
        ip1.y = ipArray[3]
        ip2.x = ipArray[4]
        ip2.y = ipArray[5]
        ip3.x = ipArray[6]
        ip3.y = ipArray[7]

    }



    // populateFutureImagePoints should be called first
    fun getImageWidth(): Float {
        return when (rotate) {
            0f, 360f -> ip1.x - ip0.x
            90f -> ip1.y - ip0.y
            180f -> ip0.x - ip1.x
            270f -> ip0.y - ip1.y
            else -> originalWidth * scaleFactor
        }
    }

    fun getImageHeight(): Float {
        return when (rotate) {
            0f, 360f -> ip2.y - ip1.y
            90f -> ip1.x - ip2.x
            180f -> ip1.y - ip2.y
            270f -> ip2.x - ip1.x
            else -> originalHeight * scaleFactor
        }
    }

    fun getRealOrphanedViewPoints(zoomFix: Float): ArrayList<CoordinateHolder> {
        testMatrix.set(imageMatrix)
        testMatrix.apply {
            postScale(zoomFix, zoomFix, initialFocusXReal, initialFocusYReal)
        }
        return getOrphanedViewPoints(testMatrix, pointsList)
    }

    // this is to run simulations on the final chosen translation and ensure no points are left out
    fun getHypotheticalOrphanedViewPoints(transX: Float, transY: Float, zoomFix: Float): ArrayList<CoordinateHolder> {
        testMatrix.set(imageMatrix)
        testMatrix.apply {
            postScale(zoomFix, zoomFix, initialFocusXReal, initialFocusYReal)
            postTranslate(transX, transY)
        }
        return getOrphanedViewPoints(testMatrix, testPointsList)
    }

    // should not be called directly
    fun getOrphanedViewPoints(imMatrix: Matrix, resultsHolder: ArrayList<CoordinateHolder>): ArrayList<CoordinateHolder> {

        tmpMatrix.reset()
        imMatrix.invert(tmpMatrix)

        resultsHolder.clear()
        vpArray.forEach {
            vpFloatArray[0] = it.x
            vpFloatArray[1] = it.y
            tmpMatrix.mapPoints(vpFloatArray)
            drawable.copyBounds(drawableRect)
            drawableRect.right += 2
            drawableRect.bottom += 2
            drawableRect.top=-1
            drawableRect.left=-1
            if (!drawableRect.contains(vpFloatArray[0].toInt(), vpFloatArray[1].toInt())) {
                resultsHolder.add(it)
            }
        }
        return resultsHolder
    }


    fun getTranslationFromOrphanedViewPoints(points: ArrayList<CoordinateHolder>, zoomFix: Float): CoordinateHolder {

        populateFutureImagePoints(zoomFix)
        points.sortBy { it.tag }

        return when (points.size) {
            0 -> CoordinateHolder(0f, 0f)
            1 -> {
                val side = when (points[0]) {
                    vp0 -> Side.TOP
                    vp1 -> Side.RIGHT
                    vp2 -> Side.BOTTOM
                    else -> Side.LEFT
                }
                val targetPoint = getTrianglePeakCoordinates(side)
                val imagePoint = getImagePointForViewSide(side)!!
                CoordinateHolder(targetPoint.x - imagePoint.x, targetPoint.y - imagePoint.y)

            }
            else -> {
                // for each combination of points, determine the view side
                // then calculate the triangle's peak
                // then retrieve the image point corresponding to view side
                // then calculate translation and distance & return the smallest distance
                val resultTranslate = CoordinateHolder(0f, 0f)
                var resultDistance = Double.MAX_VALUE

                while (points.size > 1) {
                    val p = points.removeAt(points.lastIndex)
                    points.forEach {
                        pointArray[0] = p
                        pointArray[1] = it
                        val side = getSideFromViewPoints(pointArray) ?: return@forEach
                        val targetPoint = getTrianglePeakCoordinates(side)
                        val imagePoint = getImagePointForViewSide(side)!!
                        val transX = targetPoint.x - imagePoint.x
                        val transY = targetPoint.y - imagePoint.y
                        val distance = Math.sqrt(Math.pow(transX.toDouble(), 2.0) + Math.pow(transY.toDouble(), 2.0))
                        if (distance < resultDistance) {
                            val postTranslateOrphans = getHypotheticalOrphanedViewPoints(transX, transY, zoomFix)
                            if (postTranslateOrphans.size == 0) {
                                resultTranslate.x = transX
                                resultTranslate.y = transY
                                resultDistance = distance
                            }
                        }
                    }
                }
                resultTranslate
            }
        }
    }

    fun getTrianglePeakCoordinates(viewSide: Side): CoordinateHolder {

        val rot = Math.toRadians(rotate.toDouble() % 90)

        val knownSideLength = when (viewSide) {
            Side.TOP, Side.BOTTOM -> viewWidth
            Side.LEFT, Side.RIGHT -> viewHeight
        }
        val alternateHypotenuseLength = Math.sin(rot) * knownSideLength
        val normalLength = (Math.cos(rot) * alternateHypotenuseLength).toFloat()
        val parallelLength = (Math.sin(rot) * alternateHypotenuseLength).toFloat()

        return when (viewSide) {
            Side.TOP -> CoordinateHolder(vp0.x + parallelLength, vp0.y - normalLength)
            Side.RIGHT -> CoordinateHolder(vp1.x + normalLength, vp1.y + parallelLength)
            Side.BOTTOM -> CoordinateHolder(vp2.x - parallelLength, vp2.y + normalLength)
            Side.LEFT -> CoordinateHolder(vp3.x - normalLength, vp3.y - parallelLength)
        }
    }

    fun getSideFromViewPoints(points: Array<CoordinateHolder>): Side? {
        val p1 = points[0]
        val p2 = points[1]
        return when {
            p1.x != p2.x && p1.y == p2.y && p1.y == 0f -> Side.TOP
            p1.x == p2.x && p1.y != p2.y && p1.x == 0f -> Side.LEFT
            p1.x != p2.x && p1.y == p2.y && p1.y != 0f -> Side.BOTTOM
            p1.x == p2.x && p1.y != p2.y && p1.x != 0f -> Side.RIGHT
            else -> null
        }
    }

    fun getImagePointForViewSide(side: Side): CoordinateHolder? {


        rotate.let {
            return when (side) {

                Side.TOP -> {
                    when {
                        it > 0f && it < 90f -> ip0
                        it > 90f && it < 180f -> ip3
                        it > 180f && it < 270f -> ip2
                        it > 270f && it < 360f -> ip1
                        else -> null
                    }
                }
                Side.BOTTOM -> {
                    when {
                        it > 0f && it < 90f -> ip2
                        it > 90f && it < 180f -> ip1
                        it > 180f && it < 270f -> ip0
                        it > 270f && it < 360f -> ip3
                        else -> null
                    }
                }
                Side.LEFT -> {
                    when {
                        it > 0f && it < 90f -> ip3
                        it > 90f && it < 180f -> ip2
                        it > 180f && it < 270f -> ip1
                        it > 270f && it < 360f -> ip0
                        else -> null
                    }
                }
                Side.RIGHT -> {
                    when {
                        it > 0f && it < 90f -> ip1
                        it > 90f && it < 180f -> ip0
                        it > 180f && it < 270f -> ip3
                        it > 270f && it < 360f -> ip2
                        else -> null
                    }
                }
            }
        }
    }

    fun saveState():ZoomRotateImageViewState{
        return ZoomRotateImageViewState().apply {
            this.minZoom = this@ZoomRotateImageView.minZoom
            this.maxZoom = this@ZoomRotateImageView.maxZoom
            this.scaleFactor = this@ZoomRotateImageView.scaleFactor
            this.oldScaleFactor = this@ZoomRotateImageView.oldScaleFactor
            this.extraScaleForRotate = this@ZoomRotateImageView.extraScaleForRotate
            this.rotate = this@ZoomRotateImageView.rotate
            this.viewHeight = this@ZoomRotateImageView.viewHeight
            this.viewWidth = this@ZoomRotateImageView.viewWidth
        }
    }

    fun restoreState(state:ZoomRotateImageViewState?){
        state?.let {
            minZoom = it.minZoom
            maxZoom = it.maxZoom
            scaleFactor = it.scaleFactor
            oldScaleFactor = it.oldScaleFactor
            extraScaleForRotate = it.extraScaleForRotate
            rotate = it.rotate
        }
    }


}