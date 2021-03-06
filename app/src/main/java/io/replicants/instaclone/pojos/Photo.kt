package io.replicants.instaclone.pojos

class Photo {

    var displayName = ""
    var profileImage = ""
    var photoID = ""
    var thumbUrl = ""
    var smallUrl = ""
    var regularUrl = ""
    var previewComments = arrayListOf<Pair<String, String>>()
    var totalComments = 0
    var caption = ""
    var isLiked = false
    var previewLikes = arrayListOf<String>()
    var totalLikes = 0
    var locationName = ""
    var latitude = 999.0
    var longitude = 999.0
    var timestamp = 0L
    var distance = -1.0
    var smallWidth = 0
    var smallHeight = 0
    var thumbWidth = 0
    var thumbHeight = 0
    var regularWidth = 0
    var regularHeight = 0

    var inRange = false


    override fun equals(other: Any?): Boolean {
        return other is Photo && other.photoID == this.photoID
    }

    override fun hashCode(): Int {
        return this.photoID.hashCode()
    }
}