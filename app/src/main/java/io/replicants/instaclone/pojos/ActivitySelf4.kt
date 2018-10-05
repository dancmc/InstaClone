package io.replicants.instaclone.pojos

import io.replicants.instaclone.utilities.Utils
import org.json.JSONArray
import org.json.JSONObject

class ActivitySelf4 :ActivityBase{

    var requests = ArrayList<User>()


    companion object {
        fun fromJson(jsonObject: JSONObject): ActivitySelf4 {
            val activity = ActivitySelf4()
            activity.requests = Utils.usersFromJsonArray(jsonObject.optJSONArray("requests")?:JSONArray())

            return activity
        }
    }

}