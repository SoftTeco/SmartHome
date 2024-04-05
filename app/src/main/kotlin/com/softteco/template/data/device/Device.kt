package com.softteco.template.data.device

import java.util.UUID

interface Device {
    val id: UUID
    val name: String
    val img: String
    val location: String
    val type: Type
    val family: Family

    data class Basic(
        override val id: UUID,
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

    sealed interface Family {
        data object Sensor : Family
        data object Cleaning : Family
    }

    sealed interface Type {
        data object TemperatureAndHumidity : Type
        data object RobotVacuum : Type
        data object WashingMachine : Type
    }
}
