package com.softteco.template.data.base.model

import com.softteco.template.data.bluetooth.entity.BluetoothDevice

abstract class BluetoothDevicesSavedDb {
    abstract fun toEntity(): BluetoothDevice
}
