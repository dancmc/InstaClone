package io.replicants.instaclone.subfragments

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ProfileSubFragment : BaseSubFragment() {

    companion object {

        @JvmStatic
        fun newInstance(displayName:String): ProfileSubFragment {
            val myFragment = ProfileSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putString("displayName", displayName)

            return myFragment
        }
    }

    var layout :LinearLayout? = null
    var toolbar:Toolbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.subfragment_profile, container, false) as LinearLayout
        val displayName = arguments?.getString("displayName")?:""

        val recyclerView = (layout?.subfragment_profile_recyclerview)!!
        if(toolbar!=null){
            layout?.addView(toolbar, 0)
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(40)
        recyclerView.setDrawingCacheEnabled(true)

        // use a linear layout manager
        val linearLayoutManager = LinearLayoutManager(activity)
        val gridLayoutManager = GridLayoutManager(activity, 3)
        gridLayoutManager.spanSizeLookup = object:GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return if(position==0) 3 else 1
            }
        }
        recyclerView.layoutManager = gridLayoutManager


        // initialise adapter with the item list, attach adapter to recyclerview
        // list initially empty
        val feedItems = ArrayList<Photo?>()
        val profileHeader = ProfileHeader(context!!)
        profileHeader.init(displayName)
        profileHeader.listButtonsCallback = object:ProfileHeader.ListButtonsCallback{
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
        profileHeader.clickListeners = clickListeners

        val adapter = FeedAdapter(activity!!,feedItems , recyclerView)
        adapter.header = profileHeader.view
        adapter.clickListeners = clickListeners
        recyclerView.adapter = adapter

        val sort = Prefs.getInstance().readString(Prefs.FEED_SORT, InstaApi.Sort.DATE.toString())
        if(sort == InstaApi.Sort.DATE.toString()) {
            InstaApi.getUserPhotos(displayName, InstaApi.Sort.DATE, null,null,null)
                    .enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                        override fun success(jsonResponse: JSONObject) {
                            val results = Utils.photosFromJsonArray(jsonResponse.optJSONArray("photos")?: JSONArray())
                            feedItems.clear()
                            feedItems.addAll(results)
                            adapter.notifyDataSetChanged()
                        }
                    }))
        } else {
            MyApplication.instance.getLocation(activity as AppCompatActivity, object:LocationCallback{
                override fun execute(location: Location) {
                    InstaApi.getUserPhotos(displayName, InstaApi.Sort.LOCATION, location.latitude,location.longitude,null)
                            .enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                                override fun success(jsonResponse: JSONObject) {
                                    val results = Utils.photosFromJsonArray(jsonResponse.optJSONArray("photos")?: JSONArray())
                                    feedItems.clear()
                                    feedItems.addAll(results)
                                    adapter.notifyDataSetChanged()
                                }
                            }))
                }

                override fun permissionFailed() {
                    context?.toast("Failed to get location")
                }
            })
        }

        return layout
    }

    fun changeToolbar(toolbar:Toolbar){
        this.toolbar = toolbar
        if(layout?.getChildAt(0) is Toolbar){
            layout?.removeViewAt(0)
        }
        layout?.addView(toolbar, 0)
    }
}