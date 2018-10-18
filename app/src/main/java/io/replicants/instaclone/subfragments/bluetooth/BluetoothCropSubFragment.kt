package io.replicants.instaclone.subfragments.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.BTListAdapter
import io.replicants.instaclone.adapters.BluetoothGalleryCursorAdapter
import io.replicants.instaclone.pojos.BluetoothItem
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.subfragments.upload.pickphoto.GalleryPagerFragment
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.utilities.Utils.Companion.getDirectoryCursor
import kotlinx.android.synthetic.main.subfragment_bluetooth.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.HashMap
import android.R.attr.button
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewTreeObserver
import com.theartofdev.edmodo.cropper.CropImageView
import io.replicants.instaclone.subfragments.upload.pickphoto.CropSubFragment
import kotlinx.android.synthetic.main.subfragment_bluetooth_crop.view.*
import kotlinx.android.synthetic.main.subfragment_gallery.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream


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