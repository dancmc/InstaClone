package io.replicants.instaclone.subfragments.bluetooth

import io.replicants.instaclone.pojos.Photo

interface BluetoothActivityInterface{
    fun photoObtained(filename: String)
    fun photoCropped(filename: String)
    fun sendPhoto(photo:Photo, filepath:String)

    fun goBack()
    fun errorAndGoBack(message:String)

}