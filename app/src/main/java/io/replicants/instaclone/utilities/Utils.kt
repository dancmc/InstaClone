package io.replicants.instaclone.utilities

import android.Manifest
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import org.json.JSONArray
import org.json.JSONObject
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import android.text.TextPaint
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.replicants.instaclone.R


class Utils {

    companion object {

        val colorBlack = Color.parseColor("#ff000000")

        fun distance(lat1:Double, long1:Double, lat2:Double, long2:Double):Double{
            val point1 = LatLng(lat1, long1)
            val point2 = LatLng(lat2, long2)
            return LatLngTool.distance(point1, point2, LengthUnit.KILOMETER)
        }

        fun photoFromJson(jsonObject:JSONObject):Photo{
            val photo = Photo()
            photo.displayName = jsonObject.optString("display_name")
            photo.profileImage = jsonObject.optString("profile_image")
            photo.photoID = jsonObject.optString("photo_id")
            val urlObject = jsonObject.optJSONObject("url") ?: JSONObject()
            val regularUrlObject = urlObject.optJSONObject("regular") ?: JSONObject()
            val smallUrlObject = urlObject.optJSONObject("small") ?: JSONObject()
            val thumbUrlObject = urlObject.optJSONObject("thumb") ?: JSONObject()
            photo.regularUrl = regularUrlObject.optString("link")
            photo.smallUrl =smallUrlObject.optString("link")
            photo.thumbUrl = thumbUrlObject.optString("link")
            val previewCommentObject = jsonObject.optJSONObject("preview_comments") ?: JSONObject()
            val previewCommentsJsonArray = previewCommentObject.optJSONArray("preview_text") ?: JSONArray()

            val previewCommentsList = ArrayList<Pair<String, String>>()
            for(i in 0 until previewCommentsJsonArray.length()){
                val previewTextObject = previewCommentsJsonArray.optJSONObject(i)
                if(previewTextObject!=null){
                    val displayName = previewCommentObject.optString("display_name")
                    val text = previewCommentObject.optString("text")
                    previewCommentsList.add(Pair(displayName, text))
                }
            }
            photo.previewComments = previewCommentsList
            photo.totalComments = previewCommentObject.optInt("total_comments")
            photo.caption = jsonObject.optString("caption")
            photo.isLiked = jsonObject.optBoolean("is_liked")
            val previewLikesObject = jsonObject.optJSONObject("preview_likes") ?: JSONObject()
            val previewLikesJsonArray = previewLikesObject.optJSONArray("preview_names") ?: JSONArray()

            val previewLikesList = ArrayList<String>()
            for(i in 0 until previewLikesJsonArray.length()){
                val name = previewLikesJsonArray.getString(i)
                if(name!=null){
                    previewLikesList.add(name)
                }
            }
            photo.previewLikes = previewLikesList
            photo.totalLikes = previewLikesObject.optInt("total_likes")

            val locationObject = jsonObject.optJSONObject("location") ?: JSONObject()
            photo.locationName = locationObject.optString("location_name")
            photo.latitude = locationObject.optDouble("latitude")
            photo.longitude = locationObject.optDouble("longitude")
            photo.timestamp = locationObject.optLong("timestamp")
            photo.distance = jsonObject.optDouble("distance", Double.MIN_VALUE)
            photo.thumbHeight =thumbUrlObject.optInt("height")
            photo.thumbWidth =thumbUrlObject.optInt("width")
            photo.smallHeight =smallUrlObject.optInt("height")
            photo.smallWidth =smallUrlObject.optInt("width")
            photo.regularHeight =regularUrlObject.optInt("height")
            photo.regularWidth =regularUrlObject.optInt("width")

            return photo
        }

        fun photosFromJsonArray(array:JSONArray):List<Photo>{
            val results = ArrayList<Photo>()
            for (i in 0 until array.length()){
                val photoOb = array.getJSONObject(i)
                if(photoOb!=null){
                    results.add(photoFromJson(photoOb))
                }
            }
            return results
        }

        fun userFromJson(jsonObject: JSONObject):User{
            val user = User()
            user.displayName = jsonObject.optString("display_name")
            user.numberPosts = jsonObject.optInt("number_posts")
            user.isPrivate = jsonObject.optBoolean("private")
            user.followers = jsonObject.optInt("followers")
            user.following = jsonObject.optInt("following")
            user.followStatusToThem = jsonObject.optInt("follow_status_to_them")
            user.followStatusToMe = jsonObject.optInt("follow_status_to_me")
            user.profileName = jsonObject.optString("profile_name")
            user.profileDesc = jsonObject.optString("profile_desc")
            user.profileImage = jsonObject.optString("profile_image")
            val followingArray = jsonObject.optJSONArray("following_who_follow") ?: JSONArray()
            val followingList = ArrayList<String>()

            for (i in 0 until followingArray.length()){
                val name = followingArray.optString(i)
                if(name.isNotBlank()){
                    followingList.add(name)
                }
            }
            user.followingWhoFollow = followingList
            user.areFollowing = jsonObject.optBoolean("are_following")
            user.reason = jsonObject.optString("reason")

            return user
        }

        fun usersFromJsonArray(jsonArray:JSONArray):ArrayList<User>{
            val results = ArrayList<User>()
            for (i in 0 until jsonArray.length()){
                val userOb = jsonArray.getJSONObject(i)
                if(userOb!=null){
                    results.add(userFromJson(userOb))
                }
            }
            return results
        }

        fun commentFromJson(jsonObject: JSONObject):Comment{
            val comment = Comment()
            comment.commentID = jsonObject.optString("comment_id")
            comment.displayName = jsonObject.optString("display_name")
            comment.profileImage = jsonObject.optString("profile_image")
            comment.text = jsonObject.optString("text")
            comment.timestamp = jsonObject.optLong("timestamp")

            return comment
        }

        fun commentsFromJsonArray(jsonArray:JSONArray):ArrayList<Comment>{
            val results = ArrayList<Comment>()
            for (i in 0 until jsonArray.length()){
                val commentOb = jsonArray.getJSONObject(i)
                if(commentOb!=null){
                    results.add(commentFromJson(commentOb))
                }
            }
            return results
        }

        fun likeFromJson(jsonObject: JSONObject):Like{
            val like = Like()
            like.displayName = jsonObject.optString("display_name")
            like.profileImage = jsonObject.optString("profile_image")
            like.profileName = jsonObject.optString("profile_name")
            like.areFollowing = jsonObject.optBoolean("are_following")
            like.timestamp = jsonObject.optLong("timestamp")

            return like
        }

        fun likesFromJsonArray(jsonArray:JSONArray):ArrayList<Like>{
            val results = ArrayList<Like>()
            for (i in 0 until jsonArray.length()){
                val likeOb = jsonArray.getJSONObject(i)
                if(likeOb!=null){
                    results.add(likeFromJson(likeOb))
                }
            }
            return results
        }

        fun hideKeyboardFrom(context: Context, view: View) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

        fun formatDistance(distanceInMetres:Double):String{
            val d = Math.round(distanceInMetres)
            return if(distanceInMetres<=999)"$d m" else "${d/1000} km"
        }
    }

}

fun SpannableStringBuilder.setClickableSpan(textToClick:String, index:Int=-1, color:Int = Utils.colorBlack, onClickListener : ()->Unit):SpannableStringBuilder{
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View?) = onClickListener.invoke()
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = color

        }
    }
    val start = if(index!=-1)index else indexOf(textToClick)
    setSpan(clickableSpan,
            start,
            start + textToClick.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}