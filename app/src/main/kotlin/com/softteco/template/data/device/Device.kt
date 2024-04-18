package com.softteco.template.data.device

import android.content.Context
import androidx.annotation.StringRes
import com.softteco.template.R
import java.util.UUID

interface Device {
    val id: UUID
    val defaultName: String
    val name: String
    val img: String
    val location: String
    val type: Type
    val family: Family

    data class Basic(
        override val id: UUID,
        override val defaultName: String,
        override val name: String,
        override val img: String,
        override val location: String,
        override val type: Type,
        override val family: Family
    ) : Device

    interface QuickAccess : Device {
        val onClickAction: () -> Unit
        val actionIcon: Int
        val actionDescription: Int
    }

    sealed class Family(@StringRes val nameRes: Int) {
        data object Sensor : Family(R.string.sensor)
        data object Cleaning : Family(R.string.cleaning)

        fun Family.getName(context: Context): String {
            return context.getString(nameRes)
        }
    }

    sealed class Type(@StringRes val nameRes: Int, val img: String) {

        data object TemperatureAndHumidity : Type(
            R.string.temperature_and_humidity,
            "file:///android_asset/icon/temperature_monitor.webp"
        )

        data object RobotVacuum : Type(
            R.string.robot_vacuum_mop,
            "file:///android_asset/icon/robot_vacuum.webp"
        )

        data object WashingMachine : Type(
            R.string.washing_machine,
            "file:///android_asset/icon/washing_machine.webp"
        )

        fun getName(context: Context): String {
            return context.getString(nameRes)
        }
    }
}

data class SupportedDevice(
    val type: Device.Type,
    val family: Device.Family,
    val img: String,
)
