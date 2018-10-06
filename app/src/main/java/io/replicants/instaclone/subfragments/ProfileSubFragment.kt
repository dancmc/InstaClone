package io.replicants.instaclone.subfragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.Photo
import io.replicants.instaclone.utilities.LocationCallback
import io.replicants.instaclone.utilities.MyApplication
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.views.ProfileHeader
import kotlinx.android.synthetic.main.subfragment_profile.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ProfileSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(displayName: String): ProfileSubFragment {
            val myFragment = ProfileSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("displayName", displayName)
            args.putBoolean("self", Prefs.getInstance().readString(Prefs.DISPLAY_NAME, "") == displayName)

            return myFragment
        }
    }

    lateinit var layout: LinearLayout
    var toolbar: Toolbar? = null
    var profileHeader: ProfileHeader? = null
    lateinit var displayName: String
    val feedItems = ArrayList<Photo?>()
    lateinit var adapter: FeedAdapter
    var self = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!this::layout.isInitialized) {
            layout = inflater.inflate(R.layout.subfragment_profile, container, false) as LinearLayout
            displayName = arguments?.getString("displayName") ?: ""
            self = arguments?.getBoolean("self") ?: false

            profileHeader = ProfileHeader(context!!)
            val recyclerView = (layout.subfragment_profile_recyclerview)!!
            if (toolbar != null) {
                layout.addView(toolbar, 0)
            }

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(40)
            recyclerView.setDrawingCacheEnabled(true)

            // use a linear layout manager
            val linearLayoutManager = LinearLayoutManager(activity)
            val gridLayoutManager = GridLayoutManager(activity, 3)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0) 3 else 1
                }
            }
            recyclerView.layoutManager = gridLayoutManager

            // initialise adapter with the item list, attach adapter to recyclerview
            // list initially empty
            adapter = FeedAdapter(activity!!, feedItems, recyclerView)
            adapter.header = profileHeader?.view
            adapter.clickListeners = clickListeners
            recyclerView.adapter = adapter

            // setup the  listeners to switch between grid and linear views
            profileHeader?.listButtonsCallback = object : ProfileHeader.ListButtonsCallback {
                override fun onGridClicked() {
                    val oldManager = recyclerView.layoutManager
                    val firstItemView = oldManager!!.findViewByPosition(0)
                    val topOffset = firstItemView!!.top
                    recyclerView.layoutManager = gridLayoutManager
                    (recyclerView.layoutManager as GridLayoutManager).scrollToPositionWithOffset(0, topOffset)
                }

                override fun onListClicked() {
                    val oldManager = recyclerView.layoutManager
                    val firstItemView = oldManager!!.findViewByPosition(0)
                    val topOffset = firstItemView!!.top
                    recyclerView.layoutManager = linearLayoutManager
                    (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, topOffset)
                }
            }
            profileHeader?.clickListeners = clickListeners

            // setup swipe refresh
            layout.subfragment_profile_refresh.setOnRefreshListener {
                load()
            }


            // finally load everything from api
            load()
        }
        return layout
    }

    fun load(withSelfUpdate: Boolean = true) {

        fun internalLoad() {
            loadHeader()

            val sort = Prefs.getInstance().readString(Prefs.FEED_SORT, InstaApi.Sort.DATE.toString())
            if (sort == InstaApi.Sort.DATE.toString()) {
                layout.subfragment_profile_button_request_location.visibility = View.GONE
                InstaApi.getUserPhotos(displayName, InstaApi.Sort.DATE, null, null, null)
                        .enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                            override fun success(jsonResponse: JSONObject) {
                                val results = Utils.photosFromJsonArray(jsonResponse.optJSONArray("photos")
                                        ?: JSONArray())
                                feedItems.clear()
                                feedItems.addAll(results)
                                adapter.notifyDataSetChanged()
                                layout.subfragment_profile_refresh.isRefreshing = false
                            }

                            override fun failure(context: Context?, jsonResponse: JSONObject?) {
                                super.failure(context, jsonResponse)
                                layout.subfragment_profile_refresh.isRefreshing = false
                            }

                            override fun networkFailure(context: Context?) {
                                super.networkFailure(context)
                                layout.subfragment_profile_refresh.isRefreshing = false
                            }
                        }))
            } else {
                MyApplication.instance.getLocation(activity as AppCompatActivity, object : LocationCallback {
                    override fun execute(location: Location) {
                        layout.subfragment_profile_button_request_location.visibility = View.GONE
                        InstaApi.getUserPhotos(displayName, InstaApi.Sort.LOCATION, location.latitude, location.longitude, null)
                                .enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                                    override fun success(jsonResponse: JSONObject) {
                                        val results = Utils.photosFromJsonArray(jsonResponse.optJSONArray("photos")
                                                ?: JSONArray())
                                        feedItems.clear()
                                        feedItems.addAll(results)
                                        adapter.notifyDataSetChanged()
                                        layout.subfragment_profile_refresh.isRefreshing = false
                                    }

                                    override fun failure(context: Context?, jsonResponse: JSONObject?) {
                                        super.failure(context, jsonResponse)
                                        layout.subfragment_profile_refresh.isRefreshing = false
                                    }

                                    override fun networkFailure(context: Context?) {
                                        super.networkFailure(context)
                                        layout.subfragment_profile_refresh.isRefreshing = false
                                    }
                                }))
                    }

                    override fun permissionFailed() {
                        layout.subfragment_profile_button_request_location.visibility = View.VISIBLE
                        layout.subfragment_profile_button_request_location.onClick {
                            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    Prefs.getInstance().readBoolean(Prefs.LOCATION_DENIED_FOREVER, false)) {
                                context?.let { c ->
                                    Utils.redirectToSettings(R.string.request_location_title, R.string.request_location_text, c)
                                }
                            } else {
                                load()
                            }
                        }
                        context?.toast("Failed to get location")

                        layout.subfragment_profile_refresh.isRefreshing = false
                    }
                })

            }
        }

        if (self && withSelfUpdate) {
            Utils.updateDetails(context!!) {
                displayName = Prefs.getInstance().readString(Prefs.DISPLAY_NAME, "")
                toolbar?.title = displayName
                internalLoad()
            }
        } else {
            internalLoad()
        }
    }

    fun changeToolbar(toolbar: Toolbar) {
        this.toolbar = toolbar
        if (layout.getChildAt(0) is Toolbar) {
            layout.removeViewAt(0)
        }
        layout.addView(toolbar, 0)
    }

    fun loadHeader() {
        profileHeader?.init(displayName)
    }

    override fun onResume() {
        super.onResume()

        if (self) {
            val retrievedDisplayName = Prefs.getInstance().readString(Prefs.DISPLAY_NAME, "")
            if (displayName != retrievedDisplayName) {
                displayName = retrievedDisplayName
                toolbar?.title = displayName
                load(false)
            }
        }
    }
}