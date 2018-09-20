package io.replicants.instaclone.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.subfragments.LoginSubFragment
import io.replicants.instaclone.subfragments.RegisterSubFragment
import io.replicants.instaclone.utilities.Prefs
import org.jetbrains.anko.toast
import org.json.JSONObject

// Simple activity with two fragments - login and register
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.add(R.id.login_container, LoginSubFragment())
        transaction.commit()

    }

    fun goToRegister() {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.login_container, RegisterSubFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun gotoLogin() {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.login_container, LoginSubFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun login(username: String, password: String) {
        val callback = InstaApi.generateCallback(this,  object: InstaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
                val jwt = jsonResponse.optString("jwt")
                if (jwt.isNotBlank()) {
                    Prefs.getInstance().writeString(Prefs.JWT, jwt)
                    Prefs.getInstance().writeString(Prefs.USERNAME, jsonResponse.optString("username"))
                    Prefs.getInstance().writeString(Prefs.USER_ID, jsonResponse.optString("user_id"))
                    Prefs.getInstance().writeString(Prefs.DISPLAY_NAME, jsonResponse.optString("display_name"))
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun failure(context: Context, jsonResponse: JSONObject?) {
                this@LoginActivity.toast(jsonResponse?.optString("error_message") ?:"")
            }
        })
        InstaApi.userLogin(username, password).enqueue(callback)

    }

    fun register(username: String, password: String, firstName: String, lastName: String, displayName: String, email: String) {
        val callback = InstaApi.generateCallback(this,  object: InstaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
                val jwt = jsonResponse.optString("jwt")
                if (jwt.isNotBlank()) {
                    Prefs.getInstance().writeString(Prefs.JWT, jwt)
                    Prefs.getInstance().writeString(Prefs.USERNAME, jsonResponse.optString("username"))
                    Prefs.getInstance().writeString(Prefs.USER_ID, jsonResponse.optString("user_id"))
                    Prefs.getInstance().writeString(Prefs.DISPLAY_NAME, jsonResponse.optString("display_name"))
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun failure(context: Context,jsonResponse: JSONObject?) {
                Toast.makeText(this@LoginActivity,jsonResponse?.optString("error_message"), Toast.LENGTH_SHORT).show()
            }
        })

        InstaApi.userRegister(username, password, firstName, lastName, displayName, email).enqueue(callback)
    }
}