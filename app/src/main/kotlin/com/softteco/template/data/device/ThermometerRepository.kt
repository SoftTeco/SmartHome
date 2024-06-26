package com.softteco.template.data.device

import com.softteco.template.data.base.error.Result

interface ThermometerRepository {

    suspend fun getDevices(): Result<List<Device>>
    suspend fun saveDevice(device: Device): Result<Long>
    suspend fun getThermometerData(macAddress: String): Result<ThermometerData>
    suspend fun saveThermometerData(thermometerData: ThermometerData): Result<Long>
    suspend fun getCurrentMeasurement(macAddress: String): Result<ThermometerValues>
    suspend fun saveCurrentMeasurement(thermometerValues: ThermometerValues.DataLYWSD03MMC): Result<Long>
}
