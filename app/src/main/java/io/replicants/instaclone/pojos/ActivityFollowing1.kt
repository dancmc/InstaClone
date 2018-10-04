package io.replicants.instaclone.pojos

import io.replicants.instaclone.utilities.Utils
import org.json.JSONArray
import org.json.JSONObject

class ActivityFollowing1 :ActivityFollowing{

    var timestamp = 0L
    var displayName = ""
    var profileImage = ""
    var totalLiked = 0
    var photosLiked = ArrayList<Photo>()


    companion object {
        fun fromJson(jsonObject: JSONObject): ActivityFollowing1 {
            val activity = ActivityFollowing1()
            activity.timestamp = jsonObject.optLong("timestamp")
            activity.displayName = jsonObject.optString("display_name")
            activity.profileImage = jsonObject.optString("profile_image")
            activity.totalLiked = jsonObject.optInt("total_liked")

            val photosArray =  jsonObject.optJSONArray("photos_liked") ?:JSONArray()
            activity.photosLiked = Utils.photosFromJsonArray(photosArray)

            return activity
        }
    }

}