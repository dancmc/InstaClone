package io.replicants.instaclone.adapters

import android.app.Activity
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.BaseMainFragment
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.utilities.GlideHeader
import io.replicants.instaclone.utilities.Prefs
import kotlinx.android.synthetic.main.adapter_userlist_item.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.yesButton
import org.json.JSONObject
import java.util.*


// Adapter for displaying lists of users, with links to profile page and buttons to follow
open class UserListAdapter(private val context: Activity, private val dataset: ArrayList<User?>,
                           recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var clickListeners: BaseMainFragment.ClickListeners? = null
    var onLoadMoreListener: UserListAdapter.OnLoadMoreListener? = null
    var currentlyLoading = false
    var canLoadMore = true

    private val visibleThreshold = 6
    val VIEW_TYPE_USER = 1
    val VIEW_TYPE_LOADING = 3

    inner class UserHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivProfileImage = v.userlist_item_profile_head
        val tvDisplayName = v.userlist_item_displayname
        val tvProfileName = v.userlist_item_profile_name
        val btnFollow = v.userlist_item_follow_button
        val layout = v.userlist_layout
    }
    inner class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v)

    init {
        val llm = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!currentlyLoading && canLoadMore &&
                        llm.itemCount <= llm.findLastVisibleItemPosition() + visibleThreshold) {
                    currentlyLoading = true
                    onLoadMoreListener?.onLoadMore()
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == VIEW_TYPE_LOADING){
            ProgressViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_loading, parent, false))
        }else {
            UserHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_userlist_item, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(dataset[position]==null) VIEW_TYPE_LOADING else VIEW_TYPE_USER
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder is UserHolder) {
            val userItem = dataset[position]
            val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
            Glide.with(context)
                    .load(GlideHeader.getUrlWithHeaders(userItem?.profileImage))
                    .apply(requestOptions)
                    .into(holder.ivProfileImage)
            holder.tvDisplayName.text = SpannableStringBuilder()
                    .bold { append(userItem?.displayName) }
            holder.tvProfileName.text = userItem?.profileName

            val self = Prefs.getInstance().readString(Prefs.DISPLAY_NAME,"")
            if (self == userItem?.displayName) {
                holder.btnFollow.visibility = View.GONE
            } else {
                holder.btnFollow.visibility = View.VISIBLE
                when(userItem?.followStatusToThem)  {
                     0 -> {
                        holder.btnFollow.text = "Follow"
                        holder.btnFollow.onClick {
                            follow(holder.btnFollow, userItem)
                        }
                    }
                    1 -> {
                        holder.btnFollow.text = "Following"
                        holder.btnFollow.onClick {
                            unfollow(holder.btnFollow, userItem)
                        }
                    }
                    2 -> {
                        holder.btnFollow.text = "Requested"
                        holder.btnFollow.onClick {
                            unfollow(holder.btnFollow, userItem)
                        }
                    }
                }

            }
            holder.layout.onClick {
                clickListeners?.moveToProfileSubFragment(userItem?.displayName)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun unfollow(btn: Button, user: User) {
        context.alert("Are you sure you want to ${if (user.followStatusToThem == 2) "unrequest" else "unfollow"}?") {
            yesButton {
                InstaApi.unfollowUser(user.displayName).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                    override fun success(jsonResponse: JSONObject?) {
                        user.followStatusToThem = User.STATUS_NOT_FOLLOWING
                        btn.text = if (user.isPrivate) "Request" else "Follow"
                        btn.onClick {
                            follow(btn, user)
                        }
                    }
                }))
            }
            noButton { }
        }.show()
    }

    fun follow(btn: Button, user: User) {
        InstaApi.followUser(user.displayName).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
            override fun success(jsonResponse: JSONObject) {
                val response = jsonResponse.optInt("result", -1)
                when (response) {
                    0, 2 -> {
                        btn.text = "Following"
                        user.followStatusToThem = User.STATUS_FOLLOWING
                    }
                    1, 3 -> {
                        btn.text = "Requested"
                        user.followStatusToThem = User.STATUS_REQUESTED
                    }
                }
                btn.onClick {
                    unfollow(btn, user)
                }
            }
        }))
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

}