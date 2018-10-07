package io.replicants.instaclone.subfragments.upload.pickphoto

import io.realm.RealmList
import io.realm.RealmObject

open class EditPhotoState : RealmObject(){
    var currentRotation = 0f
    var baseRotation = 0f
    var imageMatrix = RealmList<Float>()
    var colorMatrix = RealmList<Float>()
    var brightness = 0
    var contrast = 0
    var contrastMatrixValue = 1f
    var saturation = 0
    var filter = ""
}