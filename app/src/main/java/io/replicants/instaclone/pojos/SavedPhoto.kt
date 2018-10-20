package io.replicants.instaclone.pojos

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.replicants.instaclone.subfragments.upload.pickphoto.EditPhotoState
import io.replicants.instaclone.views.ZoomRotateImageViewState

open class SavedPhoto():RealmObject(){
        @PrimaryKey var photoID: String = ""
        var photoFile: String = ""
        var photoFilePreview: String = ""
        var caption: String = ""
        var locationName: String = ""
        var longitude: Double = 0.0
        var latitude: Double = 0.0
        var editPhotoState:EditPhotoState?=null
        var imageViewState: ZoomRotateImageViewState?=null


    constructor(temp:Boolean, photoID:String, photoFile:String, photoFilePreview:String,caption:String, locationName:String,
                  longitude:Double, latitude:Double, editPhotoState:EditPhotoState, imageViewState:ZoomRotateImageViewState):this(){
        this.photoID = photoID
        this.photoFile = photoFile
        this.photoFilePreview = photoFilePreview
        this.caption = caption
        this.locationName = locationName
        this.longitude = longitude
        this.latitude = latitude
        this.editPhotoState = editPhotoState
        this.imageViewState = imageViewState
    }
}