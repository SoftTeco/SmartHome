package com.softteco.template.data.device.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private const val DATABASE_ID = 0L

@Entity(
    tableName = "devices",
    indices = [Index(value = ["macAddress"], unique = true)]
)
data class DeviceDb(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "database_id")
    var databaseId: Long,
    @ColumnInfo(name = "id")
    var id: String,
    @ColumnInfo(name = "defaultName")
    var defaultName: String,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "macAddress")
    var macAddress: String,
    @ColumnInfo(name = "img")
    var img: String?,
    @ColumnInfo(name = "location")
    var location: String,
    @ColumnInfo(name = "type")
    var type: String,
    @ColumnInfo(name = "family")
    var family: String,
    @ColumnInfo(name = "model")
    var model: String,
    @ColumnInfo(name = "protocolType")
    var protocolType: String
) : DeviceDataSavedDb() {
    constructor(entity: Device.Basic) : this(
        DATABASE_ID,
        entity.id.toString(),
        entity.defaultName,
        entity.name,
        entity.macAddress,
        entity.img,
        entity.location,
        Json.encodeToString(entity.type),
        Json.encodeToString(entity.family),
        Json.encodeToString(entity.model),
        entity.protocolType.name
    )

    override fun toEntity(): Device.Basic {
        return Device.Basic(
            id = UUID.fromString(id),
            defaultName = defaultName,
            name = name,
            macAddress = macAddress,
            img = img,
            location = location,
            type = Json.decodeFromString(type),
            family = Json.decodeFromString(family),
            model = Json.decodeFromString(model),
            protocolType = ProtocolType.valueOf(protocolType)
        )
    }
}
