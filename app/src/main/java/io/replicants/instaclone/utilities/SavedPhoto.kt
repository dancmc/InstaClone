package io.replicants.instaclone.utilities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SavedPhoto:RealmObject(){
        @PrimaryKey var photoID: String = ""
        var photoFile: String = ""
        var caption: String = ""
        var locationName: String = ""
        var longitude: Double = 0.0
        var latitude: Double = 0.0
        var brightness: Int = 0
         var contrast: Int = 0
         var saturation: Int = 0
         var rotation: Double = 0.0
        var filter: String = ""

    fun assignVar(photoID:String, photoFile:String, caption:String, locationName:String,
                  longitude:Double, latitude:Double, brightness:Int, contrast:Int, saturation:Int, rotation:Double, filter:String){
        this.photoID = photoID
        this.photoFile = photoFile
        this.caption = caption
        this.locationName = locationName
        this.longitude = longitude
        this.latitude = latitude
        this.brightness = brightness
        this.contrast = contrast
        this.saturation = saturation
        this.rotation = rotation
        this.filter = filter
    }
}