package io.replicants.instaclone.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints.TAG
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.utilities.Photo
import io.replicants.instaclone.utilities.setClickableSpan
import java.util.ArrayList
import kotlinx.android.synthetic.main.feed_item.view.*
import kotlinx.android.synthetic.main.feed_loading.view.*
import org.json.JSONObject

class FeedAdapter(private val context: Activity, private val dataset: ArrayList<Photo?>, private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onLoadMoreListener : OnLoadMoreListener? = null
    var currentlyLoading = false
    var canLoadMore = true

    private val visibleThreshold = 5
    private val VIEW_TYPE_PHOTO = 1
    private val VIEW_TYPE_LOADING = 2


    // this inner class doesn't need to be static since we never use it outside of FeedAdapter
    inner class PhotoViewHolder(v: ConstraintLayout) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var ivProfileHead = v.feed_item_profile_head
        var tvProfileName = v.feed_item_profile_name
        var tvLocationName = v.feed_item_location_name
        var ivPhoto = v.feed_item_image
        var progressBar = v.feed_item_loading
        var btLike = v.feed_item_like
        var tvLikes = v.feed_item_like_text

        init {

            tvProfileName.movementMethod = LinkMovementMethod.getInstance()
            tvLocationName.movementMethod = LinkMovementMethod.getInstance()
            tvLikes.movementMethod = LinkMovementMethod.getInstance()

            @Suppress
            ivPhoto.setOnTouchListener(object : View.OnTouchListener {
                private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        val item = dataset[adapterPosition]
                        handleLike(btLike, item!!)
                        return super.onDoubleTap(e)
                    }
                })

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    gestureDetector.onTouchEvent(event)
                    return true
                }
            })

            btLike.setOnClickListener {
                val item = dataset[adapterPosition]
                handleLike(btLike, item!!)
            }

        }
    }

    inner class ProgressViewHolder(v: LinearLayout) : RecyclerView.ViewHolder(v)

    fun handleLike(button: Button, item:Photo){
        toggleLike(button, item)

        if(item.isLiked){
            InstaApi.likePhoto(item.photoID, InstaApi.generateCallback(context, object:InstaApiCallback(){
                override fun success(jsonResponse: JSONObject) {
                    if (!jsonResponse.optBoolean("success")){
                        toggleLike(button, item)
                    }
                }
                override fun failure(jsonResponse: JSONObject?) = toggleLike( button, item)

            }))
        } else {
            InstaApi.unlikePhoto(item.photoID, InstaApi.generateCallback(context, object:InstaApiCallback(){
                override fun success(jsonResponse: JSONObject) {
                    if (!jsonResponse.optBoolean("success")){
                        toggleLike(button, item)
                    }
                }
                override fun failure(jsonResponse: JSONObject?) = toggleLike(button, item)
            }))
        }
    }

    fun toggleLike(button:Button, photo:Photo){
        photo.isLiked = !photo.isLiked
        button.isSelected = photo.isLiked
    }

    init {

        if(recyclerView.layoutManager is LinearLayoutManager){
            val llm  = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if(!currentlyLoading && canLoadMore &&
                            llm.itemCount<=llm.findLastVisibleItemPosition() + visibleThreshold){
                        currentlyLoading = true
                        onLoadMoreListener?.onLoadMore()
                    }
                }
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(dataset.get(position)==null) VIEW_TYPE_LOADING else VIEW_TYPE_PHOTO
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType==VIEW_TYPE_PHOTO) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false) as ConstraintLayout
            return PhotoViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.feed_loading, parent, false) as LinearLayout
            return ProgressViewHolder(v)
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder is PhotoViewHolder) {
            val feedItem = dataset[position]!!

            val image = holder.ivPhoto
            image.setAspectRatio(feedItem.regularHeight / feedItem.regularWidth.toFloat())

            holder.progressBar.visibility = View.VISIBLE

            val displayNameBuilder = SpannableStringBuilder()
                    .bold { append(feedItem.displayName) }
                    .setClickableSpan(feedItem.displayName, 0) {
                        //TODO show profile
                        Toast.makeText(context, "Will show profile", Toast.LENGTH_SHORT).show()
                    }

            holder.tvProfileName.text = displayNameBuilder

            if (feedItem.locationName.isNotBlank()) {
                holder.tvLocationName.visibility = View.VISIBLE
                val locationNameBuilder = SpannableStringBuilder()
                        .append(feedItem.locationName)
                        .setClickableSpan(feedItem.locationName, 0) {
                            //TODO show location
                            Toast.makeText(context, "Will show map", Toast.LENGTH_SHORT).show()
                        }
                holder.tvLocationName.text = locationNameBuilder
            } else {
                holder.tvLocationName.visibility = View.GONE
            }

            holder.btLike.isSelected = feedItem.isLiked

            val likeBuilder = SpannableStringBuilder()

            if (feedItem.previewLikes.isNotEmpty()) {

                val joinedLikes = feedItem.previewLikes.joinToString(", ")
                likeBuilder.append("Liked by ")
                        .bold { append(joinedLikes) }
                val remaining = feedItem.totalLikes - feedItem.previewLikes.size
                if (remaining > 0) {
                    likeBuilder.append(" and ")
                            .bold { append("$remaining ${if (remaining > 1) "others" else "other"}") }
                }
                feedItem.previewLikes.forEach {
                    likeBuilder.setClickableSpan(it) {
                        //TODO show profile
                        Toast.makeText(context, "Will show profile", Toast.LENGTH_SHORT).show()
                    }
                }
                likeBuilder.setClickableSpan(likeBuilder.toString().substringAfter(" and ")) {
                    //TODO show likes
                    Toast.makeText(context, "Will show likes", Toast.LENGTH_SHORT).show()
                }

            } else {
                if (feedItem.totalLikes > 0) {
                    likeBuilder.bold { append("${feedItem.totalLikes} ${if (feedItem.totalLikes > 1) "likes" else "like"}") }
                }

                likeBuilder.setClickableSpan(likeBuilder.toString()) {
                    //TODO show likes
                    Log.d(TAG, "likes")
                    Toast.makeText(context, "Will show likes", Toast.LENGTH_SHORT).show()
                }
            }
            holder.tvLikes.text = likeBuilder

            Glide.with(context)
                    .load(feedItem.profileImage)
//                .placeholder(R.drawable.icon_android)
                    .into(holder.ivProfileHead)


            if (feedItem.regularUrl.isNotBlank()) {
                Glide.with(context)
                        .load(feedItem.regularUrl)
//                    .placeholder(R.drawable.icon_placeholder)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                holder.progressBar.visibility = View.GONE
                                return false
                            }
                        })
                        .into(holder.ivPhoto)
            } else {
                Glide.with(context).clear(holder.ivPhoto)
                holder.ivPhoto.setImageDrawable(null)
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataset.size
    }


    interface OnLoadMoreListener{
        fun onLoadMore()
    }
}