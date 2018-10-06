package io.replicants.instaclone.subfragments.upload.edit

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.EditPhotoState
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.views.ZoomRotateImageView
import io.replicants.instaclone.views.ZoomRotateImageViewState
import kotlinx.android.synthetic.main.subfragment_edit_photo.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.windowManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditPhotoSubFragment : BaseSubFragment(),
        AdjustSubFragment.ImageAdjust,
        EditControlsSubFragment.OnChooseListener,
        BrightnessSubFragment.ImageBrightness,
        ContrastSubFragment.ImageContrast,
        FilterSubFragment.ImageFilter{

    companion object {

        @JvmField
        val TAG_ADJUST = "Adjust"
        @JvmField
        val TAG_BRIGHTNESS = "Brightness"
        @JvmField
        val TAG_CONTRAST = "Contrast"
        @JvmField
        val TAG_FILTER = "Filter"

        @JvmStatic
        fun newInstance(photoID: String, tempFileName: String): EditPhotoSubFragment {
            val myFragment = EditPhotoSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("photoID", photoID)
            args.putString("fileName", tempFileName)

            return myFragment
        }
    }

    lateinit var layout: View
    lateinit var imageView: ZoomRotateImageView
    val realm = Realm.getDefaultInstance()
    var filepath = ""
    var photoID = ""
    lateinit var saveDialog : AlertDialog

    var currentRotation = 0f
    var savedCurrentRotation = 0f
    var baseRotation = 0f
    var savedBaseRotation = 0f
    val savedMatrix = Matrix()
    val colorMatrix = ColorMatrix()
    val savedColorMatrix = ColorMatrix()
    var colorFilter = ColorMatrixColorFilter(colorMatrix)
    var brightness = 0
    var savedBrightness = 0
    var contrast = 0
    var savedContrast = 0
    var saturation = 0
    var savedSaturation = 0
    var filter = FilterSubFragment.ORIGINAL
    var savedFilter = FilterSubFragment.ORIGINAL
    lateinit var savedState: ZoomRotateImageViewState

    var listener: PhotoEditListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if(this::layout.isInitialized){
            return layout
        }

        layout = inflater.inflate(R.layout.subfragment_edit_photo, container, false)

        filepath = arguments?.getString("fileName") ?: ""
        photoID = arguments?.getString("photoID") ?: ""

        context?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.save_draft_title)
                    .setMessage(R.string.save_draft_text)
            builder.apply {
                setNegativeButton(R.string.discard){ dialog, id ->
                    listener?.editCancelled()
                }
                setPositiveButton(R.string.save_draft) { dialog, id ->
                    realm.beginTransaction()
                    val savedPhoto = realm.where(SavedPhoto::class.java).equalTo("photoID", photoID).findFirst() ?:
                    SavedPhoto()
                    savedPhoto.photoID = photoID
                    savedPhoto.temp = false
                    savedPhoto.photoFile = filepath
                    savedPhoto.editPhotoState = saveState()
                    savedPhoto.imageViewState = imageView.saveState()
                    realm.copyToRealmOrUpdate(savedPhoto)
                    realm.commitTransaction()
                    listener?.editCancelled()
                }
            }
            saveDialog = builder.create()
        }


        //create the overall bitmap
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filepath, options)
        val rawWidth = options.outWidth
        val rawHeight = options.outHeight
        val rawShortestSide = Math.min(options.outHeight, options.outWidth)

        options = BitmapFactory.Options()

        // somewhat arbitrary - but insamplesize only takes powers of 2 so just have to choose a number
        val sampleSize = if (rawShortestSide <= 1200) 1 else rawShortestSide / 1200 + 1
        options.inSampleSize = sampleSize
        val bitmap = BitmapFactory.decodeFile(filepath, options)
        val sampleImageWidth = options.outWidth
        val sampleImageHeight = options.outHeight

        val display = context!!.windowManager.defaultDisplay
        val displayDimen = Point()
        display.getSize(displayDimen)
        val displayWidth = displayDimen.x

        imageView = ZoomRotateImageView(context!!)
        imageView.backgroundColor = Color.rgb(255, 255, 255)

        imageView.scaleType = ImageView.ScaleType.MATRIX
        val scale: Float
        if (sampleImageHeight > sampleImageWidth) {
            scale = displayWidth / sampleImageHeight.toFloat()
            imageView.layoutParams = FrameLayout.LayoutParams(Math.min(Math.ceil(sampleImageWidth * scale.toDouble()).toInt(), displayWidth),
                    FrameLayout.LayoutParams.MATCH_PARENT)
        } else if (sampleImageHeight < sampleImageWidth) {
            scale = displayWidth / sampleImageWidth.toFloat()
            imageView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    Math.min(Math.ceil(sampleImageHeight * scale.toDouble()).toInt(), displayWidth))
        } else {
            scale = displayWidth / sampleImageWidth.toFloat()
            imageView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        }
        layout.subfragment_filter_preview.addView(imageView)
        imageView.setImageBitmap(bitmap)

        // set variables depending on whether loading draft or new photo
        val draft = realm.where(SavedPhoto::class.java).equalTo("photoID", photoID).findFirst()
        if (draft == null) {
            savedMatrix.postScale(scale, scale)
            imageView.setMinZoom(scale)
        } else {
            filepath = draft.photoFile
            restoreState(draft.editPhotoState)
            imageView.restoreState(draft.imageViewState)
        }

        imageView.imageMatrix = savedMatrix
        imageView.colorFilter = colorFilter
        savedState = imageView.saveState()


        // Setup the edit controls
        val tx = childFragmentManager.beginTransaction()
        val frag = EditControlsSubFragment.newInstance()
        frag.onChooseListener = this
        tx.add(R.id.subfragment_filter_controls, frag)
        tx.commit()


        // Setup the toolbar
        layout.subfragment_edit_photo_toolbar_back.onClick {
            saveDialog.show()
        }

        layout.subfragment_edit_photo_toolbar_next.onClick {
            val photoFolder = File(context!!.filesDir, "posting")
            if (!photoFolder.exists()) {
                photoFolder.mkdir()
            }
            val file = File(photoFolder, "$photoID.jpg")

            // we need to scale by the original sample scale
            val finalMatrix = Matrix(imageView.imageMatrix)

            // ideally would use third party native library/renderscript in real app
            // here we just constrain longest side to 2160 and hope for no OOM
            val rawLongestSide = Math.max(rawHeight, rawWidth)
            val finalSampleSize = if (rawLongestSide <= 2160) 1 else rawLongestSide / 2160 + 1
            val newOptions = BitmapFactory.Options()
            newOptions.inSampleSize = finalSampleSize
            val finalBitmapRaw = BitmapFactory.decodeFile(filepath, newOptions)
            val finalBitmapWidth = newOptions.outWidth
            val finalBitmapHeight = newOptions.outHeight

            // the insample inverse size makes it confusing
            val ratioToSample = (1 / sampleSize.toFloat()) / (1 / finalSampleSize.toFloat())

            // prescale our final matrix by the difference in previous *sample* size and current *sample* size to create an exact replica of the imageview pixel representation
            finalMatrix.preScale(ratioToSample, ratioToSample)


            // the viewport/matrix original top left is 0,0 invariant
            // find all the image's current corner coordinates wrt to that 0,0 point (eg the matrix translate is the translation of the image's top left corner)
            // we can find the corner coordinates by mapping the image's original coordinates using current matrix
            // !!!!!! remember finalMatrix and imageview matrix are NOT the same matrix, though result is same image
            // if use finalMatrix must pass in the -finalImageHeight- and -finalImageWidth- to map points correctly
            val mappedPoints = Utils.getMappedImagePoints(imageView.imageMatrix, sampleImageHeight, sampleImageWidth)

            // we find the bitmap's top left coordinates wrt viewport 0,0 by taking the smallest X and smallest Y (will likely be negative wrt viewport's top left)
            var smallestX = Float.MAX_VALUE
            var smallestY = Float.MAX_VALUE
            mappedPoints.forEach { point ->
                if (smallestX > point.first) smallestX = point.first
                if (smallestY > point.second) smallestY = point.second
            }

            // take bitmap's top left as invariant now, find the relative coordinates of viewport's top left
            val rectTop = -smallestY
            val rectLeft = -smallestX

            // can now calculate the rect of viewport wrt to final bitmap
            val rectRight = rectLeft + scale * sampleImageWidth
            val rectBottom = rectTop + scale * sampleImageHeight

            // this is actually the imageview width and height too
            val rowWidth = rectRight - rectLeft
            val columnHeight = rectBottom - rectTop

            // we want to make sure final cropped image is 1080 pixels or less in width, depending on raw image width
            // if original raw width = 100, insample size was 4, imageview scale is 1.5, then scale is 1.5 * 1/4 to the raw image
            // if new insample size is 2, imageview scale to this new sample size is rawscale * insample = 1.5/4*2
            val imageViewScaleToFinalSampleSize = imageView.scaleFactor / sampleSize.toFloat() * finalSampleSize.toFloat()
            val imageViewScaleAt1080 = 1080 / rowWidth * imageViewScaleToFinalSampleSize

            // if scale value is >1, then we would need to scale the bitmap past intrinsic size (undesirable)
            // if scale value <1, then scale down bitmap
            var finalMatrixPostScale = 1f
            if (imageViewScaleAt1080 < 1f) {
                // if scaling cropped width to 1080 would not involve scaling newSampledImage past 1, then scale
                finalMatrixPostScale = 1080 / rowWidth
            } else {
                // if it is already overscaled compared to the newSampledImage intrinsic size, scale down to original scale
                // or if scaling to 1080 would overscale it, scale up to original size
                finalMatrixPostScale = 1f / imageViewScaleToFinalSampleSize
            }

            // if we want to apply a final postscale, we postscale from the bitmap's top left coordinates
            // the new wanted rect is simply scale*rect
            finalMatrix.postScale(finalMatrixPostScale, finalMatrixPostScale, smallestX, smallestY)
            val finalBitmapTransformed = Bitmap.createBitmap(finalBitmapRaw, 0, 0, finalBitmapWidth, finalBitmapHeight, finalMatrix, true)
            if (finalBitmapTransformed !== finalBitmapRaw) {
                finalBitmapRaw.recycle()
            }

            val finalRectTop = Math.max((rectTop * finalMatrixPostScale).toInt(), 0)
            val finalRectLeft = Math.max((rectLeft * finalMatrixPostScale).toInt(), 0)
            val maxWidth = finalBitmapWidth - finalRectLeft
            val maxHeight = finalBitmapHeight - finalRectTop
            val finalRowWidth = Math.min((rowWidth * finalMatrixPostScale).toInt(), maxWidth)
            val finalColumnHeight = Math.min((columnHeight * finalMatrixPostScale).toInt(), maxHeight)
            val finalBitmap = Bitmap.createBitmap(finalBitmapTransformed, finalRectLeft, finalRectTop, finalRowWidth, finalColumnHeight)
//            val finalBitmap = Bitmap.createBitmap(finalBitmapTransformed, rectLeft.toInt(), rectTop.toInt(), rowWidth.toInt(), columnHeight.toInt())

            if (finalBitmapTransformed !== finalBitmap) {
                finalBitmapTransformed.recycle()
            }

            val mutableBitmap = finalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            finalBitmap.recycle()
            val canvas = Canvas(mutableBitmap)
            val paint = Paint()
            paint.colorFilter = colorFilter
            canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)


            try {
                FileOutputStream(file).use { out ->
                    mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }


            listener?.photoEdited(photoID, filepath)
        }

        return layout
    }


    override fun onRotateRatchetScrolled(centre: Float, current: Float, max: Float, display: TextView) {
        val rawIntendedRotation = (current - centre) / (max - centre) * 50f
        currentRotation =
                when {
                    rawIntendedRotation < -50f -> -50f
                    rawIntendedRotation > 50f -> 50f
                    else -> rawIntendedRotation
                }

        display.text = "${Utils.floatFormat.format(currentRotation)}°"

        rotateImage()
    }

    private fun rationaliseRotation(deg: Float): Float {

        var newRotate = (deg) % 360
        newRotate = if (newRotate < 0f) newRotate + 360f else newRotate

        return newRotate
    }

    override fun onRotateRightAngle(deg: Float) {
        val newBase = baseRotation + deg
        baseRotation = when {
            newBase <= -360f || newBase >= 360f -> 0f
            else -> newBase
        }

        rotateImage()
    }

    private fun rotateImage() {
        imageView.rotateImage(rationaliseRotation(currentRotation + baseRotation))


    }

    override fun onRotateFinished() {
        imageView.rotateFinished()
    }

    private fun startNewEdit(frag: Fragment, tag: String) {

        switchToEditingToolbar(tag)

        // save current edits
        when (tag) {
            TAG_ADJUST -> {
                savedMatrix.set(imageView.imageMatrix)
                savedState = imageView.saveState()
                savedBaseRotation = baseRotation
                savedCurrentRotation = currentRotation
                imageView.canTouch = true

            }
            TAG_FILTER -> {
                savedFilter = filter
                savedColorMatrix.set(colorMatrix)
            }
            TAG_BRIGHTNESS -> {
                savedBrightness = brightness
                savedColorMatrix.set(colorMatrix)
            }
            TAG_CONTRAST -> {
                savedContrast = contrast
                savedColorMatrix.set(colorMatrix)
            }
        }


        val tx = childFragmentManager.beginTransaction()
        tx.add(R.id.subfragment_filter_controls, frag)
        tx.addToBackStack(tag)
        tx.commit()
    }

    private fun switchToEditingToolbar(title: String) {
        layout.subfragment_edit_photo_toolbar_back.visibility = View.GONE
        layout.subfragment_edit_photo_toolbar_next.visibility = View.GONE
        layout.subfragment_edit_photo_toolbar_title.text = title
    }

    private fun switchToOverallToolbar() {
        layout.subfragment_edit_photo_toolbar_back.visibility = View.VISIBLE
        layout.subfragment_edit_photo_toolbar_next.visibility = View.VISIBLE
        layout.subfragment_edit_photo_toolbar_title.text = getString(R.string.edit_photo)
    }

    override fun goToFilter() {
        startNewEdit(FilterSubFragment.newInstance(filter), TAG_FILTER)
    }

    override fun goToAdjust() {
        startNewEdit(AdjustSubFragment.newInstance(currentRotation), TAG_ADJUST)
    }

    override fun goToBrightness() {
        startNewEdit(BrightnessSubFragment.newInstance(brightness), TAG_BRIGHTNESS)
    }

    override fun goToContrast() {
        startNewEdit(ContrastSubFragment.newInstance(contrast), TAG_CONTRAST)
    }

    override fun adjustBrightness(brightness: Int) {
        this.brightness = brightness
        Utils.setBrightnessOnColorMatrix(colorMatrix, brightness)
        colorFilter = ColorMatrixColorFilter(colorMatrix)
        imageView.colorFilter = colorFilter
    }

    override fun adjustContrast(contrastMatrixValue: Float, contrastSetting: Int) {
        this.contrast = contrastSetting
        Utils.setContrastOnColorMatrix(colorMatrix, contrastMatrixValue)
        colorFilter = ColorMatrixColorFilter(colorMatrix)
        imageView.colorFilter = colorFilter
    }

    override fun putFilter(filter: String) {

        when(this.filter){
            FilterSubFragment.GREYSCALE-> colorMatrix.apply {
                val greyMatrix = ColorMatrix().apply {
                    setSaturation(100f)
                }
                postConcat(greyMatrix)
            }
            FilterSubFragment.SEPIA -> colorMatrix.apply {
                val sepiaMatrix = ColorMatrix().apply {
                    setScale(1f, 1f, 1.25f, 1f)
                }
                val sepiaMatrix2 = ColorMatrix().apply {
                    setSaturation(100f)
                }
                postConcat(sepiaMatrix)
                postConcat(sepiaMatrix2)
            }
        }

        when(filter){
            FilterSubFragment.GREYSCALE-> colorMatrix.apply {
                val greyMatrix = ColorMatrix().apply {
                    setSaturation(0.01f)
                }
                postConcat(greyMatrix)
            }
            FilterSubFragment.SEPIA -> colorMatrix.apply {
                val sepiaMatrix = ColorMatrix().apply {
                    setSaturation(0.01f)
                }
                val sepiaMatrix2 = ColorMatrix().apply {
                    setScale(1f, 1f, 0.8f, 1f)
                }
                postConcat(sepiaMatrix)
                postConcat(sepiaMatrix2)
            }
        }
        this.filter = filter
        colorFilter = ColorMatrixColorFilter(colorMatrix)
        imageView.colorFilter = colorFilter
    }

    override fun cancelCurrentEdit():Boolean {

        switchToOverallToolbar()

        if (childFragmentManager.backStackEntryCount > 0) {
            when (childFragmentManager.getBackStackEntryAt(0).name) {
                TAG_ADJUST -> {
                    imageView.canTouch = false
                    imageView.imageMatrix.set(savedMatrix)
                    imageView.restoreState(savedState)
                    currentRotation = savedCurrentRotation
                    baseRotation = savedBaseRotation
                    imageView.invalidate()
                }
                TAG_FILTER -> {
                    filter= savedFilter
                    colorMatrix.set(savedColorMatrix)
                    colorFilter = ColorMatrixColorFilter(colorMatrix)
                    imageView.colorFilter = colorFilter
                }
                TAG_BRIGHTNESS -> {
                    brightness = savedBrightness
                    colorMatrix.set(savedColorMatrix)
                    colorFilter = ColorMatrixColorFilter(colorMatrix)
                    imageView.colorFilter = colorFilter
                }
                TAG_CONTRAST -> {
                    contrast = savedContrast
                    colorMatrix.set(savedColorMatrix)
                    colorFilter = ColorMatrixColorFilter(colorMatrix)
                    imageView.colorFilter = colorFilter
                }
            }
            childFragmentManager.popBackStack()

        } else {
            if(saveDialog.isShowing) saveDialog.cancel() else saveDialog.show()
        }
        return false
    }


    override fun done() {
        switchToOverallToolbar()
        imageView.canTouch = false
        savedMatrix.set(imageView.imageMatrix)
        savedColorMatrix.set(colorMatrix)
        savedState = imageView.saveState()
        savedBrightness = brightness
        savedContrast = contrast
        savedFilter = filter
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        }
    }

    interface PhotoEditListener {
        fun photoEdited(photoID: String, postFilepath: String)

        fun editCancelled()
    }

    fun saveState(): EditPhotoState {
        return EditPhotoState().apply {
            this.currentRotation = this@EditPhotoSubFragment.currentRotation
            this.baseRotation = this@EditPhotoSubFragment.baseRotation
            val imageMatrixArray = FloatArray(9)
            this@EditPhotoSubFragment.imageView.imageMatrix.getValues(imageMatrixArray)
            imageMatrixArray.forEach {
                this.imageMatrix.add(it)
            }
            this@EditPhotoSubFragment.colorMatrix.array.forEach {
                this.colorMatrix.add(it)
            }
            this.brightness = this@EditPhotoSubFragment.brightness
            this.contrast = this@EditPhotoSubFragment.contrast
            this.saturation = this@EditPhotoSubFragment.saturation
            this.filter = this@EditPhotoSubFragment.filter
        }
    }

    fun restoreState(state: EditPhotoState?) {
        state?.let {
            this@EditPhotoSubFragment.currentRotation = it.currentRotation
            this@EditPhotoSubFragment.savedCurrentRotation = it.currentRotation
            this@EditPhotoSubFragment.baseRotation = it.baseRotation
            this@EditPhotoSubFragment.savedBaseRotation = it.baseRotation

            val imageMatrixArray = FloatArray(9)
            it.imageMatrix.forEachIndexed { index, fl ->
                imageMatrixArray[index] = fl
            }
            this@EditPhotoSubFragment.savedMatrix.setValues(imageMatrixArray)

            val colorMatrixArray = it.colorMatrix.toFloatArray()
            this@EditPhotoSubFragment.colorMatrix.set(colorMatrixArray)
            this@EditPhotoSubFragment.savedColorMatrix.set(colorMatrixArray)
            this@EditPhotoSubFragment.colorFilter = ColorMatrixColorFilter(colorMatrix)

            this@EditPhotoSubFragment.brightness = it.brightness
            this@EditPhotoSubFragment.savedBrightness = it.brightness
            this@EditPhotoSubFragment.contrast = it.contrast
            this@EditPhotoSubFragment.savedContrast = it.contrast
            this@EditPhotoSubFragment.saturation = it.saturation
            this@EditPhotoSubFragment.savedSaturation = it.saturation
            this@EditPhotoSubFragment.filter = it.filter
            this@EditPhotoSubFragment.savedFilter = it.filter
        }
    }


}