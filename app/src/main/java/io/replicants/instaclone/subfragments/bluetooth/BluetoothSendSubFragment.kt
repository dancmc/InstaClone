package io.replicants.instaclone.subfragments.bluetooth

import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.InRangePhoto
import io.replicants.instaclone.subfragments.BaseSubFragment
import io.replicants.instaclone.utilities.LocationCallback
import io.replicants.instaclone.utilities.MyApplication
import io.replicants.instaclone.utilities.Prefs
import kotlinx.android.synthetic.main.subfragment_bluetooth_send.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class BluetoothSendSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(postFilepath: String): BluetoothSendSubFragment {
            val myFragment = BluetoothSendSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("postFilepath", postFilepath)

            return myFragment
        }
    }

    lateinit var layout: View
    var dialog:ProgressDialog?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_bluetooth_send, container, false)

        val filepath = arguments?.getString("postFilepath") ?: ""


        layout.subfragment_bluetooth_send_toolbar_back.onClick {
            (activity as? BluetoothActivityInterface)?.goBack()
        }


        val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
        Glide.with(context!!).load(filepath).apply(requestOptions).into(layout.subfragment_bluetooth_send_image)



        layout.subfragment_bluetooth_send_toolbar_post.onClick {

            val caption = layout.subfragment_bluetooth_send_caption.text.toString()
            val locationName = layout.subfragment_bluetooth_send_location_name.text.toString()
            var longitude :Double? = null
            var latitude :Double? = null

            fun handlePost(){

                dialog = ProgressDialog(context).apply {
                    setMessage("Sending...")
                    show()
                }

                val photo = InRangePhoto()
                photo.displayName = Prefs.getInstance().readString(Prefs.DISPLAY_NAME,"")
                photo.caption = caption
                photo.locationName = locationName
                longitude?.let { l->
                    photo.longitude = l
                }
                latitude?.let { l->
                    photo.latitude = l
                }

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(filepath, options)
                val width = options.outWidth
                val height = options.outHeight

                photo.regularWidth = width
                photo.regularHeight = height

                (activity as? BluetoothActivityInterface)?.sendPhoto(photo, filepath)
            }

            MyApplication.instance.getLocation(activity!!, object : LocationCallback {
                override fun execute(location: Location?) {
                    longitude = location?.longitude
                    latitude = location?.latitude
                    handlePost()
                }

                override fun permissionFailed() {
                    handlePost()
                }
            })

        }


        return layout
    }

    fun dismissProgress(){
        dialog?.let {
            if(it.isShowing){
                it.dismiss()
            }
        }
    }




}