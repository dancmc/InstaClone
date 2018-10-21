package io.replicants.instaclone.adapters

import android.app.Activity
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.BaseMainFragment
import io.replicants.instaclone.pojos.ActivityBase
import io.replicants.instaclone.pojos.ActivityFollowing1
import io.replicants.instaclone.pojos.ActivityFollowing2
import io.replicants.instaclone.pojos.ActivityFollowing3
import io.replicants.instaclone.utilities.GlideHeader
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.utilities.setClickableSpan
import io.replicants.instaclone.utilities.setColorSpan
import kotlinx.android.synthetic.main.adapter_activityfollowing1.view.*
import kotlinx.android.synthetic.main.adapter_activityfollowing2.view.*
import kotlinx.android.synthetic.main.adapter_activityfollowing3.view.*
import org.jetbrains.anko.clickable
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

// Adapter for viewpager tab showing activity of users you follow
class ActivityFollowingAdapter(private val context: Activity, private val dataset: ArrayList<ActivityBase>, private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var clickListeners: BaseMainFragment.ClickListeners? = null

    // Types are defined in our API Specification
    private val ACTIVITY_TYPE_1 = 1 // when one person likes multiple photos
    private val ACTIVITY_TYPE_2 = 2 // when multiple people like one photo
    private val ACTIVITY_TYPE_3 = 3 // when a user starts following other users

    private inner class ActivityFollowingHolder1(v: View) : RecyclerView.ViewHolder(v) {

        val ivProfileImage = v.activityfollowing1_profile_head
        val tvText = v.activityfollowing1_text
        val llFirstImageRow = v.activity_following1_firstimagerow
        val llSecondImageRow = v.activity_following1_secondimagerow
        val imageList = arrayOf(
                v.activity_following1_image1,
                v.activity_following1_image2,
                v.activity_following1_image3,
                v.activity_following1_image4,
                v.activity_following1_image5,
                v.activity_following1_image6,
                v.activity_following1_image7,
                v.activity_following1_image8,
                v.activity_following1_image9,
                v.activity_following1_image10,
                v.activity_following1_image11,
                v.activity_following1_image12

        )
        init {
            tvText.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private inner class ActivityFollowingHolder2(v: View) : RecyclerView.ViewHolder(v) {

        val ivProfileImage = v.activityfollowing2_profile_head
        val tvText = v.activityfollowing2_text
        val ivImage = v.activity_following2_image
        init {
            tvText.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private inner class ActivityFollowingHolder3(v: View) : RecyclerView.ViewHolder(v) {

        val ivProfileImage = v.activityfollowing3_profile_head
        val tvText = v.activityfollowing3_text
        init {
            tvText.movementMethod = LinkMovementMethod.getInstance()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ACTIVITY_TYPE_1 -> ActivityFollowingHolder1(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activityfollowing1, parent, false))
            ACTIVITY_TYPE_2 -> ActivityFollowingHolder2(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activityfollowing2, parent, false))
            else -> ActivityFollowingHolder3(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activityfollowing3, parent, false))
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position]) {
            is ActivityFollowing1 -> ACTIVITY_TYPE_1
            is ActivityFollowing2 -> ACTIVITY_TYPE_2
            else -> ACTIVITY_TYPE_3
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ActivityFollowingHolder1 -> {

                val item = dataset[position] as ActivityFollowing1

                // load the profile image
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.profileImage))
                        .apply(requestOptions)
                        .into(holder.ivProfileImage)

                // construct the text with clickable links
                val time = Utils.formatDateForActivity(item.timestamp)
                val textBuilder = SpannableStringBuilder()
                        .bold { append(item.displayName) }
                        .append(" liked ${item.totalLiked} posts. $time")
                        .setColorSpan(time, color = ContextCompat.getColor(context, R.color.grey400))
                        .setClickableSpan(item.displayName) {
                            clickListeners?.moveToProfileSubFragment(item.displayName)
                        }
                holder.tvText.text = textBuilder


                // load photos with clickable links
                if (item.photosLiked.size > 6) {
                    holder.llSecondImageRow.visibility = View.VISIBLE
                    holder.llSecondImageRow.onClick {
                        clickListeners?.moveToPhotoSpecificSubFragment(item.photosLiked.mapTo(ArrayList<String>()) { it.photoID })
                    }

                } else {
                    holder.llSecondImageRow.visibility = View.GONE
                    holder.llSecondImageRow.onClick { }
                }

                holder.llFirstImageRow.onClick {
                    clickListeners?.moveToPhotoSpecificSubFragment(item.photosLiked.mapTo(ArrayList<String>()) { it.photoID })
                }

                holder.imageList.forEachIndexed { index, aspectImageView ->
                    if(index<item.photosLiked.size) {
                        Glide.with(context)
                                .load(GlideHeader.getUrlWithHeaders(item.photosLiked[index].thumbUrl))
                                .into(aspectImageView)
                    } else {
                        Glide.with(context).clear(aspectImageView)
                    }
                }

            }
            is ActivityFollowingHolder2 -> {
                val item = dataset[position] as ActivityFollowing2

                // load profile image
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.previewUsers[0].profileImage))
                        .apply(requestOptions)
                        .into(holder.ivProfileImage)
                val textBuilder = SpannableStringBuilder()

                // construct text with links
                item.previewUsers.forEachIndexed { index, user ->
                    when (index) {
                        0 -> textBuilder.bold { append(user.displayName) }
                        item.previewUsers.size - 1 -> {
                            if (item.totalLiked == item.previewUsers.size) {
                                textBuilder.append(" and ")
                            } else {
                                textBuilder.append(", ")
                            }
                            textBuilder.bold { append(user.displayName) }
                        }
                        else -> {
                            textBuilder.append(", ")
                            textBuilder.bold { append(user.displayName) }
                        }
                    }
                }
                val time = Utils.formatDateForActivity(item.timestamp)
                if (item.totalLiked > item.previewUsers.size) {
                    val num = item.totalLiked - item.previewUsers.size
                    textBuilder.append(" and ")
                    textBuilder.bold { append("$num ${if (num == 1) "other" else "others"}") }
                }
                textBuilder.append(" liked a photo. $time")
                        .setColorSpan(time, color = ContextCompat.getColor(context, R.color.grey400))

                item.previewUsers.forEach{
                    textBuilder.setClickableSpan(it.displayName){
                        clickListeners?.moveToProfileSubFragment(it.displayName)
                    }
                }

                holder.tvText.text = textBuilder


                // load photo with link
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.photo.thumbUrl))
                        .into(holder.ivImage)

                holder.ivImage.onClick {
                    clickListeners?.moveToPhotoSpecificSubFragment(arrayListOf(item.photo.photoID))
                }
            }
            is ActivityFollowingHolder3 -> {
                val item = dataset[position] as ActivityFollowing3

                // load profile image
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.profileImage))
                        .apply(requestOptions)
                        .into(holder.ivProfileImage)

                // construct text with links
                val textBuilder = SpannableStringBuilder()
                        .bold { append(item.displayName) }
                        .append(" started following ")
                        .bold { append(item.usersFollowed[0].displayName) }

                var link = item.usersFollowed[0].displayName

                if (item.usersFollowed.size != 1) {
                    val remaining = item.usersFollowed.size - 1
                    val others = "$remaining ${if (remaining == 1) "other" else "others"}"

                    textBuilder.append(" and ")
                    textBuilder.bold { append(others) }

                    link += " and $others"
                }
                val time = Utils.formatDateForActivity(item.timestamp)
                textBuilder.append(". $time")
                textBuilder.setColorSpan(time, color = ContextCompat.getColor(context, R.color.grey400))

                textBuilder.setClickableSpan(item.displayName){
                    clickListeners?.moveToProfileSubFragment(item.displayName)
                }

                textBuilder.setClickableSpan(link){
                    clickListeners?.moveToUserListSubFragmentWithList(item.usersFollowed, "${item.usersFollowed.size} users")
                }
                holder.tvText.text = textBuilder

            }
        }


    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}