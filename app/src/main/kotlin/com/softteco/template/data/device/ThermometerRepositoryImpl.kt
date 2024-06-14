package com.softteco.template.data.device

import com.softteco.template.data.base.error.AppError
import com.softteco.template.data.base.error.Result
import com.softteco.template.utils.ChartUtils
import com.softteco.template.utils.ChartUtils.generateRandomHistory
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ThermometerRepositoryImpl @Inject constructor() : ThermometerRepository {

    // TODO replace to DB connection
    @Suppress("TooGenericExceptionCaught")
    override suspend fun getThermometerData(): Result<Thermometer> {
        return try {
            val temperatureHistory = generateRandomHistory()
            val humidityHistory = generateRandomHistory()
            Result.Success(
                Thermometer(
                    deviceId = "111",
                    deviceName = "Main thermometer",
                    currentTemperature = temperatureHistory.entries.last().value,
                    currentHumidity = humidityHistory.entries.last().value,
                    temperatureHistory = temperatureHistory,
                    humidityHistory = humidityHistory
                )
            )
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }

    // TODO replace to thermometer connection
    @Suppress("TooGenericExceptionCaught")
    override suspend fun getCurrentMeasurement(): Result<Float> {
        return try {
            Result.Success(ChartUtils.generateRandomThermometerValue())
        } catch (e: Exception) {
            Timber.e(e)
            Result.Error(AppError.UnknownError())
        }
    }
}
