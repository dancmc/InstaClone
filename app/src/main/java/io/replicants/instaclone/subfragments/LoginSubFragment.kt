package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import io.replicants.instaclone.R
import io.replicants.instaclone.activities.LoginActivity
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_login.view.*


class LoginSubFragment:BaseSubFragment(){

    companion object {

        @JvmStatic
        fun newInstance(): LoginSubFragment {
            val myFragment = LoginSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_login, container, false)

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