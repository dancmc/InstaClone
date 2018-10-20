package io.replicants.instaclone.subfragments.bluetooth

import io.replicants.instaclone.pojos.InRangePhoto

interface BluetoothActivityInterface{
    fun photoObtained(filename: String)
    fun photoCropped(filename: String)
    fun sendPhoto(photo:InRangePhoto, filepath:String)

    fun goBack()
    fun errorAndGoBack(message:String)
    fun handleSendSuccess()
    fun handleSendError()
    fun receivedPhoto(address:String, json:String)

}