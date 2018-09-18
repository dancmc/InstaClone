package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.replicants.instaclone.R
import io.replicants.instaclone.activities.LoginActivity
import kotlinx.android.synthetic.main.fragment_login.view.*
import android.view.inputmethod.EditorInfo
import io.replicants.instaclone.utilities.Utils


class LoginFragment:Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_login, container, false)

        layout.input_login_password.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                layout.button_login.performClick()
                Utils.hideKeyboardFrom(activity!!.applicationContext, layout.input_login_password)

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        layout.button_login.setOnClickListener {
            (activity as LoginActivity).login(
                    layout.input_login_username.text.toString(),
                    layout.input_login_password.text.toString())
        }

        layout.button_register_alt.setOnClickListener {
            (activity as LoginActivity).goToRegister()
        }

        return layout
    }
}