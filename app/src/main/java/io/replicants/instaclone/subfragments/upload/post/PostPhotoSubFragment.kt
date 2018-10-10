package io.replicants.instaclone.subfragments.upload.post

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.LocationCallback
import io.replicants.instaclone.utilities.MyApplication
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_post_photo.view.*
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.appcompat.v7.alertDialogLayout
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.File

class PostPhotoSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(photoID: String, postFilepath: String): PostPhotoSubFragment {
            val myFragment = PostPhotoSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("photoID", photoID)
            args.putString("postFilepath", postFilepath)

            return myFragment
        }
    }

    lateinit var layout: View
    var listener: PhotoPostListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_post_photo, container, false)

        val filepath = arguments?.getString("postFilepath") ?: ""
        val photoID = arguments?.getString("photoID") ?: ""


        layout.subfragment_post_photo_toolbar_back.onClick {
            listener?.goBackToEdit()
        }

        layout.subfragment_post_photo_toolbar_post.onClick {

        }

        val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
        Glide.with(context!!).load(filepath).apply(requestOptions).into(layout.subfragment_post_image)

        context?.let {
            layout.subfragment_post_photo_coordinates_switch.isChecked = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        layout.subfragment_post_photo_coordinates_switch.onCheckedChange { _, isChecked ->
            if (isChecked) {
                MyApplication.instance.getLocation(activity!!, object : LocationCallback {
                    override fun execute(location: Location?) {
                        // do nothing
                    }

                    override fun permissionFailed() {
                       handleLocationFailed()
                    }
                })
            }
        }

        layout.subfragment_post_photo_toolbar_post.onClick {

            val imageFile = File(filepath)
            val caption = layout.subfragment_post_caption.text.toString()
            val locationName = layout.subfragment_post_location_name.text.toString()
            var longitude :Double? = null
            var latitude :Double? = null

            fun handlePost(){

                val dialog = ProgressDialog(context).apply {
                    setMessage("Posting...")
                    show()
                }

                InstaApi.uploadPhoto(imageFile, caption, latitude, longitude, locationName)
                        .enqueue(InstaApi.generateCallback(context, object: InstaApiCallback(){
                            override fun success(jsonResponse: JSONObject?) {
                                // remove from pending database
                                val realm = Realm.getDefaultInstance()
                                realm.beginTransaction()
                                realm.where(SavedPhoto::class.java).equalTo("photoID", photoID).findFirst()?.deleteFromRealm()
                                realm.commitTransaction()

                                if(dialog.isShowing){
                                    dialog.dismiss()
                                }

                                listener?.photoPosted()
                            }
                        }))
            }

            if(layout.subfragment_post_photo_coordinates_switch.isChecked){
                MyApplication.instance.getLocation(activity!!, object : LocationCallback {
                    override fun execute(location: Location?) {
                        longitude = location?.longitude
                        latitude = location?.latitude
                        handlePost()
                    }

                    override fun permissionFailed() {
                        handleLocationFailed()
                    }
                })
            } else {
                handlePost()
            }

        }


        return layout
    }


    interface PhotoPostListener {
        fun photoPosted()

        fun goBackToEdit()
    }

    fun handleLocationFailed(){
        context?.toast(R.string.request_location_failed)
        layout.subfragment_post_photo_coordinates_switch.isChecked = false

        if (Prefs.getInstance().readBoolean(Prefs.LOCATION_DENIED_FOREVER, false)) {

            context?.let { c ->
                Utils.redirectToSettings(R.string.request_location_title, R.string.request_location_text, c)
            }
        }
    }




}