package io.replicants.instaclone.views

import android.app.Activity
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.text.bold
import com.bumptech.glide.Glide
import io.replicants.instaclone.R
import io.replicants.instaclone.maintabs.BaseMainFragment
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.User
import io.replicants.instaclone.subfragments.UserListSubFragment
import io.replicants.instaclone.utilities.GlideHeader
import io.replicants.instaclone.utilities.Prefs
import io.replicants.instaclone.utilities.Utils
import io.replicants.instaclone.utilities.setClickableSpan
import kotlinx.android.synthetic.main.view_profile_header.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import org.json.JSONObject

class ProfileHeader(val context: Context) {

    var view :View = LayoutInflater.from(context).inflate(R.layout.view_profile_header, null, false)
    var user:User = User()
    var listButtonsCallback:ListButtonsCallback? = null
    var clickListeners:BaseMainFragment.ClickListeners? = null

    init {
        view.view_profile_header_others_following.movementMethod = LinkMovementMethod.getInstance()
    }

    fun init(displayName :String){

        val isSelf = Prefs.getInstance().readString(Prefs.DISPLAY_NAME, "") == displayName

        InstaApi.getUserInfo(displayName).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
            override fun success(jsonResponse: JSONObject) {

                user= Utils.userFromJson(jsonResponse)
                view.view_profile_header_post_text.text = user.numberPosts.toString()
                view.view_profile_header_followers_text.text = user.followers.toString()
                view.view_profile_header_followers_text.onClick {
                    clickListeners?.moveToUserListSubFragmentWithCall(UserListSubFragment.CallType.FOLLOWERS, displayName)
                }
                view.view_profile_header_c2.onClick {
                    clickListeners?.moveToUserListSubFragmentWithCall(UserListSubFragment.CallType.FOLLOWERS, displayName)
                }
                view.view_profile_header_following_text.text = user.following.toString()
                view.view_profile_header_following_text.onClick {
                    clickListeners?.moveToUserListSubFragmentWithCall(UserListSubFragment.CallType.FOLLOWING, displayName)
                }
                view.view_profile_header_c3.onClick {
                    clickListeners?.moveToUserListSubFragmentWithCall(UserListSubFragment.CallType.FOLLOWING, displayName)
                }

                Glide.with(context).load(GlideHeader.getUrlWithHeaders(user.profileImage)).into(view.view_profile_header_image)


                // 0 not following, 1 following, 2 requested
                if(isSelf){
                    view.view_profile_header_approve_request.visibility = View.GONE
                    view.view_profile_header_follow_btn.text = "Edit Profile"
                    view.view_profile_header_follow_btn.onClick {
                        clickListeners?.moveToEditProfileSubFragment(displayName)
                    }
                }else {
                    if (user.followStatusToMe == 2) {
                        view.view_profile_header_approve_request.visibility = View.VISIBLE
                        view.view_profile_header_approve_request.onClick {
                            InstaApi.approveUser(displayName).enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                                override fun success(jsonResponse: JSONObject?) {
                                    view.view_profile_header_approve_request.text = "Approved"
                                    view.view_profile_header_approve_request.onClick { }
                                }
                            }))
                        }
                    } else {
                        view.view_profile_header_approve_request.visibility = View.GONE
                    }

