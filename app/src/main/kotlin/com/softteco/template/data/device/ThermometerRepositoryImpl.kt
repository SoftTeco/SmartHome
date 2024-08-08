package com.softteco.template.data.device

import com.softteco.template.data.base.error.AppError
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.bluetooth.DevicesCacheStore
import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import com.softteco.template.data.device.model.DeviceDb
import com.softteco.template.data.device.model.ThermometerDataDb
import com.softteco.template.data.device.model.ThermometerValuesDb
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ThermometerRepositoryImpl @Inject constructor(
    private val devicesCacheStore: DevicesCacheStore,
    private val devicesDataCacheStore: DevicesDataCacheStore,
) : ThermometerRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getDevices(): Result<List<Device>> {
        return try {
            val savedBluetoothDevices = mutableListOf<Device>()
            devicesCacheStore.getDevices().first().forEach {
                savedBluetoothDevices.add(it.toEntity())
            }
            Result.Success(savedBluetoothDevices)
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveDevice(device: Device): Result<Long> {
        return try {
            Result.Success(
                devicesCacheStore.saveDevice(
                    DeviceDb(device as Device.Basic)
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getThermometerData(macAddress: String): Result<ThermometerData> {
        return try {
            Result.Success(devicesDataCacheStore.getResource(macAddress).toEntity())
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveThermometerData(thermometerData: ThermometerData): Result<Long> {
        return try {
            Result.Success(
                devicesDataCacheStore.saveResource(
                    ThermometerDataDb(
                        ThermometerData(
                            deviceId = thermometerData.deviceId,
                            deviceName = thermometerData.deviceName,
                            macAddress = thermometerData.macAddress
                        )
                    )
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getCurrentMeasurement(macAddress: String): Result<ThermometerValues> {
        return try {
            Result.Success(devicesDataCacheStore.getThermometerValues(macAddress).toEntity())
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveCurrentMeasurement(thermometerValues: ThermometerValues.DataLYWSD03MMC): Result<Long> {
        return try {
            Result.Success(
                devicesDataCacheStore.saveThermometerValues(
                    ThermometerValuesDb(
                        ThermometerValues.DataLYWSD03MMC(
                            thermometerValues.temperature,
                            thermometerValues.humidity,
                            thermometerValues.battery,
                            thermometerValues.macAddress,
                            LocalDateTime.now(),
                        )
                    )
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }
}
