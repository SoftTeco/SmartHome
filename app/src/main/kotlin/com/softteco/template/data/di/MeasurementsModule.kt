package com.softteco.template.data.di

import com.softteco.template.data.measurement.MeasurementCacheStore
import com.softteco.template.utils.measurement.MeasurementCacheStoreImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface MeasurementsModule {

    @Binds
    fun bindMeasurementCacheStore(store: MeasurementCacheStoreImpl): MeasurementCacheStore
}