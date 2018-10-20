package io.replicants.instaclone.subfragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.utilities.GlideHeader
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_editprofile.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.File

class EditProfileSubFragment : BaseSubFragment() {


    companion object {

        val PHOTO_REQUEST_CODE = 1

        @JvmStatic
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

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_editprofile, container, false)

        InstaApi.getDetails().enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject) {

                val displayName = jsonResponse.optString("display_name")
                val lastName = jsonResponse.optString("last_name")
                val firstName = jsonResponse.optString("first_name")
                val profileName = jsonResponse.optString("profile_name")
                val profileDesc = jsonResponse.optString("profile_desc")
                val email = jsonResponse.optString("email")

                layout.input_display_name.setText(displayName)
                layout.firstname_input.setText(firstName)
                layout.lastname_input.setText(lastName)
                layout.profile_name.setText(profileName)
                layout.profile_desc.setText(profileDesc)
                layout.email_input.setText(email)

                // check whether it is private account
                var privacy = jsonResponse.optBoolean("is_private")
                layout.private_switch.isChecked = privacy
                layout.private_switch.setOnCheckedChangeListener { _, isChecked ->
                    privacy = isChecked
                }

                val profileImage = jsonResponse.optString("profile_image")
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context!!).load(GlideHeader.getUrlWithHeaders(profileImage))
                        .apply(requestOptions)
                        .into(layout.profile_image)

                // back button
                layout.edit_profile_toolbar.setNavigationIcon(R.drawable.icon_back_grey)
                layout.edit_profile_toolbar.onClick {
                    activity?.onBackPressed()
                }

                // pick photo from library
                layout.image_button.onClick {
                    val pickPhoto = Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, PHOTO_REQUEST_CODE)//one can be replaced with any action code
                }

                // confirm the update of profile
                val confirmButton = layout.button_confirm
                confirmButton.onClick {
                    val inputDisplayName = layout.input_display_name.text.toString()
                    val inputEmail = layout.email_input.text.toString()
                    val inputLastName = layout.lastname_input.text.toString()
                    val inputFirstName = layout.firstname_input.text.toString()
                    val inputProfileDesc = layout.profile_desc.text.toString()
                    val inputProfileName = layout.profile_name.text.toString()
                    var oldPassword: String? = layout.old_password.text.toString()
                    var password: String? = layout.new_password.text.toString()

                    if (oldPassword!!.isBlank()) {
                        oldPassword = null
                    }

                    if (password!!.isBlank()) {
                        password = null
                    }


                    Utils.hideKeyboardFrom(activity!!.applicationContext, layout.profile_desc)

                    val dialog = ProgressDialog(context).apply {
                        setMessage("Updating details...")
                        show()
                    }

                    // todo scale the photo first
                    InstaApi.updateDetails(newPhotoFile, oldPassword, password, inputEmail, inputFirstName, inputLastName, inputDisplayName, inputProfileName, inputProfileDesc, privacy).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                        override fun success(jsonResponse: JSONObject?) {
                            context?.toast("Successfully updated details")
                            if (dialog.isShowing) {
                                dialog.dismiss()
                            }
                            Utils.updateDetails(context!!, {
                                clickListeners?.popBackStack(true);
                            })
                        }

                        override fun failure(context: Context?, jsonResponse: JSONObject?) {
                            super.failure(context, jsonResponse)
                            if (dialog.isShowing) {
                                dialog.dismiss()
                            }
                        }

                        override fun networkFailure(context: Context?,code:Int) {
                            super.networkFailure(context,code)
                            if (dialog.isShowing) {
                                dialog.dismiss()
                            }
                        }
                    }))
                }
            }
        }))

        return layout
    }

    // change profile photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PHOTO_REQUEST_CODE) {
            val selectedImage = data?.data
            if (selectedImage != null) {

                val cursor = activity?.contentResolver?.query(selectedImage, arrayOf(android.provider.MediaStore.Images.ImageColumns.DATA), null, null, null)
                cursor?.moveToFirst()
                newPhotoFile = cursor?.getString(0)?.let {
                    File(it)
                }
                cursor?.close()
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context!!)
                        .load(newPhotoFile)
                        .apply(requestOptions)
                        .into(layout.profile_image)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
