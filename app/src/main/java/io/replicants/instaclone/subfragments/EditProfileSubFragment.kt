package io.replicants.instaclone.subfragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Prefs.DISPLAY_NAME
import kotlinx.android.synthetic.main.subfragment_editprofile.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONObject

class EditProfileSubFragment : BaseSubFragment() {


    companion object {

        fun newInstance(displayName: String): EditProfileSubFragment {
            val myFragment = EditProfileSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("displayName", displayName)

            return myFragment
        }
    }

    lateinit var layout:View

    @Nullable
    override fun onCreateView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View {

        layout = inflater.inflate(R.layout.subfragment_editprofile, container, false)

        InstaApi.getDetails().enqueue(InstaApi.generateCallback(context, object : InstaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
                var displayName = jsonResponse.optString("display_name")
                var etDisplayName = layout.input_display_name
                etDisplayName.setText(displayName)

                val username = Prefs.getInstance().readString(Prefs.USERNAME,"")
                var etUsername = layout.profile_username
                etUsername.setText(username)

                var inputDisplayName = layout.input_display_name.text.toString()

                var profileImage = jsonResponse.optString("profile_image")
                Glide.with(context!!).load(profileImage).into(layout.profile_image)


                // pick photo from library
                var photoChangeButton = layout.image_button
                photoChangeButton.onClick {
                    val pickPhoto = Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1)//one can be replaced with any action code
                }

                // confirm the update of profile
                var confirmButton = layout.button_confirm
                confirmButton.onClick{
                    if (inputDisplayName != displayName){
                        Prefs.getInstance().writeString(DISPLAY_NAME,inputDisplayName)
                    }
                }
            }
        }))
        
        return layout
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1){
            val selectedImage = data?.getData()
            Glide.with(context!!).load(selectedImage).into(layout.profile_image)
        }
    }
}
