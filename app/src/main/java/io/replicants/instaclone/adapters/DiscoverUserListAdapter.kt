package io.replicants.instaclone.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import io.replicants.instaclone.R
import io.replicants.instaclone.pojos.User
import java.util.*

// Extension of UserListAdapter specifically for suggested users list
class DiscoverUserListAdapter(context: Activity,dataset: ArrayList<User?>, recyclerView: RecyclerView):UserListAdapter(context, dataset, recyclerView) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        // A bit of a hack to define a "Suggested" Header for the recyclerview since you can't load more suggested users
        // May as well double up the loading viewholder as a header instead
        return if(viewType == VIEW_TYPE_LOADING){
            ProgressViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_suggested, parent, false) as LinearLayout)
        }else {
            UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_userlist_item, parent, false))
        }
    }
}