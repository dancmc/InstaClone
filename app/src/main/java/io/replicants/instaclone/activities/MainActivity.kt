package io.replicants.instaclone.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.util.TypedValue
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.*
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.utilities.ExampleClass
import io.replicants.instaclone.utilities.MyApplication
import io.replicants.instaclone.utilities.Prefs
import org.jetbrains.anko.toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG_HOME = "home"
        private val TAG_DISCOVER = "discover"
        private val TAG_ACTIVITY = "activity"
        private val TAG_PROFILE = "profile"
    }


    private var backStack = ArrayList<String>()
    private var currentFragment = ""
    private var backPressed = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ExampleClass(this)

        val menuView = navigation.getChildAt(0) as BottomNavigationMenuView
        for (i in 0 until menuView.childCount) {
            val iconView = menuView.getChildAt(i).findViewById<View>(R.id.icon)
            val layoutParams = iconView.layoutParams
            val displayMetrics = resources.displayMetrics
            layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, displayMetrics).toInt()
            layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, displayMetrics).toInt()
            iconView.layoutParams = layoutParams
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (Prefs.getInstance().readString(Prefs.JWT,"").isBlank()) {
            logout()
        } else {
            if (savedInstanceState != null) {
                currentFragment = savedInstanceState.getString("currentFragment")
            } else {
                switchFragment(TAG_HOME)
            }
            InstaApi.validate().enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>?, response: Response<String>?) {
                    val responseJson = JSONObject(response?.body() ?: "{}")
                    val success = responseJson.optBoolean("success")
                    if (!success) {
                        if (responseJson.optInt("error_code", -1) == 0) {
                            logout()
                        }
                    }
                }

                override fun onFailure(call: Call<String>?, t: Throwable?) {
                }
            })
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                switchFragment(TAG_HOME)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_discover -> {
                switchFragment(TAG_DISCOVER)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_add_photo -> {
                val intent = Intent(this, AddPhotoActivity::class.java)
                startActivity(intent)
                return@OnNavigationItemSelectedListener false
            }
            R.id.navigation_activity -> {
                switchFragment(TAG_ACTIVITY)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                switchFragment(TAG_PROFILE)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun switchFragment(target: String) {

        if(currentFragment == target){
            (supportFragmentManager.findFragmentByTag(currentFragment) as BaseMainFragment).clearBackStack()
        }else {

            val manager = supportFragmentManager
            val transaction = manager.beginTransaction()
            val currentFrag = supportFragmentManager.findFragmentByTag(currentFragment)
            if (currentFrag != null) {
                transaction.hide(currentFrag)
            }

            var newFrag = supportFragmentManager.findFragmentByTag(target)
            if (newFrag == null) {

                newFrag = when (target) {
                    TAG_HOME -> HomeMainFragment.newInstance()
                    TAG_DISCOVER -> DiscoverMainFragment.newInstance()
                    TAG_ACTIVITY -> ActivityMainFragment.newInstance()
                    TAG_PROFILE -> ProfileMainFragment.newInstance()
                    else -> HomeMainFragment.newInstance()
                }!!

                transaction.add(R.id.activity_container, newFrag, target)
            } else {
                transaction.show(newFrag)
            }

            backStack.remove(target)
            if (!backPressed) {
                if (currentFragment.isNotBlank()) {
                    backStack.add(currentFragment)
                }
            }
            currentFragment = target
            backPressed = false

            transaction.commit()
        }

    }

    override fun onBackPressed() {

        val fm = supportFragmentManager
        for (frag in fm.fragments) {
            if (frag.isVisible) {
                val childFm = frag.childFragmentManager
                if (childFm.backStackEntryCount > 0) {
                    childFm.popBackStack()
                    return
                }
            }
        }

        if (backStack.isNotEmpty()) {
            backPressed = true
            navigation.selectedItemId = when (backStack.last()) {
                TAG_HOME -> R.id.navigation_home
                TAG_DISCOVER -> R.id.navigation_discover
                TAG_ACTIVITY -> R.id.navigation_activity
                TAG_PROFILE -> R.id.navigation_profile
                else -> null
            } ?: navigation.selectedItemId
        } else {
            super.onBackPressed()
        }
    }


    fun logout() {
        Prefs.getInstance().writeString(Prefs.JWT, "")
        Prefs.getInstance().writeString(Prefs.DISPLAY_NAME, "")
        Prefs.getInstance().writeString(Prefs.USER_ID, "")
        Prefs.getInstance().writeString(Prefs.USERNAME, "")
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("currentFragment", currentFragment)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Prefs.LOCATION_REQUEST_CODE) {
            if (permissions.size == 1 && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION ) {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyApplication.instance.activateStoredCallback(true, this)
                    toast("Location permission granted")
                }else{
                    // check if user checked never show again
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        Prefs.getInstance().writeBoolean(Prefs.LOCATION_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                    }
                    MyApplication.instance.activateStoredCallback(false, this)
                    toast("Location permission not granted")
                }
            }
        }
    }
}
