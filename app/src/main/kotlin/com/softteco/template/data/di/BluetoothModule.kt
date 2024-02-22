package com.softteco.template.data.di

import com.softteco.template.data.bluetooth.BluetoothByteParser
import com.softteco.template.data.bluetooth.BluetoothHelper
import com.softteco.template.data.bluetooth.BluetoothPermissionChecker
import com.softteco.template.utils.bluetooth.BluetoothHelperImpl
import com.softteco.template.utils.bluetooth.BluetoothPermissionCheckerImpl
import com.softteco.template.utils.bluetooth.BluetoothByteParserImpl
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
}
