package io.replicants.instaclone.pojos

import io.replicants.instaclone.utilities.Utils
import org.json.JSONArray
import org.json.JSONObject

class ActivitySelf3 :ActivityFollowing{

    var timestamp = 0L
    var photo = Photo()
    var recentComments = 0
    var previewComment = Comment()


    companion object {
        fun fromJson(jsonObject: JSONObject): ActivitySelf3 {
            val activity = ActivitySelf3()
            activity.timestamp = jsonObject.optLong("timestamp")
            activity.photo = Utils.photoFromJson(jsonObject)
            activity.recentComments = jsonObject.optInt("recent_comments")
            activity.previewComment = Utils.commentFromJson(jsonObject.optJSONObject("preview_comment")?:JSONObject())


            return activity
        }
    }

}