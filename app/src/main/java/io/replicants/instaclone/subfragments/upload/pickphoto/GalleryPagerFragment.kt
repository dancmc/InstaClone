package io.replicants.instaclone.subfragments.upload.pickphoto

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.theartofdev.edmodo.cropper.CropImageView
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.GalleryCursorAdapter
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.Prefs
import kotlinx.android.synthetic.main.subfragment_feed.view.*
import kotlinx.android.synthetic.main.subfragment_gallery.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class GalleryPagerFragment : BaseSubFragment(), GalleryCursorAdapter.Listener {


    private lateinit var layout: View
    private var directoryList = ArrayList<ImageDirectory>()
    lateinit var adapter: GalleryCursorAdapter
    lateinit var recyclerView: RecyclerView
    var photoObtainedListener: PickPhotoSubFragment.PhotoObtainedListener? = null
    lateinit var gridManager: GridLayoutManager
    var draftSelected = false
    var draft: SavedPhoto? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this::layout.isInitialized) {
            return layout
        }

        layout = inflater.inflate(R.layout.subfragment_gallery, container, false)

        recyclerView = layout.subfragment_gallery_recyclerview
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)
        gridManager = GridLayoutManager(activity, 4);
        recyclerView.layoutManager = gridManager

        layout.subfragment_gallery_toolbar_back.onClick {
            activity?.finish()
        }

        layout.subfragment_gallery_toolbar_next.onClick {
            if (draft != null) {
                photoObtainedListener?.photoObtained(draft!!.photoID, draft!!.photoFile)
            } else {
                (childFragmentManager.findFragmentById(R.id.subfragment_gallery_image_container) as CropSubFragment).getImageAsync()
            }
        }


        if (childFragmentManager.findFragmentById(R.id.subfragment_gallery_image_container) == null) {
            val tx = childFragmentManager.beginTransaction()
            val cropFrag = CropSubFragment()
            cropFrag.onCropImageCompleteListener = CropImageView.OnCropImageCompleteListener { view: CropImageView?, result: CropImageView.CropResult? ->
                val photoFolder = File(activity!!.filesDir, "photos")
                if (!photoFolder.exists()) {
                    photoFolder.mkdir()
                }
                val photoID = UUID.randomUUID().toString()
                val newPhotoFile = File(photoFolder, "$photoID.jpg")
                FileOutputStream(newPhotoFile).use { out ->
                    result?.bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    photoObtainedListener?.photoObtained(photoID, newPhotoFile.absolutePath)
                }
            }
            tx.add(R.id.subfragment_gallery_image_container, cropFrag, null)
            tx.commit()
        }


        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            populateDirectory()

        } else {
            // todo put some button
        }

        layout.subfragment_gallery_toolbar_longclick.inflateMenu(R.menu.gallery_longclick)

        layout.subfragment_gallery_toolbar_longclick.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_delete->{
                    val realm = Realm.getDefaultInstance()
                    realm.beginTransaction()
                    adapter.getLongClickItems().forEach { photo->
                        photo.deleteFromRealm()
                    }
                    realm.commitTransaction()
                    adapter.cancelLongClickMode()
                    val drafts = realm.where(SavedPhoto::class.java).findAll().mapTo(ArrayList<SavedPhoto>()) { p -> p }
                    adapter.reloadDrafts(drafts)
                }
                else->{}
            }
            true
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

                val dirListString = directoryList.mapTo(ArrayList()) { dir -> dir.albumName }
                val spinnerAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, dirListString).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                layout.subfragment_gallery_toolbar_directory_spinner.adapter = spinnerAdapter
                layout.subfragment_gallery_toolbar_directory_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }

                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        adapter.swapCursor(getDirectoryCursor(directoryList[position]))
                    }
                }
                val initialCursor = getDirectoryCursor(directoryList[0])
                val drafts = Realm.getDefaultInstance().where(SavedPhoto::class.java).findAll().mapTo(ArrayList<SavedPhoto>()) { p -> p }
                adapter = GalleryCursorAdapter(context!!, gridManager, drafts, initialCursor, this@GalleryPagerFragment)
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

    private fun getImageDirectories(): ArrayList<ImageDirectory> {
        val projection = arrayOf("DISTINCT " + MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val cursor = MediaStore.Images.Media.query(activity?.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null)

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

    private fun refresh() {
        //todo
    }


    data class ImageDirectory(var id: Int, var albumName: String) : Serializable {
        override fun hashCode(): Int {
            return id.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return other is ImageDirectory && id == other.id
        }
    }

    fun reloadDrafts() {
        if (this::adapter.isInitialized) {
            val drafts = Realm.getDefaultInstance().where(SavedPhoto::class.java).findAll().mapTo(ArrayList<SavedPhoto>()) { p -> p }
            if (draftSelected) {
                adapter.swapCursor(adapter.cursor)
            } else {
                adapter.reloadDrafts(drafts)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Prefs.EXTERNAL_STORAGE_CODE) {
            if (permissions.size == 1 && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Prefs.EXTERNAL_STORAGE_CODE)

                    permissionGranted()

                    context?.toast("Storage permission granted")
                } else {
                    // check if user checked never show again
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Prefs.getInstance().writeBoolean(Prefs.EXTERNAL_STORAGE_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                    }
                    permissionDenied()


                    context?.toast("Storage permission not granted")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reloadDrafts()
    }

    override fun onClick(filePath: String, position: Int, draft: SavedPhoto?) {
        val cropFrag = childFragmentManager.findFragmentById(R.id.subfragment_gallery_image_container) as CropSubFragment
        if (draft != null) {
            draftSelected = true
            this@GalleryPagerFragment.draft = draft
            cropFrag.setPlainImage(draft.photoFilePreview)
        } else {
            draftSelected = false
            this@GalleryPagerFragment.draft = null
            cropFrag.setImageUri(Uri.fromFile(File(filePath)))
        }
    }

    override fun onLongClick() {
        layout.subfragment_gallery_toolbar_longclick.visibility = View.VISIBLE
        layout.subfragment_gallery_toolbar_longclick_title.text = "1 photo selected"
        layout.subfragment_gallery_toolbar_longclick_cancel.onClick {
            adapter.cancelLongClickMode()
            adapter.notifyDataSetChanged()
        }

    }

    override fun onLongClickChanged() {
        val num = adapter.getLongClickItems().size
        layout.subfragment_gallery_toolbar_longclick_title.text = "$num photo${if(num!=1)"s" else ""} selected"
    }

    override fun onLongClickCancelled() {
        layout.subfragment_gallery_toolbar_longclick.visibility = View.GONE

    }

    fun handleBackPressed():Boolean{
        if(adapter.inLongClickMode){
            adapter.cancelLongClickMode()
            adapter.notifyDataSetChanged()
            return true
        }
        return false
    }
}