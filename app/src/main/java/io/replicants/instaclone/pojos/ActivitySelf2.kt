package io.replicants.instaclone.pojos

import io.replicants.instaclone.utilities.Utils
import org.json.JSONArray
import org.json.JSONObject

class ActivitySelf2 :ActivityFollowing{

    var timestamp = 0L
    var photo = Photo()
    var recentLikes = 0
    var previewUsers = ArrayList<User>()


    companion object {
        fun fromJson(jsonObject: JSONObject): ActivitySelf2 {
            val activity = ActivitySelf2()
            activity.timestamp = jsonObject.optLong("timestamp")
            activity.photo = Utils.photoFromJson(jsonObject)
            activity.recentLikes = jsonObject.optInt("recent_likes")

            val previewUsersArray =  jsonObject.optJSONArray("preview_users") ?:JSONArray()
            activity.previewUsers = Utils.usersFromJsonArray(previewUsersArray)

            return activity
        }
    }

}