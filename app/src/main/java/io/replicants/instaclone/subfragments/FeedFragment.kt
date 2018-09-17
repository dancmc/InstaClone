package io.replicants.instaclone.subfragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.maintabs.HomeFragment
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.utilities.*
import kotlinx.android.synthetic.main.feed_item.*
import kotlinx.android.synthetic.main.subfragment_feed.view.*
import org.json.JSONArray
import org.json.JSONObject

class FeedFragment : Fragment() {

    companion object {

        fun newInstance(): FeedFragment {
            val myFragment = FeedFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var adapter: FeedAdapter
    var locationManager :LocationManager? = null
    var feedItems = ArrayList<Photo?>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.subfragment_feed, container, false)

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        // deal with toolbar
        layout.fragment_feed_toolbar.inflateMenu(R.menu.menu_feed_fragment)
        layout.fragment_feed_toolbar.setOnClickListener {
            if(adapter.itemCount>0) {
                // TODO find a way to scroll a bit more slowly
                recyclerView.scrollToPosition(0)
            }
        }
        layout.fragment_feed_toolbar.setOnMenuItemClickListener {
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
                R.id.action_sort_date->{
                    Prefs.getInstance().writeString(Prefs.FEED_SORT, InstaApi.Sort.DATE.toString())
                    initialLoad()
                    true
                }
                R.id.action_sort_location->{
                    Prefs.getInstance().writeString(Prefs.FEED_SORT, InstaApi.Sort.LOCATION.toString())
                    initialLoad()
                    true
                }
                else -> {
                    true
                }
            }
        }



        // deal with the feed list
        recyclerView = layout.fragment_feed_recycler

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)

        // use a linear layout manager
        layoutManager = LinearLayoutManager(activity)
        recyclerView.setLayoutManager(layoutManager)

        // initialise adapter with the item list, attach adapter to recyclerview
        // list initially empty
        adapter = FeedAdapter(activity!!, feedItems, recyclerView)
        recyclerView.adapter = adapter

        // intial call - either date or location (default date)
        initialLoad()


        adapter.onLoadMoreListener = object : FeedAdapter.OnLoadMoreListener {
            override fun onLoadMore() {


                recyclerView.post{
                    val lastPhotoID = if(feedItems.size>0)feedItems.last()?.photoID else null
                    feedItems.add(null)
                    adapter.notifyItemInserted(feedItems.lastIndex)

                    if (Prefs.getInstance().readString(Prefs.FEED_SORT, InstaApi.Sort.DATE.toString()) == InstaApi.Sort.DATE.toString()) {
                        InstaApi.getFeed(InstaApi.Sort.DATE, null, null, lastPhotoID, InstaApi.generateCallback(activity, object : InstaApiCallback() {
                            override fun success(jsonResponse: JSONObject?){
                                val photoArray = jsonResponse?.optJSONArray("photos") ?: JSONArray()
                                val photoList = Utils.photosFromJsonArray(photoArray)
                                feedItems.removeAt(feedItems.lastIndex)
                                feedItems.addAll(photoList)
                                if (photoList.size == 0) {
                                    adapter.canLoadMore = false
                                }
                                adapter.notifyDataSetChanged()
                                adapter.currentlyLoading = false
                            }
                            override fun failure(jsonResponse: JSONObject?){
                                val failureMessage = jsonResponse?.optString("error_message") ?: ""
                                if (failureMessage.isNotBlank()) {
                                    Toast.makeText(activity, failureMessage, Toast.LENGTH_SHORT).show()
                                }
                                feedItems.removeAt(feedItems.lastIndex)
                                adapter.notifyDataSetChanged()
                                adapter.currentlyLoading = false
                                // TODO consider displaying reload button
                            }

                            override fun networkFailure(context: Activity?) {
                                super.networkFailure(context)
                                feedItems.removeAt(feedItems.lastIndex)
                                adapter.notifyDataSetChanged()
                                adapter.currentlyLoading = false
                                // TODO consider displaying reload button
                            }
                        }))

                    } else {
                        // TODO get current location and load (DO NOT SET NEW LOCATION OTHERWISE SERVER RESULTS WILL NOT MAKE SENSE)

                    }
                }

            }
        }

        return layout
    }

    private fun initialLoad(){

        feedItems.clear()
        adapter.notifyDataSetChanged()

        if (Prefs.getInstance().readString(Prefs.FEED_SORT, InstaApi.Sort.DATE.toString()) == InstaApi.Sort.DATE.toString()) {

            InstaApi.getFeed(InstaApi.Sort.DATE, null, null, null, InstaApi.generateCallback(activity, object : InstaApiCallback() {
                override fun success(jsonResponse: JSONObject?){
                    val photoArray = jsonResponse?.optJSONArray("photos") ?: JSONArray()
                    val photoList = Utils.photosFromJsonArray(photoArray)
                    feedItems.addAll(photoList)
                    adapter.currentlyLoading = false
                    adapter.canLoadMore = true
                    adapter.notifyDataSetChanged()
                }
                override fun failure(jsonResponse: JSONObject?){
                    val failureMessage = jsonResponse?.optString("error_message") ?: ""
                    if (failureMessage.isNotBlank()) {
                        Toast.makeText(activity, failureMessage, Toast.LENGTH_SHORT).show()
                    }
                    // TODO consider displaying reload button
                }
            }))

        } else {
            // TODO set current location and load
            MyApplication.instance.getLocation(activity as AppCompatActivity, LocationCallback{

            })

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