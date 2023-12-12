package com.softteco.template.utils

import com.softteco.template.Constants
import com.softteco.template.Constants.BIT_SHIFT_VALUE
import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.entity.BluetoothDeviceType
import com.softteco.template.data.bluetooth.entity.DataLYWSD03MMC
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BluetoothByteParserImpl @Inject constructor() : BluetoothByteParser {

    override fun parseBytes(bytes: ByteArray, bluetoothDeviceType: BluetoothDeviceType): Any {
        return when (bluetoothDeviceType) {
            BluetoothDeviceType.LYWSD03MMC -> {
                DataLYWSD03MMC(
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
                DataLYWSD03MMC() //Change to any new devices
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun characteristicByteConversation(
        bytes: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): Double {
        val array = bytes.copyOfRange(startIndex, endIndex).toUByteArray()
        var result = 0
        for (i in array.indices) {
            result = result or (array[i].toInt() shl BIT_SHIFT_VALUE * i)
        }
        return result.toDouble()
    }
}