                    when (user.followStatusToThem) {
                        0 -> {
                            view.view_profile_header_follow_btn.text = if (user.isPrivate) "Request" else if (user.followStatusToMe == 1) "Follow Back" else "Follow"
                            view.view_profile_header_follow_btn.onClick {
                                follow()
                            }
                        }
                        1 -> {
                            view.view_profile_header_follow_btn.text = "Following"
                            view.view_profile_header_follow_btn.onClick {
                                unfollow()
                            }
                        }
                        2 -> {
                            view.view_profile_header_follow_btn.text = "Requested"
                            view.view_profile_header_follow_btn.onClick {
                                unfollow()
                            }
                        }
                    }
                }

                view.view_profile_header_profile_name.setText(user.profileName, TextView.BufferType.NORMAL)
                view.view_profile_header_desc.setText(user.profileDesc, TextView.BufferType.NORMAL)
                if(user.followingWhoFollow.size>0) {
                    view.view_profile_header_others_following.visibility = View.VISIBLE
                    val previewUsers = user.followingWhoFollow.take(3)
                    val remaining = user.followingWhoFollow.size - previewUsers.size
                    val followingFollowingText = SpannableStringBuilder()
                            .append("Followed by ")
                            .bold { append(previewUsers.joinToString(", ")) }
                    if (remaining>0){
                        followingFollowingText.append(" + ")
                        followingFollowingText.bold { append("$remaining more") }
                        followingFollowingText.setClickableSpan("$remaining more"){
                            clickListeners?.moveToUserListSubFragmentWithCall(UserListSubFragment.CallType.FOLLOWINGWHOFOLLOW, displayName)
                        }
                    }
                    previewUsers.forEach {
                        followingFollowingText.setClickableSpan(it){
                            clickListeners?.moveToProfileSubFragment(it)
                        }
                    }



                    view.view_profile_header_others_following.setText(followingFollowingText, TextView.BufferType.NORMAL)
                } else {
                    view.view_profile_header_others_following.visibility = View.GONE
                }
            }

            override fun failure(context:Context, jsonResponse: JSONObject?) {
                super.failure(context, jsonResponse)
                clear()
            }

            override fun networkFailure(context: Context?) {
                super.networkFailure(context)
                clear()
            }
        }))
        view.view_profile_header_grid_btn.onClick { listButtonsCallback?.onGridClicked() }
        view.view_profile_header_list_btn.onClick { listButtonsCallback?.onListClicked() }
    }

    fun clear(){
        view.view_profile_header_post_text.text = "-"
        view.view_profile_header_followers_text.text = "-"
        view.view_profile_header_followers_text.onClick {  }
        view.view_profile_header_c2.onClick {  }
        view.view_profile_header_following_text.text = "-"
        view.view_profile_header_following_text.onClick {  }
        view.view_profile_header_c3.onClick {  }
        Glide.with(context).clear(view.view_profile_header_image)
        view.view_profile_header_follow_btn.text = "-"
        view.view_profile_header_follow_btn.onClick {  }
        view.view_profile_header_profile_name.setText("", TextView.BufferType.NORMAL)
        view.view_profile_header_desc.setText("", TextView.BufferType.NORMAL)
        view.view_profile_header_others_following.setText("", TextView.BufferType.NORMAL)
        view.view_profile_header_approve_request.visibility = View.GONE
        view.view_profile_header_approve_request.onClick {  }
        view.view_profile_header_grid_btn.onClick {  }
        view.view_profile_header_list_btn.onClick {  }
    }

    fun unfollow(){
        context.alert("Are you sure you want to ${if(user.followStatusToThem == 2)"unrequest" else "unfollow"}?") {
            yesButton {
                InstaApi.unfollowUser(user.displayName).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
                    override fun success(jsonResponse: JSONObject?) {
                        view.view_profile_header_follow_btn.text = if(user.isPrivate) "Request" else if(user.followStatusToMe == 1)"Follow Back" else "Follow"
                        view.view_profile_header_follow_btn.onClick {
                            follow()
                        }
                    }
                }))
            }
            noButton { }
        }.show()
    }

    fun follow(){
        InstaApi.followUser(user.displayName).enqueue(InstaApi.generateCallback(context, object:InstaApiCallback(){
            override fun success(jsonResponse: JSONObject) {
                val response  = jsonResponse.optInt("result", -1)

                 when(response){
                    0,2-> view.view_profile_header_follow_btn.text = "Following"
                    1,3-> view.view_profile_header_follow_btn.text = "Requested"
                }
                view.view_profile_header_follow_btn.onClick {
                    unfollow()
                }
            }
        }))
    }

    interface ListButtonsCallback{
        fun onGridClicked()
        fun onListClicked()
    }

}