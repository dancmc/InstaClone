package io.replicants.instaclone.subfragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.theartofdev.edmodo.cropper.CropImageView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.GalleryCursorAdapter
import io.replicants.instaclone.utilities.Prefs
import kotlinx.android.synthetic.main.adapter_feed_item_grid.view.*
import kotlinx.android.synthetic.main.subfragment_gallery.view.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.HashSet


class GallerySubFragment : BaseSubFragment() {



    private lateinit var layout: View
    private var directoryList = ArrayList<ImageDirectory>()
    lateinit var adapter: GalleryCursorAdapter
    lateinit var recyclerView: RecyclerView
    var photoObtainedListener:GetPhotoSubFragment.PhotoObtainedListener? = null
    var firstLoad = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(firstLoad) {
        layout = inflater.inflate(R.layout.subfragment_gallery, container, false)

            recyclerView = layout.subfragment_gallery_recyclerview
            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(40)
            recyclerView.setDrawingCacheEnabled(true)
            val layoutManager = GridLayoutManager(activity, 4);
            recyclerView.layoutManager = layoutManager

            layout.subfragment_gallery_toolbar.inflateMenu(R.menu.menu_gallery_fragment)
            layout.subfragment_gallery_toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_next -> {
                        (childFragmentManager.findFragmentById(R.id.subfragment_gallery_image_container) as CropSubFragment).getImageAsync()
                        true
                    }
                    else -> false
                }

            }


            if (childFragmentManager.findFragmentById(R.id.subfragment_gallery_image_container) == null) {
                val tx = childFragmentManager.beginTransaction()
                val cropFrag = CropSubFragment()
                cropFrag.onCropImageCompleteListener = CropImageView.OnCropImageCompleteListener { view: CropImageView?, result: CropImageView.CropResult? ->
                    val photoFolder = File(activity!!.filesDir, "photos")
                    val newPhotoFile = File(photoFolder, UUID.randomUUID().toString() + ".jpg")
                    FileOutputStream(newPhotoFile).use { out ->
                        result?.bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        photoObtainedListener?.photoObtained(newPhotoFile.absolutePath)
                    }
                }
                tx.add(R.id.subfragment_gallery_image_container, cropFrag, null)
                tx.commit()
            }

            if (ContextCompat.checkSelfPermission(activity as AppCompatActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                populateDirectory()


            } else {
                ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Prefs.EXTERNAL_STORAGE_CODE)
            }

            firstLoad = false
        }

        return layout
    }



    fun permissionGranted() {
        populateDirectory()
    }

    fun permissionDenied() {
        //TODO create some request button
    }

    private fun populateDirectory() {

        doAsync {
            val result = getImageDirectories()
            uiThread {
                directoryList.addAll(result)
                // put in spinner

                val dirListString = directoryList.mapTo(ArrayList()) { it.albumName }
                val spinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, dirListString).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                layout.subfragment_gallery_directory_spinner.adapter = spinnerAdapter
                layout.subfragment_gallery_directory_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        adapter.swapCursor(getDirectoryCursor(directoryList[position]))
                    }
                }
                val initialCursor = getDirectoryCursor(directoryList[0])
                adapter = GalleryCursorAdapter(context!!, initialCursor, object : GalleryCursorAdapter.Listener {
                    override fun onClick(filePath: String, position:Int) {
//                        Glide.with(context!!).load(filePath).into(layout.subfragment_gallery_image)
                        (childFragmentManager.findFragmentById(R.id.subfragment_gallery_image_container) as CropSubFragment).setImageUri(Uri.fromFile(File(filePath)))
                    }
                })
                recyclerView.adapter = adapter


            }
        }

    }

    private fun getDirectoryCursor(imageDirectory: ImageDirectory): Cursor {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
        val cursor: Cursor
        if (imageDirectory.albumName == "All") {
            cursor = MediaStore.Images.Media.query(context!!.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
        } else {
            cursor = MediaStore.Images.Media.query(context!!.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.BUCKET_ID + " = ?", arrayOf("${imageDirectory.id}"), MediaStore.Images.Media.DATE_TAKEN + " DESC");
        }
        return cursor
    }

    private fun getImageDirectories() :ArrayList<ImageDirectory>{
        val projection = arrayOf("DISTINCT "+MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val cursor = MediaStore.Images.Media.query(context?.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null)

        cursor.moveToFirst()
        val albumName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val albumID = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)

        val directorySet = HashSet<ImageDirectory>()
        while (cursor.moveToNext()) {
            directorySet.add(ImageDirectory(cursor.getInt(albumID), cursor.getString(albumName)))
        }
        cursor.close()
        val resultList = ArrayList<ImageDirectory>()
        resultList.addAll(directorySet)
        resultList.sortBy { it.albumName }
        resultList.add(0, ImageDirectory(-128937, "All"))
        return resultList
    }


    data class ImageDirectory(var id: Int, var albumName: String):Serializable {
        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return other is ImageDirectory && id == other.id
        }
    }

}