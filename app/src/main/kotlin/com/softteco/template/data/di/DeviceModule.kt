package com.softteco.template.data.di

import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.BluetoothPermissionChecker
import com.softteco.template.data.bluetooth.DevicesCacheStore
import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import com.softteco.template.data.device.protocol.bluetooth.BluetoothByteParserImpl
import com.softteco.template.data.device.protocol.bluetooth.BluetoothHelperImpl
import com.softteco.template.data.device.protocol.bluetooth.BluetoothPermissionCheckerImpl
import com.softteco.template.data.device.DevicesCacheStoreImpl
import com.softteco.template.data.device.DevicesDataCacheStoreImpl
import com.softteco.template.data.device.protocol.zigbee.ZigbeeHelperImpl
import com.softteco.template.data.zigbee.ZigbeeHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DeviceModule {

    @Binds
    fun bindBluetoothHelper(bluetoothHelper: BluetoothHelperImpl): BluetoothHelper

    @Binds
    fun bindZigbeeHelper(zigbeeHelper: ZigbeeHelperImpl): ZigbeeHelper

    @Binds
    fun bindBluetoothPermissionChecker(bluetoothPermissionChecker: BluetoothPermissionCheckerImpl):
        BluetoothPermissionChecker

    @Binds
    fun bindBluetoothByteParserImpl(bluetoothByteParserImpl: BluetoothByteParserImpl): BluetoothByteParser

    @Binds
    fun bindDevicesCacheStore(store: DevicesCacheStoreImpl): DevicesCacheStore

    @Binds
    fun bindDevicesDataCacheStore(store: DevicesDataCacheStoreImpl): DevicesDataCacheStore
}
