package io.replicants.instaclone.subfragments.upload.pickphoto

import androidx.fragment.app.Fragment
import com.theartofdev.edmodo.cropper.CropImageView
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.LayoutInflater
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.MenuItem
import android.view.View
import io.replicants.instaclone.R


class CropSubFragment : Fragment(), CropImageView.OnSetImageUriCompleteListener{

// todo remember to constrain crop ratio to 2:1 on either side
    private var mCropImageView: CropImageView? = null
    var onCropImageCompleteListener:CropImageView.OnCropImageCompleteListener? = null
    lateinit var rootView : View


    /** Set the image to show for cropping.  */
    fun setImageUri(imageUri: Uri) {
        mCropImageView!!.setImageUriAsync(imageUri)

        //        CropImage.activity(imageUri)
        //                .start(getContext(), this);
    }

    fun clear(){
        mCropImageView?.clearImage()
    }

    /** Set the options of the crop image view to the given values.  */
//    fun setCropImageViewOptions(options: CropImageViewOptions) {
//        mCropImageView!!.scaleType = options.scaleType
//        mCropImageView!!.cropShape = options.cropShape
//        mCropImageView!!.guidelines = options.guidelines
//        mCropImageView!!.setAspectRatio(options.aspectRatio.first, options.aspectRatio.second)
//        mCropImageView!!.setFixedAspectRatio(options.fixAspectRatio)
//        mCropImageView!!.setMultiTouchEnabled(options.multitouch)
//        mCropImageView!!.isShowCropOverlay = options.showCropOverlay
//        mCropImageView!!.isShowProgressBar = options.showProgressBar
//        mCropImageView!!.isAutoZoomEnabled = options.autoZoomEnabled
//        mCropImageView!!.maxZoom = options.maxZoomLevel
//        mCropImageView!!.isFlippedHorizontally = options.flipHorizontally
//        mCropImageView!!.isFlippedVertically = options.flipVertically
//    }

    /** Set the initial rectangle to use.  */
    fun setInitialCropRect() {
        mCropImageView!!.cropRect = Rect(100, 300, 500, 1200)
    }

    /** Reset crop window to initial rectangle.  */
    fun resetCropRect() {
        mCropImageView!!.resetCropRect()
    }

    fun updateCurrentCropViewOptions() {
//        val options = CropImageViewOptions()
//        options.scaleType = mCropImageView!!.scaleType
//        options.cropShape = mCropImageView!!.cropShape
//        options.guidelines = mCropImageView!!.guidelines
//        options.aspectRatio = mCropImageView!!.aspectRatio
//        options.fixAspectRatio = mCropImageView!!.isFixAspectRatio
//        options.showCropOverlay = mCropImageView!!.isShowCropOverlay
//        options.showProgressBar = mCropImageView!!.isShowProgressBar
//        options.autoZoomEnabled = mCropImageView!!.isAutoZoomEnabled
//        options.maxZoomLevel = mCropImageView!!.maxZoom
//        options.flipHorizontally = mCropImageView!!.isFlippedHorizontally
//        options.flipVertically = mCropImageView!!.isFlippedVertically
//        (activity as MainActivity).setCurrentOptions(options)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.subfragment_crop, container, false)
        mCropImageView = rootView.findViewById(R.id.subfragment_crop_imageview)
        mCropImageView!!.setOnSetImageUriCompleteListener(this)
        mCropImageView!!.setOnCropImageCompleteListener(onCropImageCompleteListener)
//        mCropImageView!!.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        mCropImageView!!.setCropShape(CropImageView.CropShape.RECTANGLE);
        mCropImageView!!.setGuidelines(CropImageView.Guidelines.ON_TOUCH);
        mCropImageView!!.rotatedDegrees
//        mCropImageView!!.setAspectRatio(options.aspectRatio.first, options.aspectRatio.second);
//        mCropImageView!!.setFixedAspectRatio(options.fixAspectRatio);
//        mCropImageView!!.setMultiTouchEnabled(true);
//        mCropImageView!!.setShowCropOverlay(options.showCropOverlay);
//        mCropImageView!!.setShowProgressBar(options.showProgressBar);
//        mCropImageView!!.setAutoZoomEnabled(options.autoZoomEnabled);
//        mCropImageView!!.setMaxZoom(options.maxZoomLevel);
//        mCropImageView!!.setFlippedHorizontally(options.flipHorizontally);
//        mCropImageView!!.setFlippedVertically(options.flipVertically);

        updateCurrentCropViewOptions()
        return rootView
    }


    override fun onResume() {
        super.onResume()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.getItemId() === R.id.main_action_crop) {
//            mCropImageView!!.getCroppedImageAsync()
//            return true
//        } else if (item.getItemId() === R.id.main_action_rotate) {
//            mCropImageView!!.rotateImage(90)
//            return true
//        } else if (item.getItemId() === R.id.main_action_flip_horizontally) {
//            mCropImageView!!.flipImageHorizontally()
//            return true
//        } else if (item.getItemId() === R.id.main_action_flip_vertically) {
//            mCropImageView!!.flipImageVertically()
//            return true
//        }
        return super.onOptionsItemSelected(item)
    }

    fun rotate(deg:Int){
        mCropImageView?.rotateImage(deg)
    }


    override fun onStop() {
        super.onStop()
//        if (mCropImageView != null) {
//            mCropImageView!!.setOnSetImageUriCompleteListener(null)
//            mCropImageView!!.setOnCropImageCompleteListener(null)
//        }
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error == null) {
            Toast.makeText(activity, "Image load successful", Toast.LENGTH_SHORT).show()

        } else {
            Log.e("AIC", "Failed to load image by URI", error)
            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                    .show()
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            val result = CropImage.getActivityResult(data)
//            handleCropResult(result)
//        }
    }

    public fun getImageAsync(){
        mCropImageView?.getCroppedImageAsync()
    }

}