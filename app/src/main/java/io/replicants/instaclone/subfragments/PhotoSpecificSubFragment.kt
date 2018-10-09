package io.replicants.instaclone.subfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.adapters.FeedAdapter
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.Photo
import io.replicants.instaclone.utilities.Utils
import kotlinx.android.synthetic.main.subfragment_photo_specific.view.*
import org.json.JSONArray
import org.json.JSONObject

class PhotoSpecificSubFragment : BaseSubFragment() {

    lateinit var recyclerView:RecyclerView
    lateinit var layoutManager:LinearLayoutManager
    lateinit var adapter:FeedAdapter
    var photoList = ArrayList<Photo?>()

    companion object {

        @JvmStatic
        fun newInstance(photoIDs:ArrayList<String>): PhotoSpecificSubFragment {
            val myFragment = PhotoSpecificSubFragment()

            val args = Bundle()
            myFragment.arguments = args
            args.putStringArrayList("photoIDs", photoIDs)

            return myFragment
        }
    }

    lateinit var layout:View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(!this::layout.isInitialized) {
            layout = inflater.inflate(R.layout.subfragment_photo_specific, container, false)

            val argsList = arguments?.getStringArrayList("photoIDs") ?: ArrayList<String>()

            // deal with the feed list
            recyclerView = layout.subfragment_photo_specific_recycler

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(40)
            recyclerView.setDrawingCacheEnabled(true)

            // use a linear layout manager
            layoutManager = LinearLayoutManager(activity)
            recyclerView.layoutManager = layoutManager


            // initialise cursorAdapter with the item list, attach cursorAdapter to recyclerview
            // list initially empty
            adapter = FeedAdapter(activity!!, photoList, recyclerView)
            adapter.clickListeners = clickListeners
            recyclerView.adapter = adapter

            InstaApi.specificPhotos(argsList).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                override fun success(jsonResponse: JSONObject?) {
                    val list = Utils.photosFromJsonArray(jsonResponse?.optJSONArray("photos")
                            ?: JSONArray())
                    photoList.addAll(list)
                    adapter.notifyDataSetChanged()
                }
            }))

            layout.subfragment_photo_specific_toolbar.title = "${argsList.size} photo${if(argsList.size>1)"s" else ""}"

        }
        return layout
    }


}