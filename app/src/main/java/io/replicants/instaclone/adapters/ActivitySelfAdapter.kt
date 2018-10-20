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
import io.replicants.instaclone.pojos.*
import io.replicants.instaclone.utilities.GlideHeader
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.utilities.setClickableSpan
import io.replicants.instaclone.utilities.setColorSpan
import kotlinx.android.synthetic.main.adapter_activityself1.view.*
import kotlinx.android.synthetic.main.adapter_activityself2.view.*
import kotlinx.android.synthetic.main.adapter_activityself3.view.*
import kotlinx.android.synthetic.main.adapter_activityself4.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class ActivitySelfAdapter(private val context: Activity, private val dataset: ArrayList<ActivityBase>, private val recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var clickListeners: BaseMainFragment.ClickListeners? = null

    private val ACTIVITY_TYPE_1 = 1
    private val ACTIVITY_TYPE_2 = 2
    private val ACTIVITY_TYPE_3 = 3
    private val ACTIVITY_TYPE_4 = 4

    private inner class ActivitySelfHolder1(v: View) : RecyclerView.ViewHolder(v) {

        val ivProfileImage = v.activityself1_profile_head
        val tvText = v.activityself1_text
        init {
            tvText.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private inner class ActivitySelfHolder2(v: View) : RecyclerView.ViewHolder(v) {

        val ivProfileImage = v.activityself2_profile_head
        val tvText = v.activityself2_text
        val ivImage = v.activityself2_image
        init {
            tvText.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private inner class ActivitySelfHolder3(v: View) : RecyclerView.ViewHolder(v) {

        val ivProfileImage = v.activityself3_profile_head
        val tvText = v.activityself3_text
        val ivImage = v.activityself3_image
        init {
            tvText.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private inner class ActivitySelfHolder4(v: View) : RecyclerView.ViewHolder(v) {

        val layout = v.activityself4_layout
        val ivProfileImage = v.activityself4_profile_head
        val tvText = v.activityself4_text

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ACTIVITY_TYPE_1 -> ActivitySelfHolder1(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activityself1, parent, false))
            ACTIVITY_TYPE_2 -> ActivitySelfHolder2(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activityself2, parent, false))
            ACTIVITY_TYPE_3 -> ActivitySelfHolder3(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activityself3, parent, false))
            else -> ActivitySelfHolder4(LayoutInflater.from(parent.context).inflate(R.layout.adapter_activityself4, parent, false))
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position]) {
            is ActivitySelf1 -> ACTIVITY_TYPE_1
            is ActivitySelf2 -> ACTIVITY_TYPE_2
            is ActivitySelf3 -> ACTIVITY_TYPE_3
            else -> ACTIVITY_TYPE_4
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ActivitySelfHolder1 -> {
                val item = dataset[position] as ActivitySelf1
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.users[0].profileImage))
                        .apply(requestOptions)
                        .into(holder.ivProfileImage)
                val textBuilder = SpannableStringBuilder()
                val previewUsers = item.users.take(3)
                previewUsers.forEachIndexed { index, user ->
                    when (index) {
                        0 -> textBuilder.bold { append(user.displayName) }
                        item.users.size - 1 -> {
                            if (item.users.size == previewUsers.size) {
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
                var others = ""
                if (item.users.size > previewUsers.size) {
                    val num = item.users.size - previewUsers.size
                    others = "$num ${if (num == 1) "other" else "others"}"
                    textBuilder.append(" and ")
                    textBuilder.bold { append(others) }
                }
                textBuilder.append(" started following you. $time")
                        .setColorSpan(time, color = ContextCompat.getColor(context, R.color.grey400))

                previewUsers.forEach{
                    textBuilder.setClickableSpan(it.displayName){
                        clickListeners?.moveToProfileSubFragment(it.displayName)
                    }
                }

                if(others.isNotBlank()) {
                    textBuilder.setClickableSpan(others) {
                        clickListeners?.moveToUserListSubFragmentWithList(item.users, "${item.users.size} users")
                    }
                }

                holder.tvText.text = textBuilder



            }
            is ActivitySelfHolder2 -> {
                val item = dataset[position] as ActivitySelf2
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.previewUsers[0].profileImage))
                        .apply(requestOptions)
                        .into(holder.ivProfileImage)
                val textBuilder = SpannableStringBuilder()

                item.previewUsers.forEachIndexed { index, user ->
                    when (index) {
                        0 -> textBuilder.bold { append(user.displayName) }
                        item.previewUsers.size - 1 -> {
                            if (item.recentLikes == item.previewUsers.size) {
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
                var others = ""
                if (item.recentLikes > item.previewUsers.size) {
                    val num = item.recentLikes - item.previewUsers.size
                    others = "$num ${if (num == 1) "other" else "others"}"
                    textBuilder.append(" and ")
                    textBuilder.bold { append(others) }
                }
                textBuilder.append(" liked your photo. $time")
                        .setColorSpan(time, color = ContextCompat.getColor(context, R.color.grey400))

                item.previewUsers.forEach{
                    textBuilder.setClickableSpan(it.displayName){
                        clickListeners?.moveToProfileSubFragment(it.displayName)
                    }
                }

                if(others.isNotBlank()) {
                    textBuilder.setClickableSpan(others) {
                        clickListeners?.moveToPhotoSpecificSubFragment(arrayListOf(item.photo.photoID))
                    }
                }

                holder.tvText.text = textBuilder

                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.photo.thumbUrl))
                        .into(holder.ivImage)

                holder.ivImage.onClick {
                    clickListeners?.moveToPhotoSpecificSubFragment(arrayListOf(item.photo.photoID))
                }
            }
            is ActivitySelfHolder3 -> {
                val item = dataset[position] as ActivitySelf3
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.previewComment.profileImage))
                        .apply(requestOptions)
                        .into(holder.ivProfileImage)
                val textBuilder = SpannableStringBuilder()
                textBuilder.bold { append(item.previewComment.displayName) }


                val time = Utils.formatDateForActivity(item.timestamp)
                var others = ""
                if (item.recentComments > 1) {
                    val num = item.recentComments - 1
                    others = "$num ${if (num == 1) "other" else "others"}"
                    textBuilder.append(" and ")
                    textBuilder.bold { append(others) }
                }
                textBuilder.append(" commented on your photo. $time\n${item.previewComment.text}")
                        .setColorSpan(time, color = ContextCompat.getColor(context, R.color.grey400))

                textBuilder.setClickableSpan(item.previewComment.displayName){
                    clickListeners?.moveToProfileSubFragment(item.previewComment.displayName)
                }

                if(others.isNotBlank()) {
                    textBuilder.setClickableSpan(others) {
                        clickListeners?.moveToPhotoSpecificSubFragment(arrayListOf(item.photo.photoID))
                    }
                }

                holder.tvText.text = textBuilder

                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.photo.thumbUrl))
                        .into(holder.ivImage)

                holder.ivImage.onClick {
                    clickListeners?.moveToPhotoSpecificSubFragment(arrayListOf(item.photo.photoID))
                }
            }
            is ActivitySelfHolder4 -> {
                val item = dataset[position] as ActivitySelf4
                val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))
                Glide.with(context)
                        .load(GlideHeader.getUrlWithHeaders(item.requests[0].profileImage))
                        .apply(requestOptions)
                        .into(holder.ivProfileImage)
                val textBuilder = SpannableStringBuilder()
                val previewUsers = item.requests.take(3)
                previewUsers.forEachIndexed { index, user ->
                    when (index) {
                        0 -> textBuilder.bold { append(user.displayName) }
                        item.requests.size - 1 -> {
                            if (item.requests.size == previewUsers.size) {
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
                if (item.requests.size > previewUsers.size) {
                    val num = item.requests.size - previewUsers.size
                    textBuilder.append(" and ")
                    textBuilder.bold { append("$num ${if (num == 1) "other" else "others"}") }
                }
                textBuilder.append(" requested to follow you.")

                holder.tvText.text = textBuilder

                holder.layout.onClick {
                    clickListeners?.moveToApproveListSubFragment(item.requests)
                }

            }
        }


    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}