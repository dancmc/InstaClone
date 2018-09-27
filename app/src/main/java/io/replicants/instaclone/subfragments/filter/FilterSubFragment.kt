package io.replicants.instaclone.subfragments.filter

import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.ortiz.touch.TouchImageView
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.views.ZoomRotateImageView
import kotlinx.android.synthetic.main.subfragment_filter.view.*
import kotlinx.android.synthetic.main.subfragment_rotate.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.windowManager
import java.text.DecimalFormat

class FilterSubFragment : BaseSubFragment(), RotateSubFragment.RotateImage {

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
    lateinit var imageView: ZoomRotateImageView
    lateinit var center: Pair<Float, Float>

    var currentRotation = 0f
    var baseRotation = 0f
    val floatFormat = DecimalFormat("##0.0")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_filter, container, false)
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
        imageView.minZoom = scale
        imageView.setImageBitmap(bitmap)

        val tx = childFragmentManager.beginTransaction()
        val frag = RotateSubFragment.newInstance()
        tx.add(R.id.subfragment_filter_controls, frag)
        tx.commit()


        return layout
    }

    override fun onRotateRatchetScrolled(centre: Float, current: Float, max: Float, display: TextView) {
        val rawIntendedRotation = (current - centre) / (max - centre) * 50f
        val absoluteIntendedRotation =
                when{
                    rawIntendedRotation < -50f-> -50f
                    rawIntendedRotation > 50f -> 50f
                    else -> rawIntendedRotation
                }
        val additionalRotation = absoluteIntendedRotation - currentRotation
        currentRotation = absoluteIntendedRotation

        display.text = "${floatFormat.format(absoluteIntendedRotation)}°"

        rotateImage(additionalRotation)
    }

    private fun rationaliseRotation(deg:Float):Float{
        return when{
            deg<=-360f || deg>=360f-> 0f
            else -> deg
        }
    }

    override fun onRotateRightAngle(deg: Float) {
        val newBase = baseRotation+deg
        baseRotation = when{
            newBase<=-360f || newBase>=360f-> 0f
            else -> newBase
        }

        rotateImage(deg)
    }

    private fun rotateImage(deg: Float) {
        imageView.rotateImage(deg)


    }
}