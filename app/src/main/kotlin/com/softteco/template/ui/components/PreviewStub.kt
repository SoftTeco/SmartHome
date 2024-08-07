package com.softteco.template.ui.components

import com.softteco.template.R
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.SupportedDevices
import java.util.UUID

object PreviewStub {
    val connectedDevices: List<Device> by lazy {
        List(20) {
            listOf(
                Device.Basic(
                    type = Device.Type.TemperatureAndHumidity,
                    family = Device.Family.Sensor,
                    model = Device.Model.LYWSD03MMC,
                    id = UUID.randomUUID(),
                    defaultName = "Temperature and Humidity Monitor",
                    name = "Temperature and Humidity Monitor",
                    macAddress = "00:00:00:00:00",
                    img = "file:///android_asset/icon/temperature_monitor.webp",
                    location = "Bedroom",
                ),
                Device.Basic(
                    id = UUID.randomUUID(),
                    defaultName = "Washing Machine",
                    name = "Washing Machine",
                    macAddress = "00:00:00:00:00",
                    img = "file:///android_asset/icon/washing_machine.webp",
                    location = "Kitchen",
                    type = Device.Type.WashingMachine,
                    family = Device.Family.Cleaning,
                    model = Device.Model.Unknown
                ),
                object : Device.QuickAccess {
                    override val type = Device.Type.RobotVacuum
                    override val family = Device.Family.Cleaning
                    override val model = Device.Model.Unknown
                    override val id = UUID.randomUUID()
                    override val defaultName = "Robot Vacuum-Mop"
                    override val name = "Robot Vacuum-Mop"
                    override val macAddress = "00:00:00:00:00"
                    override val img =
                        "file:///android_asset/icon/robot_vacuum.webp"
                    override val location = "Living room"

                    override val onClickAction = {}
                    override val actionIcon = R.drawable.outline_play_circle_24
                    override val actionDescription = R.string.start_device_action_description
                }
            ).random()
        }
    }

    val supportedDevices by lazy {
        connectedDevices
            .asSequence()
            .map { SupportedDevices.LYWSD03MMC }
            .toSet()
            .toList()
    }
}
