package io.replicants.instaclone.subfragments.upload.pickphoto

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.*
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.rotate
import kotlinx.android.synthetic.main.subfragment_camera.view.*
import kotlinx.android.synthetic.main.subfragment_gallery.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class CameraPagerFragment : BaseSubFragment() {

    companion object {
        @JvmField
        val SIDE_FRONT = CameraInfo.CAMERA_FACING_FRONT
        @JvmField
        val SIDE_BACK = CameraInfo.CAMERA_FACING_BACK
    }

    lateinit var layout: View
    var fotoapparat: Fotoapparat? = null
    var photoObtainedListener: PickPhotoSubFragment.PhotoObtainedListener? = null
    var frontWidth = 0
    var frontHeight = 0
    var backWidth = 0
    var backHeight = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //TODO check somewhere that camera actually exists

        if(this::layout.isInitialized){
            return layout
        }

        layout = inflater.inflate(R.layout.subfragment_camera, container, false)
        layout.subfragment_camera_toolbar.onClick {
            activity?.finish()
        }

        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    fun permissionGranted() {
        initialiseCameraPreview()
    }

    fun permissionDenied() {
        //TODO create some request button
    }

    private fun initialiseCameraPreview() {

        val time = System.currentTimeMillis()
        // TODO consider putting this in async
        doAsync {
            setCameraResolutions()
            println("TIME ${System.currentTimeMillis() - time}")
            uiThread {
                val time2 = System.currentTimeMillis()
                val flashStatus = Prefs.getInstance().readBoolean(Prefs.CAMERA_FLASH_STATUS, false)
                val side = Prefs.getInstance().readInt(Prefs.CAMERA_SIDE, SIDE_BACK)

                fotoapparat = Fotoapparat(
                        context = activity!!,
                        view = layout.subfragment_camera_cameraview,                   // view which will draw the camera preview
                        focusView = layout.subfragment_camera_focusview,
                        scaleType = ScaleType.CenterCrop,    // (optional) we want the preview to fill the view
                        lensPosition = if (side == SIDE_BACK) back() else front(),               // (optional) we want back camera
                        cameraConfiguration = getConfig(side, flashStatus), // (optional) define an advanced configuration
                        cameraErrorCallback = { error ->
                            println(error.message)
                        }   // (optional) log fatal errors
                )

                layout.subfragment_camera_photo_btn.onClick {
                    layout.subfragment_camera_photo_btn.isEnabled = false
                    val result = fotoapparat?.takePicture()

                    if (result == null) {
                        layout.subfragment_camera_photo_btn.isEnabled = true
                    }

                    val photoFolder = File(context!!.filesDir, "photos")
                    if (!photoFolder.exists()) {
                        photoFolder.mkdir()
                    }
                    val photoID = UUID.randomUUID().toString()
                    val file = File(photoFolder, "$photoID.jpg")
                    result?.toBitmap()?.whenAvailable {
                        layout.subfragment_camera_photo_btn.isEnabled = true
                        if (it != null) {
                            val bitmap = it.bitmap.rotate(-it.rotationDegrees.toFloat())
                            try {
                                FileOutputStream(file).use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                    photoObtainedListener?.photoObtained(photoID, file.absolutePath)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }
                }

                layout.subfragment_camera_flash.isSelected = Prefs.getInstance().readBoolean(Prefs.CAMERA_FLASH_STATUS, false)
                layout.subfragment_camera_flash.onClick {
                    val currentFlashStatus = layout.subfragment_camera_flash.isSelected
                    val newFlashStatus = !currentFlashStatus
                    val cameraSide = Prefs.getInstance().readInt(Prefs.CAMERA_SIDE, SIDE_BACK)

                    layout.subfragment_camera_flash.isSelected = newFlashStatus
                    Prefs.getInstance().writeBoolean(Prefs.CAMERA_FLASH_STATUS, layout.subfragment_camera_flash.isSelected)
                    fotoapparat?.updateConfiguration(getConfig(cameraSide, layout.subfragment_camera_flash.isSelected))
                }

                layout.subfragment_camera_flip.onClick {

                    val currentSide = Prefs.getInstance().readInt(Prefs.CAMERA_SIDE, SIDE_BACK)
                    val newSide = if (currentSide == SIDE_FRONT) SIDE_BACK else SIDE_FRONT
                    val flashStatus = Prefs.getInstance().readBoolean(Prefs.CAMERA_FLASH_STATUS, false)
                    Prefs.getInstance().writeInt(Prefs.CAMERA_SIDE, newSide)

                    layout.subfragment_camera_flash.isEnabled = newSide == SIDE_BACK
                    layout.subfragment_camera_flash.isSelected = flashStatus

                    fotoapparat?.switchTo(
                            if (newSide == SIDE_FRONT) front() else back(), getConfig(newSide, flashStatus)
                    )
                }

                println("TIME2 ${System.currentTimeMillis() - time2}")

                val time3 = System.currentTimeMillis()
                try {
                    fotoapparat?.start()
                    println("TIME3 ${System.currentTimeMillis() - time3}")
                } catch (e: IllegalStateException) {
                    println(e.message)
                }
            }
        }


    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initialiseCameraPreview()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), Prefs.CAMERA_REQUEST_CODE)
            }
        }, 500)


    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
    }

    private fun getCameraResolutions(side: Int): List<Pair<Int, Int>> {
        val noOfCameras = Camera.getNumberOfCameras()

        val list = ArrayList<Pair<Int, Int>>()
        for (i in 0 until noOfCameras) {
            val cameraInfo = CameraInfo()
            Camera.getCameraInfo(i, cameraInfo)

            if (cameraInfo.facing == side) {
                val camera = Camera.open(i)
                val cameraParams = camera.parameters
                for (j in 0 until cameraParams.supportedPictureSizes.size) {
                    list.add(Pair(cameraParams.supportedPictureSizes[j].width, cameraParams.supportedPictureSizes[j].height))
                }
                camera.release()
            }
        }

        return list
    }

    // determine smallest resolution with shortest side >= 1080 closest to a square
    private fun getFilteredCameraResolution(side: Int): Pair<Int, Int> {
        val camResolution = getCameraResolutions(side).sortedBy {
            it.first * it.second
        }
        var desiredRes: Pair<Int, Int>
        try {
            desiredRes = camResolution.first {
                it.first >= 1080 && it.second >= 1080
            }
        } catch (e: Exception) {
            desiredRes = Pair(1080, 1080)
        }
        return desiredRes
    }

    private fun setCameraResolutions() {

        arrayListOf(SIDE_FRONT, SIDE_BACK).forEach {
            when (it) {
                SIDE_FRONT -> {

                    frontWidth = Prefs.getInstance().readInt(Prefs.CAMERA_FRONT_WIDTH, 0)
                    frontHeight = Prefs.getInstance().readInt(Prefs.CAMERA_FRONT_HEIGHT, 0)

                    if (frontHeight == 0 || frontWidth == 0) {
                        val result = getFilteredCameraResolution(it)
                        frontWidth = result.first
                        frontHeight = result.second
                        Prefs.getInstance().writeInt(Prefs.CAMERA_FRONT_WIDTH, frontWidth)
                        Prefs.getInstance().writeInt(Prefs.CAMERA_FRONT_HEIGHT, frontHeight)
                    }
                }
                SIDE_BACK -> {

                    backWidth = Prefs.getInstance().readInt(Prefs.CAMERA_BACK_WIDTH, 0)
                    backHeight = Prefs.getInstance().readInt(Prefs.CAMERA_BACK_HEIGHT, 0)

                    if (backHeight == 0 || backWidth == 0) {
                        val result = getFilteredCameraResolution(it)
                        backWidth = result.first
                        backHeight = result.second
                        Prefs.getInstance().writeInt(Prefs.CAMERA_BACK_WIDTH, backWidth)
                        Prefs.getInstance().writeInt(Prefs.CAMERA_BACK_HEIGHT, backHeight)
                    }
                }
            }
        }
    }

    private fun getConfig(side: Int, flashStatus: Boolean): CameraConfiguration {
        val desiredRes = if (side == SIDE_BACK) Resolution(backWidth, backHeight) else Resolution(frontWidth, frontHeight)

        return CameraConfiguration(
                pictureResolution = firstAvailable(
                        { desiredRes },
                        highestResolution()
                ),
                focusMode = firstAvailable(
                        // (optional) use the first focus mode which is supported by device
                        continuousFocusPicture(),
                        autoFocus(),                       // if continuous focus is not available on device, auto focus will be used
                        fixed()                            // if even auto focus is not available - fixed focus mode will be used
                ),
                flashMode = if (flashStatus && side == SIDE_BACK) on() else off(),
                antiBandingMode = firstAvailable(       // (optional) similar to how it is done for focus mode & flash, now for anti banding
                        auto(),
                        hz50(),
                        hz60(),
                        none()
                ),
                jpegQuality = manualJpegQuality(90)    // (optional) select a jpeg quality of 90 (out of 0-100) values
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Prefs.CAMERA_REQUEST_CODE) {
            if (permissions.size == 1 && permissions[0] == Manifest.permission.CAMERA) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted()
                    context?.toast("Camera permission granted")
                } else {
                    // check if user checked never show again
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Prefs.getInstance().writeBoolean(Prefs.CAMERA_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                    }
                    permissionDenied()

                    context?.toast("Camera permission not granted")
                }
            }
        }
    }
}