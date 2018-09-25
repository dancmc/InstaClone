package io.replicants.instaclone.subfragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Prefs.DISPLAY_NAME
import kotlinx.android.synthetic.main.subfragment_editprofile.*
import kotlinx.android.synthetic.main.subfragment_editprofile.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.File

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

    lateinit var layout: View
    var newPhotoFile: File? = null

    @Nullable
    override fun onCreateView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View {

        layout = inflater.inflate(R.layout.subfragment_editprofile, container, false)

        InstaApi.getDetails().enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject) {

                var displayName = jsonResponse.optString("display_name")
                val username = Prefs.getInstance().readString(Prefs.USERNAME, "")
                var lastName = jsonResponse.optString("last_name")
                var firstName = jsonResponse.optString("first_name")
                var profileName = jsonResponse.optString("profile_name")
                var profileDesc = jsonResponse.optString("profile_desc")
                var email = jsonResponse.optString("email")

                var etUsername = layout.profile_username
                var etDisplayName = layout.input_display_name
                var etFirst = layout.firstname_input
                var etLast = layout.lastname_input
                var etProfileName = layout.profile_name
                var etProfileDesc = layout.profile_desc
                var etEmail = layout.email_input

                etUsername.setText(username)
                etDisplayName.setText(displayName)
                etFirst.setText(firstName)
                etLast.setText(lastName)
                etProfileName.setText(profileName)
                etProfileDesc.setText(profileDesc)
                etEmail.setText(email)

                // check whether it is private account
                var privacy = jsonResponse.optBoolean("is_private")
                var privateSwitch = layout.private_switch

                private_switch.isChecked = privacy

                privateSwitch.setOnCheckedChangeListener { _, isChecked ->
                    privacy = isChecked
                }


                var profileImage = jsonResponse.optString("profile_image")
                Glide.with(context!!).load(profileImage).into(layout.profile_image)

                // back button
                var toolbar = layout.edit_profile_toolbar
                toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
                toolbar.onClick {
                    activity?.onBackPressed()
                }

                // pick photo from library
                var photoChangeButton = layout.image_button
                photoChangeButton.onClick {
                    val pickPhoto = Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1)//one can be replaced with any action code
                }

                // confirm the update of profile
                var confirmButton = layout.button_confirm
                confirmButton.onClick {
                    var inputDisplayName = layout.input_display_name.text.toString()
                    var inputEmail = layout.email_input.text.toString()
                    var inputLastName = layout.lastname_input.text.toString()
                    var inputFirstName = layout.firstname_input.text.toString()
                    var inputProfileDesc = layout.profile_desc.text.toString()
                    var inputProfileName = layout.profile_name.text.toString()
                    var password: String? = layout.change_password.text.toString()

                    if (password!!.isBlank()) {
                        password = null
                    }

                    if (inputDisplayName != displayName) {
                        Prefs.getInstance().writeString(DISPLAY_NAME, inputDisplayName)
                    }
                    InstaApi.updateDetails(newPhotoFile, password, inputEmail, inputFirstName, inputLastName, displayName, inputProfileName, inputProfileDesc, privacy).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                        override fun success(jsonResponse: JSONObject?) {
                            context?.toast("Successfully updated details")
                        }
                    }))
                }
            }
        }))

        return layout
    }

    // change profile photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val selectedImage = data?.getData()
            if (selectedImage != null) {
                newPhotoFile = selectedImage.toFile()
                Glide.with(context!!).load(selectedImage).into(layout.profile_image)
            }
        }
    }
}
