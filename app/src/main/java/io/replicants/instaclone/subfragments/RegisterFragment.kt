package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.LoginActivity
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*

class RegisterFragment:Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_register, container, false)

        layout.input_register_email.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                layout.button_register.performClick()
                Utils.hideKeyboardFrom(activity!!.applicationContext, layout.input_register_email)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        layout.button_register.setOnClickListener {
            // TODO need to do some validation of inputs
            (activity as LoginActivity).register(
                    layout.input_register_username.text.toString(),
                    layout.input_register_password.text.toString(),
                    layout.input_register_first_name.text.toString(),
                    layout.input_register_last_name.text.toString(),
                    layout.input_register_display_name.text.toString(),
                    layout.input_register_email.text.toString()
            )
        }

        layout.button_login_alt.setOnClickListener {
            (activity as LoginActivity).gotoLogin()
        }

        return layout
    }
}