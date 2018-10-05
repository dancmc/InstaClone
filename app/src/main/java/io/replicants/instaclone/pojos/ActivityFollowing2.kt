package io.replicants.instaclone.pojos

import io.replicants.instaclone.utilities.Utils
import org.json.JSONArray
import org.json.JSONObject

class ActivityFollowing2 :ActivityBase{

    var timestamp = 0L
    var previewUsers = ArrayList<User>()
    var totalLiked = 0
    var photo = Photo()



    companion object {
        fun fromJson(jsonObject: JSONObject): ActivityFollowing2 {
            val activity = ActivityFollowing2()
            activity.timestamp = jsonObject.optLong("timestamp")
            val previewUserArray =  jsonObject.optJSONArray("preview_users") ?:JSONArray()
            activity.previewUsers = Utils.usersFromJsonArray(previewUserArray)

            activity.totalLiked = jsonObject.optInt("total_liked")
            activity.photo = Utils.photoFromJson(jsonObject)

            return activity
        }
    }

}