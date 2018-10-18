package io.replicants.instaclone.subfragments.upload.pickphoto

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.theartofdev.edmodo.cropper.CropImageView
import io.replicants.instaclone.R
import kotlinx.android.synthetic.main.subfragment_crop.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.File


class CropSubFragment : Fragment(), CropImageView.OnSetImageUriCompleteListener {

    // todo remember to constrain crop ratio to 2:1 on either side
    var cropImageView: CropImageView? = null
    var onCropImageCompleteListener: CropImageView.OnCropImageCompleteListener? = null
    lateinit var rootView: View
    var plainImageView: ImageView? = null


    /** Set the image to show for cropping.  */
    fun setImageUri(imageUri: Uri) {

        var tries = 20
        launch(UI) {
            while (cropImageView == null && tries > 0) {
                delay(100)
                tries--
            }
            cropImageView?.visibility = View.VISIBLE
            plainImageView?.visibility = View.GONE
            context?.let {
                Glide.with(it).clear(plainImageView!!)
            }
            cropImageView?.setImageUriAsync(imageUri)
        }
    }

    fun setPlainImage(path: String) {
        cropImageView?.visibility = View.GONE
        cropImageView?.clearImage()
        plainImageView?.visibility = View.VISIBLE
        context?.let {
            val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
            Glide.with(it).load(path).apply(requestOptions).into(plainImageView!!)
        }

    }

    fun clear() {
        cropImageView?.clearImage()
    }

    /** Set the options of the crop image view to the given values.  */
//    fun setCropImageViewOptions(options: CropImageViewOptions) {
//        cropImageView!!.scaleType = options.scaleType
//        cropImageView!!.cropShape = options.cropShape
//        cropImageView!!.guidelines = options.guidelines
//        cropImageView!!.setAspectRatio(options.aspectRatio.first, options.aspectRatio.second)
//        cropImageView!!.setFixedAspectRatio(options.fixAspectRatio)
//        cropImageView!!.setMultiTouchEnabled(options.multitouch)
//        cropImageView!!.isShowCropOverlay = options.showCropOverlay
//        cropImageView!!.isShowProgressBar = options.showProgressBar
//        cropImageView!!.isAutoZoomEnabled = options.autoZoomEnabled
//        cropImageView!!.maxZoom = options.maxZoomLevel
//        cropImageView!!.isFlippedHorizontally = options.flipHorizontally
//        cropImageView!!.isFlippedVertically = options.flipVertically
//    }

    /** Set the initial rectangle to use.  */
    fun setInitialCropRect() {
        cropImageView!!.cropRect = Rect(100, 300, 500, 1200)
    }

    /** Reset crop window to initial rectangle.  */
    fun resetCropRect() {
        cropImageView!!.resetCropRect()
    }

    fun updateCurrentCropViewOptions() {
        // have some preset options
//        val options = CropImageViewOptions()
//        options.scaleType = cropImageView!!.scaleType
//        options.cropShape = cropImageView!!.cropShape
//        options.guidelines = cropImageView!!.guidelines
//        options.aspectRatio = cropImageView!!.aspectRatio
//        options.fixAspectRatio = cropImageView!!.isFixAspectRatio
//        options.showCropOverlay = cropImageView!!.isShowCropOverlay
//        options.showProgressBar = cropImageView!!.isShowProgressBar
//        options.autoZoomEnabled = cropImageView!!.isAutoZoomEnabled
//        options.maxZoomLevel = cropImageView!!.maxZoom
//        options.flipHorizontally = cropImageView!!.isFlippedHorizontally
//        options.flipVertically = cropImageView!!.isFlippedVertically
//        (activity as MainActivity).setCurrentOptions(options)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val startingPath = arguments?.getString("filepath")

        rootView = inflater.inflate(R.layout.subfragment_crop, container, false)
        cropImageView = rootView.findViewById(R.id.subfragment_crop_imageview)
        cropImageView!!.setOnSetImageUriCompleteListener(this)
        cropImageView!!.setOnCropImageCompleteListener(onCropImageCompleteListener)
//        cropImageView!!.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        cropImageView!!.setCropShape(CropImageView.CropShape.RECTANGLE);
        cropImageView!!.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
        cropImageView!!.rotatedDegrees
        plainImageView = rootView.subfragment_plain_imageview

        updateCurrentCropViewOptions()

        if(startingPath!=null){
            cropImageView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    cropImageView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    setImageUri(Uri.fromFile(File(startingPath)))
                }
            })
        }

        return rootView
    }


    override fun onResume() {
        super.onResume()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.getItemId() === R.id.main_action_crop) {
//            cropImageView!!.getCroppedImageAsync()
//            return true
//        } else if (item.getItemId() === R.id.main_action_rotate) {
//            cropImageView!!.rotateImage(90)
//            return true
//        } else if (item.getItemId() === R.id.main_action_flip_horizontally) {
//            cropImageView!!.flipImageHorizontally()
//            return true
//        } else if (item.getItemId() === R.id.main_action_flip_vertically) {
//            cropImageView!!.flipImageVertically()
//            return true
//        }
        return super.onOptionsItemSelected(item)
    }

    fun rotate(deg: Int) {
        cropImageView?.rotateImage(deg)
    }


    override fun onStop() {
        super.onStop()

    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error == null) {

        } else {
            Log.e("AIC", "Failed to load image by URI", error)
            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                    .show()
        }
    }


    public fun getImageAsync() {
        cropImageView?.getCroppedImageAsync()
    }

}