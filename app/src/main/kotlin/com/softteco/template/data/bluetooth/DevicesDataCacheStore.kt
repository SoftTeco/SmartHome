package com.softteco.template.data.bluetooth

import com.softteco.template.data.base.model.ThermometerDataDb
import com.softteco.template.data.base.model.ThermometerValuesDb

interface DevicesDataCacheStore {

    fun saveResource(thermometerData: ThermometerDataDb): Long

    fun getResource(macAddress: String): ThermometerDataDb

    fun saveThermometerValues(thermometerValues: ThermometerValuesDb): Long

    fun getThermometerValues(macAddress: String): ThermometerValuesDb

    fun deleteResource(macAddress: String): Int
}
