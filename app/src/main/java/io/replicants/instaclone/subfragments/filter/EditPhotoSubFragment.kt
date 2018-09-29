package io.replicants.instaclone.subfragments.filter

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.views.ZoomRotateImageView
import kotlinx.android.synthetic.main.subfragment_edit_photo.view.*
import org.jetbrains.anko.windowManager
import java.text.DecimalFormat

class EditPhotoSubFragment : BaseSubFragment(), RotateSubFragment.ImageAdjust, ChooseSubFragment.OnChooseListener {

    companion object {

        @JvmStatic
        fun newInstance(tempFileName: String): EditPhotoSubFragment {
            val myFragment = EditPhotoSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("fileName", tempFileName)

            return myFragment
        }
    }

    lateinit var layout: View
    lateinit var imageView: ZoomRotateImageView
    lateinit var center: Pair<Float, Float>

    var currentRotation = 0f
    var savedCurrentRotation = 0f
    var baseRotation = 0f
    var savedBaseRotation = 0f
    var savedMatrix = Matrix()
    lateinit var savedState:ZoomRotateImageView.State

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_edit_photo, container, false)
        val filename = arguments!!.getString("fileName")


        //create the overall bitmap
        var options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filename, options)
        val outShortest = Math.min(options.outHeight, options.outWidth)

        options = BitmapFactory.Options()
        options.inSampleSize = if (outShortest <= 1600) 1 else outShortest / 1600 + 1
        val bitmap = BitmapFactory.decodeFile(filename, options)
        val finalImageWidth = options.outWidth
        val finalImageHeight = options.outHeight

        val display = context!!.windowManager.defaultDisplay
        val displayDimen = Point()
        display.getSize(displayDimen)
        val displayWidth = displayDimen.x

        imageView = ZoomRotateImageView(context!!)
        imageView.scaleType = ImageView.ScaleType.MATRIX
        var scale = 1f
        if (finalImageHeight > finalImageWidth) {
            scale = displayWidth / finalImageHeight.toFloat()
            imageView.layoutParams = FrameLayout.LayoutParams(Math.min(Math.ceil(finalImageWidth * scale.toDouble()).toInt(), displayWidth),
                    FrameLayout.LayoutParams.MATCH_PARENT)
            imageView.imageMatrix = imageView.imageMatrix.apply {

                postScale(scale, scale)
            }
        } else if (finalImageHeight < finalImageWidth) {
            scale = displayWidth / finalImageWidth.toFloat()
            imageView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    Math.min(Math.ceil(finalImageHeight * scale.toDouble()).toInt(), displayWidth))
            imageView.imageMatrix = imageView.imageMatrix.apply {
                postScale(scale, scale)
            }
        } else {
            scale = displayWidth / finalImageWidth.toFloat()
            imageView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            imageView.imageMatrix = imageView.imageMatrix.apply {

                postScale(scale, scale)
            }
        }

        layout.subfragment_filter_preview.addView(imageView)
        center = Pair(finalImageWidth * scale, finalImageHeight * scale)
        imageView.setMinZoom(scale)
        imageView.setImageBitmap(bitmap)
        savedState = imageView.saveState()

        val tx = childFragmentManager.beginTransaction()
        val frag = ChooseSubFragment.newInstance()
        frag.onChooseListener = this
        tx.add(R.id.subfragment_filter_controls, frag)
        tx.commit()


        return layout
    }

    override fun onRotateRatchetScrolled(centre: Float, current: Float, max: Float, display: TextView) {
        val rawIntendedRotation = (current - centre) / (max - centre) * 50f
        currentRotation =
                when{
                    rawIntendedRotation < -50f-> -50f
                    rawIntendedRotation > 50f -> 50f
                    else -> rawIntendedRotation
                }

        display.text = "${Utils.floatFormat.format(currentRotation)}°"

        rotateImage()
    }

    fun rationaliseRotation(deg:Float):Float{

        var newRotate = (deg) % 360
        newRotate = if (newRotate < 0f) newRotate + 360f else newRotate

        return newRotate
    }

    override fun onRotateRightAngle(deg: Float) {
        val newBase = baseRotation+deg
        baseRotation = when{
            newBase<=-360f || newBase>=360f-> 0f
            else -> newBase
        }

        rotateImage()
    }

    private fun rotateImage() {
        imageView.rotateImage(rationaliseRotation(currentRotation+baseRotation))


    }

    override fun onRotateFinished() {
        imageView.rotateFinished()
    }

    private fun startNewEdit(frag: Fragment){

        // save current edits
        savedMatrix.set(imageView.imageMatrix)
        savedState = imageView.saveState()
        savedBaseRotation = baseRotation
        savedCurrentRotation = currentRotation

        val tx = childFragmentManager.beginTransaction()
        tx.add(R.id.subfragment_filter_controls, frag)
        tx.addToBackStack(null)
        tx.commit()
    }

    override fun goToFilter() {

    }

    override fun goToAdjust() {
        startNewEdit(RotateSubFragment.newInstance(currentRotation))
    }

    override fun goToBrightness() {

    }

    override fun goToContrast() {

    }

    override fun cancelCurrentEdit() :Boolean{

        if(childFragmentManager.backStackEntryCount>0) {
            childFragmentManager.popBackStack()
            imageView.imageMatrix.set(savedMatrix)
            imageView.restoreState(savedState)
            currentRotation = savedCurrentRotation
            baseRotation = savedBaseRotation
            imageView.invalidate()

            return true
        }
        return false
    }

    override fun done() {
        savedMatrix.set(imageView.imageMatrix)
        savedState = imageView.saveState()
        if(childFragmentManager.backStackEntryCount>0) {
            childFragmentManager.popBackStack()
        }
    }
}