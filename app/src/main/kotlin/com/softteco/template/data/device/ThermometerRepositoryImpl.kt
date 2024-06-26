package com.softteco.template.data.device

import com.softteco.template.data.base.error.AppError
import com.softteco.template.data.base.error.Result
import com.softteco.template.data.base.model.DeviceDb
import com.softteco.template.data.base.model.ThermometerDataDb
import com.softteco.template.data.base.model.ThermometerValuesDb
import com.softteco.template.data.bluetooth.usecase.DeviceSaveUseCase
import com.softteco.template.data.bluetooth.usecase.DevicesGetUseCase
import com.softteco.template.data.bluetooth.usecase.ThermometerDataGetUseCase
import com.softteco.template.data.bluetooth.usecase.ThermometerDataSaveUseCase
import com.softteco.template.data.bluetooth.usecase.ThermometerValuesGetUseCase
import com.softteco.template.data.bluetooth.usecase.ThermometerValuesSaveUseCase
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ThermometerRepositoryImpl @Inject constructor(
    private val devicesGetUseCase: DevicesGetUseCase,
    private val deviceSaveUseCase: DeviceSaveUseCase,
    private val thermometerDataGetUseCase: ThermometerDataGetUseCase,
    private val thermometerDataSaveUseCase: ThermometerDataSaveUseCase,
    private val thermometerValuesGetUseCase: ThermometerValuesGetUseCase,
    private val thermometerValuesSaveUseCase: ThermometerValuesSaveUseCase
) : ThermometerRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getDevices(): Result<List<Device>> {
        return try {
            val savedBluetoothDevices = mutableListOf<Device>()
            devicesGetUseCase.execute().first().forEach {
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
                deviceSaveUseCase.execute(
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
            Result.Success(thermometerDataGetUseCase.execute(macAddress).toEntity())
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveThermometerData(thermometerData: ThermometerData): Result<Long> {
        return try {
            Result.Success(
                thermometerDataSaveUseCase.execute(
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
            Result.Success(thermometerValuesGetUseCase.execute(macAddress).toEntity())
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveCurrentMeasurement(thermometerValues: ThermometerValues.DataLYWSD03MMC): Result<Long> {
        return try {
            Result.Success(
                thermometerValuesSaveUseCase.execute(
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
