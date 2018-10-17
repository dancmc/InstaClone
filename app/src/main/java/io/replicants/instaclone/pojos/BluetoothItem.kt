package io.replicants.instaclone.pojos

import android.bluetooth.BluetoothDevice

data class BluetoothItem(var device: BluetoothDevice, var address: String, var name: String, var connecting:Boolean=false, var connected:Boolean=false) {
    override fun equals(other: Any?): Boolean {
        return other is BluetoothItem && address == other.address
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }
}