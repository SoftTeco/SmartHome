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
    suspend fun insertOrUpdate(bluetoothDevice: BluetoothDeviceDb)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBluetoothDeviceAndGetId(bluetoothDevice: BluetoothDeviceDb): Flow<Unit>

    @Query("update bluetooth_devices SET connectedLastTime = :connectedLastTime WHERE macAddress = :macAddress")
    fun updateAutoConnectState(macAddress: String, connectedLastTime: Long)

    @Query("select * from bluetooth_devices")
    fun getBluetoothDevices(): Flow<List<BluetoothDeviceDb>>

    @Query("DELETE FROM bluetooth_devices WHERE macAddress = :macAddress")
    fun deleteBT(macAddress: String)
}
