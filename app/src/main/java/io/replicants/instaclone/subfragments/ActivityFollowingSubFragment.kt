package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.ActivityFollowingAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.ActivityBase
import io.replicants.instaclone.pojos.ActivityFollowing1
import io.replicants.instaclone.pojos.ActivityFollowing2
import io.replicants.instaclone.pojos.ActivityFollowing3
import kotlinx.android.synthetic.main.subfragment_activity_following.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject

class ActivityFollowingSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(): ActivityFollowingSubFragment {
            val myFragment = ActivityFollowingSubFragment()

            val args = Bundle()
            myFragment.arguments = args

            return myFragment
        }
    }

    lateinit var layout:View
    lateinit var recyclerView:RecyclerView
    lateinit var adapter:ActivityFollowingAdapter
    private val activityItems = ArrayList<ActivityBase>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        if(!this::layout.isInitialized) {
         layout= inflater.inflate(R.layout.subfragment_activity_following, container, false)

        recyclerView = layout.subfragment_activity_following_recycler
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)

        // use a linear layout manager
        recyclerView.layoutManager = LinearLayoutManager(activity)


        // initialise cursorAdapter with the item list, attach cursorAdapter to recyclerview
        // list initially empty
        adapter = ActivityFollowingAdapter(activity!!, activityItems, recyclerView)
        adapter.clickListeners = clickListeners
        recyclerView.adapter = adapter

        layout.subfragment_activity_following_refresh.setOnRefreshListener {
            initialLoad()
        }

        initialLoad()
        }

        return layout
    }

    private fun initialLoad(){
        InstaApi.getFollowingActivity().enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
            override fun success(jsonResponse: JSONObject?) {
                doAsync {
                    val activityArray = jsonResponse?.optJSONArray("activities")?: JSONArray()
                    val processedItems = processJsonArray(activityArray)
                    uiThread {
                        activityItems.clear()
                        activityItems.addAll(processedItems)
                        adapter.notifyDataSetChanged()
                        layout.subfragment_activity_following_refresh.isRefreshing = false
                    }
                }
            }
        }))
    }

    private fun processJsonArray(jsonArray:JSONArray):ArrayList<ActivityBase>{
        val list = ArrayList<ActivityBase>()
        for (i in 0 until jsonArray.length()){
            val activityObject = jsonArray.getJSONObject(i)
            val type = activityObject.getInt("type")
            when(type){
                1->list.add(ActivityFollowing1.fromJson(activityObject))
                2->list.add(ActivityFollowing2.fromJson(activityObject))
                3->list.add(ActivityFollowing3.fromJson(activityObject))
            }
        }

        return list
    }
}