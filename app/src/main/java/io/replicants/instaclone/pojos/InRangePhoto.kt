package io.replicants.instaclone.pojos

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class InRangePhoto :RealmObject(){

    @PrimaryKey
    var photoID = ""
    var profileImageID = ""


    var displayName = ""
    var caption = ""
    var locationName = ""
    var latitude = 999.0
    var longitude = 999.0
    var timestamp = 0L
    var regularWidth = 0
    var regularHeight = 0

}