package com.softteco.template.data.device

import android.content.Context
import androidx.annotation.StringRes
import com.softteco.template.R
import kotlinx.serialization.Serializable
import java.util.UUID

interface Device {
    val id: UUID
    val defaultName: String
    val name: String
    val macAddress: String
    val img: String
    val location: String
    val type: Type
    val family: Family
    val model: Model

    data class Basic(
        override val id: UUID,
        override val defaultName: String,
        override val name: String,
        override val macAddress: String,
        override val img: String,
        override val location: String,
        override val type: Type,
        override val family: Family,
        override val model: Model
    ) : Device

    interface QuickAccess : Device {
        val onClickAction: () -> Unit
        val actionIcon: Int
        val actionDescription: Int
    }

    @Serializable
    sealed class Family(@StringRes val nameRes: Int) {

        @Serializable
        data object Sensor : Family(R.string.sensor)

        @Serializable
        data object Cleaning : Family(R.string.cleaning)

        fun Family.getName(context: Context): String {
            return context.getString(nameRes)
        }
    }

    @Serializable
    sealed class Type(@StringRes val nameRes: Int, val img: String) {

        @Serializable
        data object TemperatureAndHumidity : Type(
            R.string.temperature_and_humidity,
            "file:///android_asset/icon/temperature_monitor.webp"
        )

        @Serializable
        data object RobotVacuum : Type(
            R.string.robot_vacuum_mop,
            "file:///android_asset/icon/robot_vacuum.webp"
        )

        @Serializable
        data object WashingMachine : Type(
            R.string.washing_machine,
            "file:///android_asset/icon/washing_machine.webp"
        )

        fun getName(context: Context): String {
            return context.getString(nameRes)
        }
    }

    @Serializable
    sealed class Model(@StringRes val nameRes: Int, val img: String) {

        @Serializable
        data object LYWSD03MMC : Model(
            R.string.temperature_and_humidity_LYWSD03MMC,
            "file:///android_asset/icon/temperature_monitor_LYWSD03MMC.webp"
        )

        @Serializable
        data object Unknown :
            Model(R.string.unknown, "file:///android_asset/icon/unknown_model.webp")

        fun Model.getName(context: Context): String {
            return context.getString(nameRes)
        }
    }
}

data class SupportedDevice(
    val type: Device.Type,
    val family: Device.Family,
    val model: Device.Model,
    val img: String,
)

enum class SupportedDevices(val supportedDevice: SupportedDevice) {
    LYWSD03MMC(
        SupportedDevice(
            Device.Type.TemperatureAndHumidity,
            Device.Family.Sensor,
            Device.Model.LYWSD03MMC,
            "file:///android_asset/icon/temperature_monitor.webp"
        )
    )
}
