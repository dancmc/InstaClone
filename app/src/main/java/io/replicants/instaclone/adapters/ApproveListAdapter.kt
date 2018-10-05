package io.replicants.instaclone.adapters

import android.app.Activity
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.BaseMainFragment
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.utilities.GlideHeader
import io.replicants.instaclone.utilities.Prefs
import java.util.ArrayList
import kotlinx.android.synthetic.main.adapter_userlist_item.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.yesButton
import org.json.JSONObject

class ApproveListAdapter(private val context: Activity, private val dataset: ArrayList<User?>, private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var clickListeners: BaseMainFragment.ClickListeners? = null


    private inner class UserHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivProfileImage = v.userlist_item_profile_head
        val tvDisplayName = v.userlist_item_displayname
        val tvProfileName = v.userlist_item_profile_name
        val btnFollow = v.userlist_item_follow_button
        val layout = v.userlist_layout
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_userlist_item, parent, false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val userItem = dataset[position]
        holder as UserHolder
        Glide.with(context).load(GlideHeader.getUrlWithHeaders(userItem?.profileImage)).into(holder.ivProfileImage)
        holder.tvDisplayName.text = SpannableStringBuilder()
                .bold { append(userItem?.displayName) }
        holder.tvProfileName.text = userItem?.profileName

        val followStatusToMe = userItem?.followStatusToMe
        if(followStatusToMe == User.STATUS_REQUESTED){
            holder.btnFollow.text = "Approve"
            holder.btnFollow.onClick {
                approve(holder.btnFollow, userItem)
            }
        } else {
            holder.btnFollow.text = "Approved"
            holder.btnFollow.onClick {
            }
        }

        holder.layout.onClick {
            clickListeners?.moveToProfileSubFragment(userItem?.displayName)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }



    private fun approve(btn: Button, user: User) {
        InstaApi.approveUser(user.displayName).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject) {

                dataset.find { it?.displayName == user.displayName}?.followStatusToMe = User.STATUS_FOLLOWING
                notifyDataSetChanged()
            }
        }))
    }


}