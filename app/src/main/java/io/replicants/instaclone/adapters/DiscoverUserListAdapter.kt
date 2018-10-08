package io.replicants.instaclone.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.User
import java.util.ArrayList

class DiscoverUserListAdapter(private val context: Activity, private val dataset: ArrayList<User?>, private val recyclerView: RecyclerView):UserListAdapter(context, dataset, recyclerView) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == VIEW_TYPE_LOADING){
            ProgressViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_suggested, parent, false) as LinearLayout)
        }else {
            UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_userlist_item, parent, false))
        }
    }
}