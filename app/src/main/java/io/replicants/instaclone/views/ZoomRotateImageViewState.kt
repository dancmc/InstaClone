package io.replicants.instaclone.views

import io.realm.RealmObject

open class ZoomRotateImageViewState :RealmObject(){
    var minZoom = 0f
    var maxZoom = 0f
    var scaleFactor = 0f
    var oldScaleFactor = 0f
    var extraScaleForRotate=0f
    var rotate = 0f
    var viewHeight = 0
    var viewWidth = 0
}