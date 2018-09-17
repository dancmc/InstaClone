package io.replicants.instaclone.maintabs

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.subfragments.LoginFragment
import io.replicants.instaclone.subfragments.RegisterFragment
import io.replicants.instaclone.utilities.Prefs
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.add(R.id.login_container, LoginFragment())
        transaction.commit()

    }

    fun goToRegister() {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.login_container, RegisterFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun gotoLogin() {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.login_container, LoginFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun login(username: String, password: String) {
        val callback = InstaApi.generateCallback(this,  object: InstaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
                val jwt = jsonResponse.optString("jwt")
                if (jwt.isNotBlank()) {
                    Prefs.getInstance().writeString(Prefs.JWT, jwt)
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun failure(jsonResponse: JSONObject?) {
                Toast.makeText(this@LoginActivity,jsonResponse?.optString("error_message"), Toast.LENGTH_SHORT).show()
            }
        })
        InstaApi.userLogin(username, password, callback)

    }

    fun register(username: String, password: String, firstName: String, lastName: String, displayName: String, email: String) {
        val callback = InstaApi.generateCallback(this,  object: InstaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
                val jwt = jsonResponse.optString("jwt")
                if (jwt.isNotBlank()) {
                    Prefs.getInstance().writeString(Prefs.JWT, jwt)
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                }
            }

            override fun failure(jsonResponse: JSONObject?) {
                Toast.makeText(this@LoginActivity,jsonResponse?.optString("error_message"), Toast.LENGTH_SHORT).show()
            }
        })

        InstaApi.userRegister(username, password, firstName, lastName, displayName, email, callback)
    }
}