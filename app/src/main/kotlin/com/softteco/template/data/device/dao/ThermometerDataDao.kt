package com.softteco.template.data.device.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.softteco.template.data.device.model.ThermometerDataDb
import com.softteco.template.data.device.model.ThermometerValuesDb

@Dao
interface ThermometerDataDao {

    fun saveResource(thermometerData: ThermometerDataDb): Long {
        val id: Long
        insertOrUpdate(thermometerData).also { thermometerDataId ->
            id = thermometerDataId
            thermometerData.valuesHistory.forEach {
                insertOrUpdate(
                    ThermometerValuesDb(it.toEntity())
                )
            }
        }
        return id
    }

    fun getResource(macAddress: String): ThermometerDataDb {
        return createEntity(getThermometerData(macAddress))
    }

    fun deleteResource(macAddress: String): Int {
        val id: Int
        deleteThermometerData(macAddress).also {
            id = it
            deleteThermometerValues(macAddress)
        }
        return id
    }

    private fun createEntity(thermometerData: ThermometerDataDb): ThermometerDataDb {
        return ThermometerDataDb(
            thermometerData.databaseId,
            thermometerData.deviceId,
            thermometerData.deviceName,
            thermometerData.macAddress,
            getThermometerValues(thermometerData.macAddress)
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(thermometerData: ThermometerDataDb): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(thermometerValues: ThermometerValuesDb): Long

    @Query("SELECT * FROM thermometerData WHERE macAddress = :macAddress")
    fun getThermometerData(macAddress: String): ThermometerDataDb

    @Query("SELECT * FROM thermometerValues WHERE macAddress = :macAddress")
    fun getThermometerValues(macAddress: String): List<ThermometerValuesDb>

    @Query("SELECT * FROM thermometerValues WHERE macAddress = :macAddress ORDER BY timestamp DESC LIMIT 1")
    fun getThermometerValue(macAddress: String): ThermometerValuesDb

    @Query("DELETE FROM thermometerData WHERE macAddress = :macAddress")
    fun deleteThermometerData(macAddress: String): Int

    @Query("DELETE FROM thermometerValues WHERE macAddress = :macAddress")
    fun deleteThermometerValues(macAddress: String): Int
}
