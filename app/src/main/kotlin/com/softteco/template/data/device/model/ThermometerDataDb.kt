package com.softteco.template.data.device.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.softteco.template.data.device.ThermometerData
import com.softteco.template.data.device.ThermometerValues

private const val DATABASE_ID = 0L

@Entity(
    tableName = "thermometerData",
    indices = [Index(value = ["macAddress"], unique = true)]
)
data class ThermometerDataDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "database_id")
    var databaseId: Long,
    @ColumnInfo(name = "deviceId")
    var deviceId: String,
    @ColumnInfo(name = "deviceName")
    var deviceName: String,
    @ColumnInfo(name = "macAddress")
    var macAddress: String,
    @Ignore
    var valuesHistory: List<ThermometerValuesDb> = emptyList()
) : ThermometerDataSavedDb() {

    constructor() : this(
        DATABASE_ID,
        "",
        "",
        ""
    )

    constructor(entity: ThermometerData) : this(
        DATABASE_ID,
        entity.deviceId,
        entity.deviceName,
        entity.macAddress,
        entity.valuesHistory.map { ThermometerValuesDb(it as ThermometerValues.DataLYWSD03MMC) }
    )

    override fun toEntity(): ThermometerData {
        return ThermometerData(
            deviceId = deviceId,
            deviceName = deviceName,
            macAddress = macAddress,
            valuesHistory = valuesHistory.map { it.toEntity() }
        )
    }
}
