package com.softteco.template.ui.components

import com.softteco.template.R
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.SupportedDevice
import java.util.UUID

object PreviewStub {
    val connectedDevices: List<Device> by lazy {
        List(20) {
            listOf(
                Device.Basic(
                    type = Device.Type.TemperatureAndHumidity,
                    family = Device.Family.Sensor,
                    id = UUID.randomUUID(),
                    defaultName = "Temperature and Humidity Monitor",
                    name = "Temperature and Humidity Monitor",
                    img = "file:///android_asset/icon/temperature_monitor.webp",
                    location = "Bedroom",
                ),
                Device.Basic(
                    id = UUID.randomUUID(),
                    defaultName = "Washing Machine",
                    name = "Washing Machine",
                    img = "file:///android_asset/icon/washing_machine.webp",
                    location = "Kitchen",
                    type = Device.Type.WashingMachine,
                    family = Device.Family.Cleaning
                ),
                object : Device.QuickAccess {
                    override val type = Device.Type.RobotVacuum
                    override val family = Device.Family.Cleaning
                    override val id = UUID.randomUUID()
                    override val defaultName = "Robot Vacuum-Mop"
                    override val name = "Robot Vacuum-Mop"
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
            .map { SupportedDevice(it.type, it.family, it.type.img) }
            .toSet()
            .toList()
    }
}
