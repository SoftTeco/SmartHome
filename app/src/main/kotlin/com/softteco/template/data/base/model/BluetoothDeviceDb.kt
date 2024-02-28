package com.softteco.template.data.base.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.softteco.template.data.bluetooth.entity.BluetoothDevice
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceType

private const val DATABASE_ID = 0L

@Entity(tableName = "bluetooth_devices",
    indices = [Index(value = ["macAddress"], unique = true)]
)
data class BluetoothDeviceDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "database_id")
    var databaseId: Long,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "macAddress")
    var macAddress: String,
    @ColumnInfo(name = "deviceType")
    var deviceType: String,
    @ColumnInfo(name = "connectedLastTime")
    var connectedLastTime: Long
) : BluetoothDevicesSavedDb() {
    constructor(entity: BluetoothDevice) : this(
        DATABASE_ID,
        entity.name,
        entity.macAddress,
        entity.deviceType.toString(),
        entity.connectedLastTime
    )

    override fun toEntity(): BluetoothDevice {
        return BluetoothDevice(
            name = name,
            macAddress = macAddress,
            deviceType = BluetoothDeviceType.valueOf(deviceType),
            connectedLastTime = connectedLastTime
        )
    }
}