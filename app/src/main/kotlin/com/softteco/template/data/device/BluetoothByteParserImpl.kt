package com.softteco.template.data.device

import com.softteco.template.Constants
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.utils.bluetooth.characteristicByteConversation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BluetoothByteParserImpl @Inject constructor() : BluetoothByteParser {

    override fun parseBytes(bytes: ByteArray, bluetoothDeviceModel: Device.Model): Any {
        return when (bluetoothDeviceModel) {
            Device.Model.LYWSD03MMC -> {
                ThermometerValues.DataLYWSD03MMC(
                    characteristicByteConversation(
                        bytes,
                        Constants.START_INDEX_OF_TEMPERATURE,
                        Constants.END_INDEX_OF_TEMPERATURE
                    ) / Constants.DIVISION_VALUE_OF_VALUES,
                    bytes[Constants.INDEX_OF_HUMIDITY].toInt(),
                    characteristicByteConversation(
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
