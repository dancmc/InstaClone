package io.replicants.instaclone.pojos

import io.replicants.instaclone.utilities.Utils
import org.json.JSONArray
import org.json.JSONObject

class ActivityFollowing3 :ActivityBase{

    var timestamp = 0L
    var displayName = ""
    var profileImage = ""
    var totalFollowed = 0
    var usersFollowed = ArrayList<User>()


    companion object {
        fun fromJson(jsonObject: JSONObject): ActivityFollowing3 {
            val activity = ActivityFollowing3()
            activity.timestamp = jsonObject.optLong("timestamp")
            activity.displayName = jsonObject.optString("display_name")
            activity.profileImage = jsonObject.optString("profile_image")
            activity.totalFollowed = jsonObject.optInt("total_followed")

            val usersFollowedArray =  jsonObject.optJSONArray("users_followed") ?:JSONArray()
            activity.usersFollowed = Utils.usersFromJsonArray(usersFollowedArray)

            return activity
        }
    }

}