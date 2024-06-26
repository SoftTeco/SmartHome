package com.softteco.template.data.base.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.softteco.template.data.base.model.DeviceDb
import kotlinx.coroutines.flow.Flow

@Dao
interface DevicesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(device: DeviceDb): Long

    @Query("select * from devices WHERE macAddress = :macAddress")
    fun getDevice(macAddress: String): DeviceDb

    @Query("select * from devices")
    fun getListDevices(): Flow<List<DeviceDb>>

    @Query("DELETE FROM devices WHERE macAddress = :macAddress")
    fun delete(macAddress: String): Int
}
