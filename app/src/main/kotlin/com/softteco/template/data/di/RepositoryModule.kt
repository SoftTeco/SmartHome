package com.softteco.template.data.di

import com.softteco.template.data.device.ThermometerRepository
import com.softteco.template.data.device.ThermometerRepositoryImpl
import com.softteco.template.data.profile.ProfileRepository
import com.softteco.template.data.profile.ProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface RepositoryModule {

    @Binds
    fun bindFeature1Repository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    fun bindThermometerRepository(impl: ThermometerRepositoryImpl): ThermometerRepository
}
