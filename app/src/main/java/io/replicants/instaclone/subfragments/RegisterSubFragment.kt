package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import io.replicants.instaclone.R
import io.replicants.instaclone.activities.LoginActivity
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_register.view.*

class RegisterSubFragment:BaseSubFragment(){

    companion object {

        @JvmStatic
        fun newInstance(): RegisterSubFragment {
            val myFragment = RegisterSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_register, container, false)

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