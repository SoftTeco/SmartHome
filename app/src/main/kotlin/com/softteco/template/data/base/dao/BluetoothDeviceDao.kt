package com.softteco.template.data.base.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.softteco.template.data.base.model.BluetoothDeviceDb
import kotlinx.coroutines.flow.Flow

@Dao
interface BluetoothDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(bluetoothDevice: BluetoothDeviceDb): Long

    @Query("update bluetooth_devices SET connectedLastTime = :connectedLastTime WHERE macAddress = :macAddress")
    fun updateLastConnectionTimeStamp(macAddress: String, connectedLastTime: Long): Int

    @Query("select * from bluetooth_devices")
    fun getList(): Flow<List<BluetoothDeviceDb>>

    @Query("DELETE FROM bluetooth_devices WHERE macAddress = :macAddress")
    fun delete(macAddress: String): Int
}
