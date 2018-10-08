package io.replicants.instaclone.subfragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.Photo
import io.replicants.instaclone.utilities.*
import kotlinx.android.synthetic.main.subfragment_feed.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject


class FeedSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): FeedSubFragment {
            val myFragment = FeedSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }


    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: FeedAdapter
    lateinit var locationRequestButton: Button
    lateinit var layout: View


    var locationManager: LocationManager? = null
    var feedItems = ArrayList<Photo?>()
    var lastLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if(!this::layout.isInitialized) {
            layout = inflater.inflate(R.layout.subfragment_feed, container, false)

            locationRequestButton = layout.subfragment_feed_button_request_location
            locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

            // deal with toolbar
            layout.subfragment_feed_toolbar.inflateMenu(R.menu.menu_feed_fragment)
            layout.subfragment_feed_toolbar.setOnClickListener {
                if (adapter.itemCount > 0) {
                    recyclerView.scrollToPosition(0)
                }
            }


            layout.subfragment_feed_toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_wifi -> {
                        if (parentFragment is FeedFragmentInterface) {
                            (parentFragment as FeedFragmentInterface).moveToAdhoc()
                        }
                        true
                    }
                    R.id.action_settings -> {
                        if (parentFragment is FeedFragmentInterface) {
                            (parentFragment as FeedFragmentInterface).moveToSettings()
                        }
                        true
                    }
                    R.id.action_sort_date -> {
                        Prefs.getInstance().writeString(Prefs.FEED_SORT, InstaApi.Sort.DATE.toString())
                        initialLoad()
                        true
                    }
                    R.id.action_sort_location -> {
                        Prefs.getInstance().writeString(Prefs.FEED_SORT, InstaApi.Sort.LOCATION.toString())
                        initialLoad()
                        true
                    }
                    R.id.action_sort_grid -> {
                        layoutManager = GridLayoutManager(activity, 3);
                        (layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int {
                                return if (position == 0) 3 else 1
                            }
                        }
                        recyclerView.setLayoutManager(layoutManager)
                        true
                    }
                    R.id.action_linear -> {
                        layoutManager = LinearLayoutManager(activity)
                        recyclerView.setLayoutManager(layoutManager)
                        true
                    }
                    else -> {
                        true
                    }
                }
            }


            // deal with the feed list
            recyclerView = layout.subfragment_feed_recycler

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(40)
            recyclerView.setDrawingCacheEnabled(true)

            // use a linear layout manager
            layoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = layoutManager


            // initialise adapter with the item list, attach adapter to recyclerview
            // list initially empty
            adapter = FeedAdapter(activity!!, feedItems, recyclerView)
            adapter.clickListeners = clickListeners
            recyclerView.adapter = adapter


            // !! onRefresh never triggers if the top view is 0px.........
            layout.subfragment_feed_refresh.setOnRefreshListener {
                initialLoad()
            }
            layout.subfragment_feed_refresh.setColorSchemeResources(android.R.color.holo_green_dark,
                    android.R.color.holo_red_dark,
                    android.R.color.holo_blue_dark,
                    android.R.color.holo_orange_dark)


            // intial call - either date or location (default date)
            initialLoad()

            adapter.onLoadMoreListener = object : FeedAdapter.OnLoadMoreListener {
                override fun onLoadMore() {


                    recyclerView.post {
                        val lastPhotoID = if (feedItems.size > 0) feedItems.last()?.photoID else null
                        feedItems.add(null)
                        adapter.notifyItemInserted(feedItems.lastIndex)

                        if (Prefs.getInstance().readString(Prefs.FEED_SORT, InstaApi.Sort.DATE.toString()) == InstaApi.Sort.DATE.toString()) {
                            InstaApi.getFeed(InstaApi.Sort.DATE, null, null, lastPhotoID).enqueue(InstaApi.generateCallback(activity, loadMoreApiCallback()))

                        } else {
                            if (lastLocation?.longitude != null) {
                                InstaApi.getFeed(InstaApi.Sort.LOCATION, lastLocation?.latitude, lastLocation?.longitude, lastPhotoID).enqueue(InstaApi.generateCallback(activity, loadMoreApiCallback()))
                            } else {
                                adapter.currentlyLoading = false
                                adapter.canLoadMore = false
                                activity?.toast("Could not resolve previous location, please loadHeader feed")
                            }
                        }
                    }

                }
            }
        }

        return layout
    }

    private fun initialLoad() {
         locationRequestButton.visibility = View.GONE
        adapter.currentlyLoading = true
        feedItems.clear()
        adapter.notifyDataSetChanged()

        if (Prefs.getInstance().readString(Prefs.FEED_SORT,InstaApi.Sort.DATE.toString()) == InstaApi.Sort.DATE.toString()) {
            InstaApi.getFeed(InstaApi.Sort.DATE, null, null, null).enqueue(InstaApi.generateCallback(activity, initialApiCallback()))
        } else {
            loadLocation()
        }
    }


    private fun loadLocation() {
        MyApplication.instance.getLocation(activity as AppCompatActivity, object : LocationCallback {
            override fun execute(location: Location?) {
                locationRequestButton.visibility = View.GONE
                if (location?.longitude == null) {
                    activity?.toast("Failed to get location")
                } else {
                    lastLocation = location
                    InstaApi.getFeed(InstaApi.Sort.LOCATION, location.latitude, location.longitude, null).enqueue(InstaApi.generateCallback(activity, initialApiCallback()))
                }
            }

            override fun permissionFailed() {
                locationRequestButton.visibility = View.VISIBLE
                locationRequestButton.onClick {
                    if(ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            Prefs.getInstance().readBoolean(Prefs.LOCATION_DENIED_FOREVER, false)){
                        context?.let{ c ->
                            Utils.redirectToSettings(R.string.request_location_title, R.string.request_location_text, c)
                        }
                    } else {
                        loadLocation()
                    }

                }
            }
        })
    }

    private fun initialApiCallback(): InstaApiCallback {
        return object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                val photoArray = jsonResponse?.optJSONArray("photos") ?: JSONArray()
                var photoList :List<Photo> = Utils.photosFromJsonArray(photoArray)

                if (Prefs.getInstance().readString(Prefs.FEED_SORT,InstaApi.Sort.DATE.toString()) == InstaApi.Sort.LOCATION.toString()) {
                    photoList = photoList.filterNot { it.latitude == 999.0 || it.longitude == 999.0 }
                }

                feedItems.addAll(photoList)
                adapter.currentlyLoading = false
                adapter.canLoadMore = true
                adapter.notifyDataSetChanged()

                layout.subfragment_feed_refresh.isRefreshing = false
            }

            override fun failure(context: Context, jsonResponse: JSONObject?) {
                super.failure(context, jsonResponse)
                layout.subfragment_feed_refresh.isRefreshing = false
            }

            override fun networkFailure(context: Context?) {
                super.networkFailure(context)
                layout.subfragment_feed_refresh.isRefreshing = false
            }
        }
    }

    private fun loadMoreApiCallback(): InstaApiCallback {
        return object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                val photoArray = jsonResponse?.optJSONArray("photos") ?: JSONArray()
                var photoList:List<Photo> = Utils.photosFromJsonArray(photoArray)

                // remove photos with invalid longitudes and latitudes (no location data when uploaded
                if (Prefs.getInstance().readString(Prefs.FEED_SORT,InstaApi.Sort.DATE.toString()) == InstaApi.Sort.LOCATION.toString()) {
                    photoList = photoList.filterNot { it.latitude == 999.0 || it.longitude == 999.0 }
                }

                feedItems.removeAt(feedItems.lastIndex)
                feedItems.addAll(photoList)
                if (photoList.isEmpty()) {
                    adapter.canLoadMore = false
                }
                adapter.notifyDataSetChanged()
                adapter.currentlyLoading = false
            }

            override fun failure(context: Context, jsonResponse: JSONObject?) {
                super.failure(context, jsonResponse)
                feedItems.removeAt(feedItems.lastIndex)
                adapter.notifyDataSetChanged()
                adapter.currentlyLoading = false
            }

            override fun networkFailure(context: Context) {
                super.networkFailure(context)
                feedItems.removeAt(feedItems.lastIndex)
                adapter.notifyDataSetChanged()
                adapter.currentlyLoading = false
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_feed_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    interface FeedFragmentInterface {
        fun moveToSettings()
        fun moveToAdhoc()
    }
}