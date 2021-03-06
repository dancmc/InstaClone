package io.replicants.instaclone.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import io.replicants.instaclone.R
import io.replicants.instaclone.network.InstaApi
import io.replicants.instaclone.network.InstaApiCallback
import io.replicants.instaclone.pojos.*
import io.replicants.instaclone.subfragments.upload.pickphoto.GalleryPagerFragment
import kotlinx.coroutines.experimental.launch
import org.apache.commons.io.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Utils {

    companion object {

        @JvmField
        val colorBlack = Color.parseColor("#ff000000")
        @JvmField
        val sdfWithYr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        @JvmField
        val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        @JvmField
        val floatFormat = DecimalFormat("##0.0")
        @JvmField
        val oneDayMs = 1000 * 60 * 60 * 24
        @JvmField
        val oneWeekMs = 1000 * 60 * 60 * 24 * 7
        @JvmField
        val oneHrMs = 1000 * 60 * 60
        @JvmField
        val oneMinMs = 1000 * 60
        @JvmField
        val oneMonthMs = 1000L * 60 * 60 * 24 * 30
        @JvmField
        val oneYrMs = 1000L * 60 * 60 * 24 * 365


        // JSON METHODS

        @JvmStatic
        fun photoFromJson(jsonObject: JSONObject): Photo {
            val photo = Photo()
            photo.displayName = jsonObject.optString("display_name")
            photo.profileImage = jsonObject.optString("profile_image")
            photo.photoID = jsonObject.optString("photo_id")
            val urlObject = jsonObject.optJSONObject("url") ?: JSONObject()
            val regularUrlObject = urlObject.optJSONObject("regular") ?: JSONObject()
            val smallUrlObject = urlObject.optJSONObject("small") ?: JSONObject()
            val thumbUrlObject = urlObject.optJSONObject("thumb") ?: JSONObject()
            photo.regularUrl = regularUrlObject.optString("link")
            photo.smallUrl = smallUrlObject.optString("link")
            photo.thumbUrl = thumbUrlObject.optString("link")
            val previewCommentObject = jsonObject.optJSONObject("preview_comments") ?: JSONObject()
            val previewCommentsJsonArray = previewCommentObject.optJSONArray("preview_text")
                    ?: JSONArray()

            val previewCommentsList = ArrayList<Pair<String, String>>()
            for (i in 0 until previewCommentsJsonArray.length()) {
                val previewTextObject = previewCommentsJsonArray.optJSONObject(i)
                if (previewTextObject != null) {
                    val displayName = previewTextObject.optString("display_name")
                    val text = previewTextObject.optString("text")
                    previewCommentsList.add(Pair(displayName, text))
                }
            }
            photo.previewComments = previewCommentsList
            photo.totalComments = previewCommentObject.optInt("total_comments")
            photo.caption = jsonObject.optString("caption")
            photo.isLiked = jsonObject.optBoolean("is_liked")
            val previewLikesObject = jsonObject.optJSONObject("preview_likes") ?: JSONObject()
            val previewLikesJsonArray = previewLikesObject.optJSONArray("preview_names")
                    ?: JSONArray()

            val previewLikesList = ArrayList<String>()
            for (i in 0 until previewLikesJsonArray.length()) {
                val name = previewLikesJsonArray.getString(i)
                if (name != null) {
                    previewLikesList.add(name)
                }
            }
            photo.previewLikes = previewLikesList
            photo.totalLikes = previewLikesObject.optInt("total_likes")

            val locationObject = jsonObject.optJSONObject("location") ?: JSONObject()
            photo.locationName = locationObject.optString("location_name")
            photo.latitude = locationObject.optDouble("latitude")
            photo.longitude = locationObject.optDouble("longitude")
            photo.timestamp = jsonObject.optLong("timestamp")
            photo.distance = jsonObject.optDouble("distance", Double.MIN_VALUE)
            photo.thumbHeight = thumbUrlObject.optInt("height")
            photo.thumbWidth = thumbUrlObject.optInt("width")
            photo.smallHeight = smallUrlObject.optInt("height")
            photo.smallWidth = smallUrlObject.optInt("width")
            photo.regularHeight = regularUrlObject.optInt("height")
            photo.regularWidth = regularUrlObject.optInt("width")

            return photo
        }

        @JvmStatic
        fun photosFromJsonArray(array: JSONArray): ArrayList<Photo> {
            val results = ArrayList<Photo>()
            for (i in 0 until array.length()) {
                val photoOb = array.getJSONObject(i)
                if (photoOb != null) {
                    results.add(photoFromJson(photoOb))
                }
            }
            return results
        }

        @JvmStatic
        fun userFromJson(jsonObject: JSONObject): User {
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

            for (i in 0 until followingArray.length()) {
                val name = followingArray.optString(i)
                if (name.isNotBlank()) {
                    followingList.add(name)
                }
            }
            user.followingWhoFollow = followingList

            when{
                jsonObject.optBoolean("are_following")-> user.followStatusToThem = User.STATUS_FOLLOWING
                jsonObject.optBoolean("have_requested") ->user.followStatusToThem = User.STATUS_REQUESTED
                jsonObject.optBoolean("requested_them") ->user.followStatusToThem = User.STATUS_REQUESTED
            }

            user.reason = jsonObject.optString("reason")

            return user
        }

        @JvmStatic
        fun usersFromJsonArray(jsonArray: JSONArray): ArrayList<User> {
            val results = ArrayList<User>()
            for (i in 0 until jsonArray.length()) {
                val userOb = jsonArray.getJSONObject(i)
                if (userOb != null) {
                    results.add(userFromJson(userOb))
                }
            }
            return results
        }

        @JvmStatic
        fun commentFromJson(jsonObject: JSONObject): Comment {
            val comment = Comment()
            comment.commentID = jsonObject.optString("comment_id")
            comment.displayName = jsonObject.optString("display_name")
            comment.profileImage = jsonObject.optString("profile_image")
            comment.text = jsonObject.optString("text")
            comment.timestamp = jsonObject.optLong("timestamp")

            return comment
        }

        @JvmStatic
        fun commentsFromJsonArray(jsonArray: JSONArray): ArrayList<Comment> {
            val results = ArrayList<Comment>()
            for (i in 0 until jsonArray.length()) {
                val commentOb = jsonArray.getJSONObject(i)
                if (commentOb != null) {
                    results.add(commentFromJson(commentOb))
                }
            }
            return results
        }

        @JvmStatic
        fun likeFromJson(jsonObject: JSONObject): Like {
            val like = Like()
            like.displayName = jsonObject.optString("display_name")
            like.profileImage = jsonObject.optString("profile_image")
            like.profileName = jsonObject.optString("profile_name")
            like.areFollowing = jsonObject.optBoolean("are_following")
            like.timestamp = jsonObject.optLong("timestamp")

            return like
        }

        @JvmStatic
        fun likesFromJsonArray(jsonArray: JSONArray): ArrayList<Like> {
            val results = ArrayList<Like>()
            for (i in 0 until jsonArray.length()) {
                val likeOb = jsonArray.getJSONObject(i)
                if (likeOb != null) {
                    results.add(likeFromJson(likeOb))
                }
            }
            return results
        }

        // DISTANCE & DATE METHODS
        @JvmStatic
        fun distance(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
            val point1 = LatLng(lat1, long1)
            val point2 = LatLng(lat2, long2)
            return LatLngTool.distance(point1, point2, LengthUnit.KILOMETER)
        }

        @JvmStatic
        fun formatDistance(distanceInMetres: Double): String {
            val d = Math.round(distanceInMetres)
            return if (distanceInMetres <= 999) "$d m" else "${d / 1000} km"
        }

        @JvmStatic
        fun validateLatLng(latitude: Double, longitude: Double): Boolean {
            return latitude >= -90.0 && latitude <= 90.0 && longitude >= -180.0 && longitude <= 180.0
        }

        @JvmStatic
        fun formatDate(time: Long): String {
            val diff = System.currentTimeMillis() - time
            return when {
                diff < oneHrMs -> {
                    "${diff / oneMinMs} minutes ago"
                }
                diff < oneDayMs -> {
                    "${diff / oneHrMs} hours ago"
                }
                diff < oneWeekMs -> {
                    "${diff / oneDayMs} days ago"
                }
                else -> {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.timeInMillis = time
                    val timeYear = timeCalendar.get(Calendar.YEAR)
                    if (timeYear < currentYear) {
                        sdfWithYr.format(Date(time))
                    } else {
                        sdf.format(Date(time))
                    }
                }
            }
        }

        @JvmStatic
        fun formatDateForActivity(time: Long): String {
            val diff = System.currentTimeMillis() - time
            return when {
                diff < oneHrMs -> {
                    "${diff / oneMinMs}m"
                }
                diff < oneDayMs -> {
                    "${diff / oneHrMs}h"
                }
                diff < oneWeekMs -> {
                    "${diff / oneDayMs}d"
                }
                diff < oneMonthMs -> {
                    "${diff / oneWeekMs}w"
                }
                diff < oneYrMs -> {
                    "${diff / oneMonthMs}M"
                }
                else -> {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.timeInMillis = time
                    val timeYear = timeCalendar.get(Calendar.YEAR)
                    if (timeYear < currentYear) {
                        sdfWithYr.format(Date(time))
                    } else {
                        sdf.format(Date(time))
                    }
                }
            }
        }

        // MISC UTILITY METHODS

        // https://stackoverflow.com/questions/12891520/how-to-programmatically-change-contrast-of-a-bitmap-in-android
        @JvmStatic
        fun setBrightnessOnColorMatrix(cm: ColorMatrix, brightness: Int) {
            brightness.toFloat().let {
                cm.array[4] = it
                cm.array[9] = it
                cm.array[14] = it
            }
        }

        @JvmStatic
        fun setContrastOnColorMatrix(cm: ColorMatrix, oldContrast: Float, newContrast: Float) {
            val undoMatrix = ColorMatrix().apply {
                array[0] = 1 / oldContrast
                array[6] = 1 / oldContrast
                array[12] = 1 / oldContrast
            }
            val newMatrix = ColorMatrix().apply {
                array[0] = newContrast
                array[6] = newContrast
                array[12] = newContrast
            }

            cm.apply {
                postConcat(undoMatrix)
                postConcat(newMatrix)
            }

        }

        @JvmStatic
        fun getMappedImagePoints(matrix: Matrix, originalHeight: Int, originalWidth: Int): ArrayList<Pair<Float, Float>> {
            val result = ArrayList<Pair<Float, Float>>()
            val originalPoints = FloatArray(8)
            val mappedPoints = FloatArray(8)
            originalPoints[0] = 0f
            originalPoints[1] = 0f
            originalPoints[2] = originalWidth.toFloat()
            originalPoints[3] = 0f
            originalPoints[4] = originalWidth.toFloat()
            originalPoints[5] = originalHeight.toFloat()
            originalPoints[6] = 0f
            originalPoints[7] = originalHeight.toFloat()
            matrix.mapPoints(mappedPoints, originalPoints)

            (0..3).forEach {
                result.add(Pair(mappedPoints[it * 2], mappedPoints[it * 2 + 1]))
            }

            return result
        }

        @JvmStatic
        fun hideKeyboardFrom(context: Context, view: View) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

        @JvmStatic
        fun redirectToSettings(@StringRes title: Int, @StringRes text: Int, context: Context) {
            AlertDialog.Builder(context).apply {
                setTitle(title)
                setMessage(text)
                setNegativeButton(R.string.cancel) { dialog, id ->

                }
                setPositiveButton(R.string.go_to_settings) { dialog, id ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }
                show()
            }
        }

        @JvmStatic
        fun updateDetails(context: Context, success: () -> Unit, failure: (JSONObject?) -> Unit = {},networkFailure: (Int) -> Unit = {}) {
            InstaApi.getDetails().enqueue(InstaApi.generateCallback(context, object : InstaApiCallback() {
                override fun success(jsonResponse: JSONObject?) {

                    Prefs.getInstance().writeString(Prefs.USERNAME, jsonResponse?.optString("username"))
                    Prefs.getInstance().writeString(Prefs.USER_ID, jsonResponse?.optString("user_id"))
                    Prefs.getInstance().writeString(Prefs.DISPLAY_NAME, jsonResponse?.optString("display_name"))

                    val profileImageURL = jsonResponse?.optString("profile_image")
                    Prefs.getInstance().writeString(Prefs.PROFILE_IMAGE, profileImageURL)
                    profileImageURL?.let { im->
                        launch{saveProfileImageToFile(context, im)}
                    }

                    success.invoke()
                }

                override fun failure(context: Context?, jsonResponse: JSONObject?) {
                    super.failure(context, jsonResponse)
                    failure.invoke(jsonResponse)
                }

                override fun networkFailure(context: Context?, code:Int) {
                    super.networkFailure(context, code)
                    networkFailure.invoke(code)
                }
            }))

        }

        @JvmStatic
        fun getImageDirectories(context: Context?): ArrayList<GalleryPagerFragment.ImageDirectory> {
            val projection = arrayOf("DISTINCT " + MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val cursor = MediaStore.Images.Media.query(context?.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null)

            cursor.moveToFirst()
            val albumName = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val albumID = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)

            val directorySet = HashSet<GalleryPagerFragment.ImageDirectory>()
            do {
                directorySet.add(GalleryPagerFragment.ImageDirectory(cursor.getInt(albumID), cursor.getString(albumName)))
            }while (cursor.moveToNext())
            cursor.close()
            val resultList = ArrayList<GalleryPagerFragment.ImageDirectory>()
            resultList.addAll(directorySet)
            resultList.sortBy { it.albumName }
            resultList.add(0, GalleryPagerFragment.ImageDirectory(-128937, "All"))
            return resultList
        }

        @JvmStatic
        fun getDirectoryCursor(context: Context, imageDirectory: GalleryPagerFragment.ImageDirectory): Cursor {
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
            val cursor: Cursor
            if (imageDirectory.albumName == "All") {
                cursor = MediaStore.Images.Media.query(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            } else {
                cursor = MediaStore.Images.Media.query(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.BUCKET_ID + " = ?", arrayOf("${imageDirectory.id}"), MediaStore.Images.Media.DATE_TAKEN + " DESC");
            }
            return cursor
        }

        @JvmStatic
        @Synchronized
        fun saveProfileImageToFile(context: Context,url:String){

            try {
                FileUtils.copyInputStreamToFile(URL(url).openConnection().apply {
                    setRequestProperty("Authorization", Prefs.getInstance().readString(Prefs.JWT,""))
                }.getInputStream(), File(context.filesDir, "profile.jpg"))
            }catch (e:Exception){
                println(e.message)
            }
        }


        @JvmStatic
        fun inrangeToPhoto(context: Context,inRangePhoto: InRangePhoto):Photo{
            val photo = Photo()
            photo.inRange = true
            photo.photoID = inRangePhoto.photoID

            photo.displayName = inRangePhoto.displayName
            photo.caption = inRangePhoto.caption
            photo.locationName = inRangePhoto.locationName
            photo.latitude = inRangePhoto.latitude
            photo.longitude = inRangePhoto.longitude
            photo.timestamp = inRangePhoto.timestamp
            photo.regularWidth = inRangePhoto.regularWidth
            photo.regularHeight = inRangePhoto.regularHeight
            val folder = File(context.filesDir, "inRange")
            val photoFile = File(folder, "${photo.photoID}.jpg")
            val profileFile = File(folder, "${photo.photoID}-profile.jpg")
            photo.profileImage = profileFile.absolutePath
            photo.regularUrl = photoFile.absolutePath
            photo.smallWidth = inRangePhoto.regularWidth
            photo.smallHeight = inRangePhoto.regularHeight
            photo.smallUrl = photoFile.absolutePath
            photo.thumbWidth = inRangePhoto.regularWidth
            photo.thumbHeight = inRangePhoto.regularHeight
            photo.thumbUrl = photoFile.absolutePath

            return photo
        }
    }


}

fun SpannableStringBuilder.setClickableSpan(textToClick: String, index: Int = -1, color: Int = Utils.colorBlack, onClickListener: () -> Unit): SpannableStringBuilder {
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View?) = onClickListener.invoke()
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = color

        }
    }
    val start = if (index != -1) index else indexOf(textToClick)
    setSpan(clickableSpan,
            start,
            start + textToClick.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}

fun SpannableStringBuilder.setColorSpan(textToColor: String, index: Int = -1, color: Int = Utils.colorBlack): SpannableStringBuilder {
    val colorSpan = ForegroundColorSpan(color)
    val start = if (index != -1) index else indexOf(textToColor)
    setSpan(colorSpan,
            start,
            start + textToColor.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}