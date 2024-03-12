package com.softteco.template.data.base.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceType
import com.softteco.template.data.measurement.entity.MeasurementDevice

private const val DATABASE_ID = 0L

@Entity(
    tableName = "measurements",
    indices = [Index(value = ["guid"], unique = true)]
)
data class MeasurementDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "database_id")
    var databaseId: Long,
    @ColumnInfo(name = "guid")
    var guid: String,
    @ColumnInfo(name = "temperature")
    var temperature: Double,
    @ColumnInfo(name = "humidity")
    var humidity: Int,
    @ColumnInfo(name = "battery")
    var battery: Double,
    @ColumnInfo(name = "bluetoothDeviceType")
    var bluetoothDeviceType: String,
    @ColumnInfo(name = "macAddressOfDevice")
    var macAddressOfDevice: String
) : MeasurementSavedDb() {
    constructor(entity: MeasurementDevice) : this(
        DATABASE_ID,
        entity.guid,
        entity.temperature,
        entity.humidity,
        entity.battery,
        entity.bluetoothDeviceType.toString(),
        entity.macAddressOfDevice
    )

    override fun toEntity(): MeasurementDevice {
        return MeasurementDevice(
            guid = guid,
            temperature = temperature,
            humidity = humidity,
            battery = battery,
            bluetoothDeviceType = BluetoothDeviceType.valueOf(bluetoothDeviceType),
            macAddressOfDevice = macAddressOfDevice
        )
    }
}
