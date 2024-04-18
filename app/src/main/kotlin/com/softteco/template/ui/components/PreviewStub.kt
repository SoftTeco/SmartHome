package com.softteco.template.ui.components

import com.softteco.template.R
import com.softteco.template.data.device.Device
import java.util.UUID

object PreviewStub {
    val connectedDevices: List<Device> by lazy {
        List(20) {
            listOf(
                Device.Basic(
                    type = Device.Type.TemperatureAndHumidity,
                    family = Device.Family.Sensor,
                    id = UUID.randomUUID(),
                    name = "Temperature and Humidity Monitor",
                    img =
                    "https://upload.wikimedia.org/wikipedia/commons/e/ee/Raumbedienger%C3%A4t.jpg",
                    location = "Bedroom",
                ),
                Device.Basic(
                    id = UUID.randomUUID(),
                    name = "Washing Machine",
                    img = "https://upload.wikimedia.org/wikipedia/commons/0/08/LGwashingmachine.jpg",
                    location = "Kitchen",
                    type = Device.Type.WashingMachine,
                    family = Device.Family.Cleaning
                ),
                object : Device.QuickAccess {
                    override val type = Device.Type.RobotVacuum
                    override val family = Device.Family.Cleaning
                    override val id = UUID.randomUUID()
                    override val name = "Robot Vacuum-Mop"
                    override val img =
                        "https://upload.wikimedia.org/wikipedia/commons/0/09/IRobot-Roomba-Top-view-01.jpg"
                    override val location = "Living room"

                    override val onClickAction = {}
                    override val actionIcon = R.drawable.outline_play_circle_24
                    override val actionDescription = R.string.start_device_action_description
                }
            ).random()
        }
    }
}
