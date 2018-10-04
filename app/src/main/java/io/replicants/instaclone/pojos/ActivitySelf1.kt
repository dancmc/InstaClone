package io.replicants.instaclone.pojos

import io.replicants.instaclone.utilities.Utils
import org.json.JSONArray
import org.json.JSONObject

class ActivitySelf1 :ActivityFollowing{

    var timestamp = 0L
    var users = ArrayList<User>()


    companion object {
        fun fromJson(jsonObject: JSONObject): ActivitySelf1 {
            val activity = ActivitySelf1()
            activity.timestamp = jsonObject.optLong("timestamp")

            val usersArray =  jsonObject.optJSONArray("users") ?:JSONArray()
            activity.users = Utils.usersFromJsonArray(usersArray)

            return activity
        }
    }

}