package com.softteco.template.data.di

import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.BluetoothPermissionChecker
import com.softteco.template.data.bluetooth.DevicesCacheStore
import com.softteco.template.data.bluetooth.DevicesDataCacheStore
import com.softteco.template.utils.bluetooth.BluetoothByteParserImpl
import com.softteco.template.utils.bluetooth.BluetoothHelperImpl
import com.softteco.template.utils.bluetooth.BluetoothPermissionCheckerImpl
import com.softteco.template.utils.bluetooth.DevicesCacheStoreImpl
import com.softteco.template.utils.bluetooth.DevicesDataCacheStoreImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface BluetoothModule {

    @Binds
    fun bindBluetoothHelper(bluetoothHelper: BluetoothHelperImpl): BluetoothHelper

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
