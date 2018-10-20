package io.replicants.instaclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.*
import io.replicants.instaclone.network.InstaRetrofit
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.utilities.MyApplication
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.toast
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG_HOME = "home"
        private val TAG_DISCOVER = "discover"
        private val TAG_ACTIVITY = "activity"
        private val TAG_PROFILE = "profile"
        private val UPLOAD_PHOTO_REQUEST = 3123
    }


    private var backStack = ArrayList<String>()
    private var currentFragment = ""
    private var backPressed = false
    private var finishedUploading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        initialiseAndAuthorise(savedInstanceState)

        launch {
            cleanupStorage()
        }

    }

    private fun initialiseAndAuthorise(savedInstanceState: Bundle?){
        if (Prefs.getInstance().readString(Prefs.JWT, "").isBlank()) {
            logout()
        } else {
            if (savedInstanceState != null) {
                currentFragment = savedInstanceState.getString("currentFragment")
            } else {
                switchFragment(TAG_HOME)
            }

            Utils.updateDetails(this,
                    success = {},
                    failure = { responseJson ->
                        if (responseJson?.optInt("error_code", -1) ?: 0 == 0) {
                            logout()
                        }
                    },
                    networkFailure = { code->
                        if(code == 502) {
                            when(InstaRetrofit.domain){
                                "dancmc.io"->{
                                    InstaRetrofit.domain = "danielchan.io"
                                    InstaRetrofit.rebuild()
                                    toast("Switching to backup server")
                                    initialiseAndAuthorise(savedInstanceState)
                                }
                                "danielchan.io"->{
                                    toast("Both servers down")
                                }
                            }
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
            R.id.navigation_upload_photo -> {
                val intent = Intent(this, UploadPhotoActivity::class.java)
                startActivityForResult(intent, UPLOAD_PHOTO_REQUEST)
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

        if (currentFragment == target) {
            (supportFragmentManager.findFragmentByTag(currentFragment) as BaseMainFragment).clearBackStack()
        } else {

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
                }

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
                // give opportunity for mainfragment to consume back press
                if (frag is BaseMainFragment) {
                    if (frag.handleBackPress()) {
                        return
                    }
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
            if (permissions.size == 1 && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MyApplication.instance.activateStoredCallback(true, this)
                    toast("Location permission granted")
                } else {
                    // check if user checked never show again
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Prefs.getInstance().writeBoolean(Prefs.LOCATION_DENIED_FOREVER, !shouldShowRequestPermissionRationale(permissions[0]))
                    }
                    MyApplication.instance.activateStoredCallback(false, this)
                    toast("Location permission not granted")
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UPLOAD_PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {

            toast("Successfully uploaded photo!")
            finishedUploading = true


        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        if (finishedUploading) {
            switchFragment(TAG_PROFILE)
            finishedUploading = false

            (supportFragmentManager.findFragmentByTag(currentFragment) as? ProfileMainFragment?)?.showUploaded()
        }

    }

    private fun cleanupStorage() {
        val savedSet = HashSet<String>()
        Realm.getDefaultInstance().where(SavedPhoto::class.java).findAll().forEach {
            savedSet.add(it.photoFile)
            savedSet.add(it.photoFilePreview)
        }
        var folder = File(filesDir, "photos")
        if (folder.exists()) {
            folder.listFiles().forEach {
                if (!it.isDirectory && it.absolutePath !in savedSet && it.absolutePath.endsWith(".jpg")) {
                    it.delete()
                }
            }
        }
        folder = File(filesDir, "posting")
        if (folder.exists()) {
            folder.listFiles().forEach {
                it.delete()
            }
        }
    }
}
