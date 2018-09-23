package io.replicants.instaclone.subfragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
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
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.rotate
import kotlinx.android.synthetic.main.subfragment_camera.view.*
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class CameraSubFragment : BaseSubFragment() {

    companion object {
        @JvmField
        val SIDE_FRONT = CameraInfo.CAMERA_FACING_FRONT
        @JvmField
        val SIDE_BACK = CameraInfo.CAMERA_FACING_BACK
    }

    lateinit var layout: View
    var fotoapparat: Fotoapparat? = null
    var photoTakenListener: PhotoTakenListener? = null
    var frontWidth = 0
    var frontHeight = 0
    var backWidth = 0
    var backHeight = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //TODO check somewhere that camera actually exists
        layout = inflater.inflate(R.layout.subfragment_camera, container, false)

        Handler().postDelayed({
            if (ContextCompat.checkSelfPermission(activity as AppCompatActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initialiseCameraPreview()
            } else {
                ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.CAMERA), Prefs.CAMERA_REQUEST_CODE)
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
                val newSide = if(currentSide== SIDE_FRONT) SIDE_BACK else SIDE_FRONT
                val flashStatus = Prefs.getInstance().readBoolean(Prefs.CAMERA_FLASH_STATUS, false)
                Prefs.getInstance().writeInt(Prefs.CAMERA_SIDE, newSide)

                layout.subfragment_camera_flash.isEnabled = newSide== SIDE_BACK
                layout.subfragment_camera_flash.isSelected = flashStatus

                fotoapparat?.switchTo(
                        if(newSide ==SIDE_FRONT) front() else back(),getConfig(newSide, flashStatus)
                )
            }
        }, 500)



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

        setCameraResolutions()
        val flashStatus = Prefs.getInstance().readBoolean(Prefs.CAMERA_FLASH_STATUS, false)
        val side = Prefs.getInstance().readInt(Prefs.CAMERA_SIDE, SIDE_BACK)

        fotoapparat = Fotoapparat(
                context = context!!,
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

            if(result==null){
                layout.subfragment_camera_photo_btn.isEnabled = true
            }

            val photoFolder = File(context!!.filesDir, "photos")
            if (!photoFolder.exists()) {
                photoFolder.mkdir()
            }
            val file = File(photoFolder, "${UUID.randomUUID()}.jpg")
            result?.toBitmap()?.whenAvailable {
                layout.subfragment_camera_photo_btn.isEnabled = true
                if (it != null) {
                    val bitmap = it.bitmap.rotate(-it.rotationDegrees.toFloat())
                    try {
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            photoTakenListener?.photoTaken(file.name)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        try{
            fotoapparat?.start()
        } catch (e:IllegalStateException){
            println(e.message)
        }
    }

    override fun onStart() {
        super.onStart()
        try{
            fotoapparat?.start()
        } catch (e:IllegalStateException){
            println(e.message)
        }

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
    private fun getFilteredCameraResolution(side:Int):Pair<Int, Int>{
        val camResolution = getCameraResolutions(side).sortedBy {
            it.first * it.second
        }
        var desiredRes: Pair<Int, Int>
        try {
            desiredRes = camResolution.first {
                it.first >= 1080 && it.second >= 1080
            }
        } catch (e: Exception) {
            desiredRes = Pair(1080,1080)
        }
        return desiredRes
    }

    private fun setCameraResolutions(){
        arrayListOf(SIDE_FRONT, SIDE_BACK).forEach {
            when(it){
                SIDE_FRONT->{
                    val result = getFilteredCameraResolution(it)
                    frontWidth = result.first
                    frontHeight = result.second
                }
                SIDE_BACK->{
                    val result = getFilteredCameraResolution(it)
                    backWidth = result.first
                    backHeight = result.second
                }
            }
        }
    }

    private fun getConfig(side:Int, flashStatus: Boolean): CameraConfiguration {
        val desiredRes = if(side== SIDE_BACK) Resolution(backWidth, backHeight) else Resolution(frontWidth, frontHeight)

        return CameraConfiguration(
                pictureResolution = firstAvailable(
                        { desiredRes},
                        highestResolution()
                ),
                focusMode = firstAvailable(
                        // (optional) use the first focus mode which is supported by device
                        continuousFocusPicture(),
                        autoFocus(),                       // if continuous focus is not available on device, auto focus will be used
                        fixed()                            // if even auto focus is not available - fixed focus mode will be used
                ),
                flashMode = if (flashStatus && side== SIDE_BACK) on() else off(),
                antiBandingMode = firstAvailable(       // (optional) similar to how it is done for focus mode & flash, now for anti banding
                        auto(),
                        hz50(),
                        hz60(),
                        none()
                ),
                jpegQuality = manualJpegQuality(90)    // (optional) select a jpeg quality of 90 (out of 0-100) values
        )
    }

    interface PhotoTakenListener {
        fun photoTaken(filename: String)
    }
}