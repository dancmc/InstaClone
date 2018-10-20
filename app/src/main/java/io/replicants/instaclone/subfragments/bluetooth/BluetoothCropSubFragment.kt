package io.replicants.instaclone.subfragments.bluetooth

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.theartofdev.edmodo.cropper.CropImageView
import io.replicants.instaclone.R
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.CropSubFragment
import kotlinx.android.synthetic.main.subfragment_bluetooth_crop.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.FileOutputStream
import java.util.*


private const val TAG = "TAG_BLUETOOTH"

class BluetoothCropSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(photoID:String, filepath:String): BluetoothCropSubFragment {
            val myFragment = BluetoothCropSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("photoID", photoID)
            args.putString("filepath", filepath)

            return myFragment
        }
    }

    lateinit var layout: View
    lateinit var filepath :String
    lateinit var photoID :String
    lateinit var cropFrag:CropSubFragment


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }

        layout = inflater.inflate(R.layout.subfragment_bluetooth_crop, container, false)
        filepath  = arguments?.getString("filepath") ?:""
        photoID  = arguments?.getString("photoID") ?:""

        if(filepath.isBlank() || photoID.isBlank()){
            (activity as? BluetoothActivityInterface)?.errorAndGoBack("Error : File not accessible")
        }

        layout.subfragment_bluetooth_crop_toolbar_back.onClick {
            (activity as? BluetoothActivityInterface)?.goBack()
        }

        layout.subfragment_bluetooth_crop_toolbar_next.onClick {
            (childFragmentManager.findFragmentById(R.id.subfragment_bluetooth_crop_container) as CropSubFragment).getImageAsync()
        }

        if (childFragmentManager.findFragmentById(R.id.subfragment_bluetooth_crop_container) == null) {
            val tx = childFragmentManager.beginTransaction()
            cropFrag = CropSubFragment()
            cropFrag.arguments = Bundle().apply { putString("filepath", filepath) }
            cropFrag.onCropImageCompleteListener = CropImageView.OnCropImageCompleteListener { view: CropImageView?, result: CropImageView.CropResult? ->
                val photoFolder = File(activity!!.filesDir, "photos")
                if (!photoFolder.exists()) {
                    photoFolder.mkdir()
                }
                val photoID = UUID.randomUUID().toString()
                val newPhotoFile = File(photoFolder, "$photoID.jpg")
                FileOutputStream(newPhotoFile).use { out ->
                    result?.bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    (activity as? BluetoothActivityInterface)?.photoCropped(newPhotoFile.absolutePath)
                }
            }
            tx.add(R.id.subfragment_bluetooth_crop_container, cropFrag, null)
            tx.commit()


        }

        return layout
    }


}