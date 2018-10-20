package io.replicants.instaclone.adapters

import android.app.Activity
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.realm.Realm
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.BaseMainFragment
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.Comment
import io.replicants.instaclone.pojos.SavedPhoto
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.subfragments.CommentsSubFragment
import io.replicants.instaclone.utilities.*
import kotlinx.android.synthetic.main.adapter_comment_item.view.*
import java.util.ArrayList
import kotlinx.android.synthetic.main.adapter_userlist_item.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import org.json.JSONObject

open class CommentsAdapter(private val context: Activity, private val photoID:String, private val dataset: ArrayList<Comment?>, private val recyclerView: RecyclerView, private val changeListener:CommentsSubFragment.Listener?=null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var clickListeners: BaseMainFragment.ClickListeners? = null
    var onLoadMoreListener: CommentsAdapter.OnLoadMoreListener? = null
    var currentlyLoading = false
    var canLoadMore = true
    var llm: LinearLayoutManager

    private val visibleThreshold = 6
    val VIEW_TYPE_COMMENT = 1
    val VIEW_TYPE_LOADING = 3

    inner class CommentHolder(v: View) : RecyclerView.ViewHolder(v) {
        val profileImage = v.adapter_comment_item_image
        val text = v.adapter_comment_item_text
        init {
            text.movementMethod = LinkMovementMethod.getInstance()
        }

    }

    inner class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v)

    init {
        llm = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                loadMore()
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == VIEW_TYPE_LOADING){
            ProgressViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_loading, parent, false))
        }else {
            CommentHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_comment_item, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(dataset[position]==null) VIEW_TYPE_LOADING else VIEW_TYPE_COMMENT
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder is CommentHolder) {
            val commentItem = dataset[position]!!
            val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
            Glide.with(context)
                    .load(GlideHeader.getUrlWithHeaders(commentItem.profileImage))
                    .apply(requestOptions)
                    .into(holder.profileImage)
            val text = SpannableStringBuilder()
            text.bold { append(commentItem.displayName) }
            val time = Utils.formatDateForActivity(commentItem.timestamp)
            text.append(" ${commentItem.text} \n$time")
                    .setColorSpan(time, color = ContextCompat.getColor(context, R.color.grey400))

            text.setClickableSpan(commentItem.displayName){
                clickListeners?.moveToProfileSubFragment(commentItem.displayName)
            }
            holder.text.text = text

            holder.text.onLongClick {
                handleLongClick(commentItem)
            }
            holder.itemView.onLongClick {
                handleLongClick(commentItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun loadMore(){
        if (!currentlyLoading && canLoadMore &&
                llm.itemCount <= llm.findLastVisibleItemPosition() + visibleThreshold) {
            currentlyLoading = true
            onLoadMoreListener?.onLoadMore()
        }
    }

    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    fun handleLongClick(comment:Comment?){
        if(comment?.displayName==Prefs.getInstance().readString(Prefs.DISPLAY_NAME, "")){
            AlertDialog.Builder(context).apply {
                setTitle("Delete Comment")
                setMessage("Are you sure you want to delete this comment?")
                setNegativeButton(R.string.cancel) { dialog, id ->

                }
                setPositiveButton(R.string.ok) { dialog, id ->
                    InstaApi.deleteComment(photoID,comment?.commentID).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                        override fun success(jsonResponse: JSONObject?) {
                            dataset.remove(comment)
                            notifyDataSetChanged()
                            changeListener?.commentsChanged(dataset.size)
                        }
                    }))
                }
                show()
            }
        }

    }

}