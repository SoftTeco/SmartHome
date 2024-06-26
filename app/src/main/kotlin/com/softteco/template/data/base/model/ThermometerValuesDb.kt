package com.softteco.template.data.base.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.utils.DateUtils.stringToLocalDateTime

private const val DATABASE_ID = 0L

@Entity(
    tableName = "thermometerValues"
)
data class ThermometerValuesDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "database_id")
    var databaseId: Long,
    @ColumnInfo(name = "temperature")
    var temperature: Double,
    @ColumnInfo(name = "humidity")
    var humidity: Int,
    @ColumnInfo(name = "battery")
    var battery: Double,
    @ColumnInfo(name = "timestamp")
    var timestamp: String,
    @ColumnInfo(name = "macAddress")
    var macAddress: String
) : ThermometerValuesSavedDb() {

    constructor(entity: ThermometerValues.DataLYWSD03MMC) : this(
        DATABASE_ID,
        entity.temperature,
        entity.humidity,
        entity.battery,
        entity.timestamp.toString(),
        entity.macAddress
    )

    override fun toEntity(): ThermometerValues.DataLYWSD03MMC {
        return ThermometerValues.DataLYWSD03MMC(
            temperature = temperature,
            humidity = humidity,
            battery = battery,
            macAddress = macAddress,
            timestamp = stringToLocalDateTime(timestamp)
        )
    }
}
