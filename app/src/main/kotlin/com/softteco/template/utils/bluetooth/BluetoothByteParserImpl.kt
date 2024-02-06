package com.softteco.template.utils.bluetooth

import com.softteco.template.Constants
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceData
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BluetoothByteParserImpl @Inject constructor() : BluetoothByteParser {

    override fun parseBytes(bytes: ByteArray, bluetoothDeviceType: BluetoothDeviceType): Any {
        return when (bluetoothDeviceType) {
            BluetoothDeviceType.LYWSD03MMC -> {
                BluetoothDeviceData.DataLYWSD03MMC(
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

            BluetoothDeviceType.OTHER -> {
                BluetoothDeviceData.DataLYWSD03MMC() // Change to any new devices
            }
        }
    }
}
