package com.softteco.template.data.device.protocol.bluetooth

import com.softteco.template.Constants
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ThermometerValues
import com.softteco.template.utils.protocol.bluetoothCharacteristicByteConversation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BluetoothByteParserImpl @Inject constructor() : BluetoothByteParser {

    override fun parseBytes(bytes: ByteArray, bluetoothDeviceModel: Device.Model): Any {
        return when (bluetoothDeviceModel) {
            Device.Model.LYWSD03MMC -> {
                ThermometerValues.DataLYWSD03MMC(
                    bluetoothCharacteristicByteConversation(
                        bytes,
                        Constants.START_INDEX_OF_TEMPERATURE,
                        Constants.END_INDEX_OF_TEMPERATURE
                    ) / Constants.DIVISION_VALUE_OF_VALUES,
                    bytes[Constants.INDEX_OF_HUMIDITY].toInt(),
                    bluetoothCharacteristicByteConversation(
                        bytes,
                        Constants.START_INDEX_OF_BATTERY,
                        Constants.END_INDEX_OF_BATTERY
                    )
                )
            }

            Device.Model.Unknown -> {
                ThermometerValues.Default
            }
        }
    }
}
