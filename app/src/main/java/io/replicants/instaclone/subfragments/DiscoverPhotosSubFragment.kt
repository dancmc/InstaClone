package io.replicants.instaclone.subfragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.Photo
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_discover_photos.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.json.JSONArray
import org.json.JSONObject

class DiscoverPhotosSubFragment : BaseSubFragment() {


    companion object {

        @JvmStatic
        fun newInstance(): DiscoverPhotosSubFragment {
            val myFragment = DiscoverPhotosSubFragment()

            val args = Bundle()
            myFragment.arguments = args


            return myFragment
        }

    }


    lateinit var layout: View
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: FeedAdapter
    private val feedItems = ArrayList<Photo?>()
    private var seed = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if (this::layout.isInitialized) {
            return layout
        }
        layout = inflater.inflate(R.layout.subfragment_discover_photos, container, false)
        recyclerView = layout.subfragment_discover_photos_recycler
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)

        val layoutManager = GridLayoutManager(activity, 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 3 else 1
            }
        }
        recyclerView.layoutManager = layoutManager
        adapter = FeedAdapter(activity!!, feedItems, recyclerView)
        adapter.clickListeners = clickListeners
        recyclerView.adapter = adapter

        layout.subfragment_discover_photos_refresh.setOnRefreshListener {
            initialLoad()
        }
        layout.subfragment_discover_photos_refresh.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark)

        initialLoad()

        adapter.onLoadMoreListener = object : FeedAdapter.OnLoadMoreListener {
            override fun onLoadMore() {

                recyclerView.post {
                    feedItems.add(null)
                    adapter.notifyItemInserted(feedItems.lastIndex)

                    InstaApi.discoverPhotos(seed).enqueue(InstaApi.generateCallback(activity, loadMoreApiCallback()))

                }

            }
        }

        layout.subfragment_discover_photos_toolbar.onClick {
            clickListeners?.moveToDiscoverUsersSubFragment()
        }


        return layout

    }

    private fun initialLoad() {
        if (!layout.subfragment_discover_photos_refresh.isRefreshing) {
            layout.subfragment_discover_photos_refresh.isRefreshing = true
        }
        adapter.currentlyLoading = true
        feedItems.clear()
        adapter.notifyDataSetChanged()

        InstaApi.discoverPhotos(null).enqueue(InstaApi.generateCallback(activity, initialApiCallback()))
    }

    private fun initialApiCallback(): InstaApiCallback {
        return object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                val photoArray = jsonResponse?.optJSONArray("photos") ?: JSONArray()
                seed = jsonResponse?.optString("seed") ?: ""
                val photoList: List<Photo> = Utils.photosFromJsonArray(photoArray)

                feedItems.addAll(photoList)
                adapter.currentlyLoading = false
                adapter.canLoadMore = true
                adapter.notifyDataSetChanged()

                layout.subfragment_discover_photos_refresh.isRefreshing = false
            }

            override fun failure(context: Context, jsonResponse: JSONObject?) {
                super.failure(context, jsonResponse)
                layout.subfragment_discover_photos_refresh.isRefreshing = false
            }

            override fun networkFailure(context: Context?,code:Int) {
                super.networkFailure(context,code)
                layout.subfragment_discover_photos_refresh.isRefreshing = false
            }
        }
    }

    private fun loadMoreApiCallback(): InstaApiCallback {
        return object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject?) {
                seed = jsonResponse?.optString("seed") ?: ""
                val photoArray = jsonResponse?.optJSONArray("photos") ?: JSONArray()
                val photoList: List<Photo> = Utils.photosFromJsonArray(photoArray)

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

            override fun networkFailure(context: Context,code:Int) {
                super.networkFailure(context,code)
                feedItems.removeAt(feedItems.lastIndex)
                adapter.notifyDataSetChanged()
                adapter.currentlyLoading = false
            }
        }
    }

}