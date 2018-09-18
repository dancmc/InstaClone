package io.replicants.instaclone.utilities

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
    var distance = Double.MIN_VALUE
    var smallWidth = 0
    var smallHeight = 0
    var thumbWidth = 0
    var thumbHeight = 0
    var regularWidth = 0
    var regularHeight = 0


}