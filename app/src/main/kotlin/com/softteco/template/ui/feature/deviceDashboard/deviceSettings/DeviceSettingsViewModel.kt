package com.softteco.template.ui.feature.deviceDashboard.deviceSettings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.softteco.template.navigation.AppNavHost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class DeviceSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val deviceId = checkNotNull(savedStateHandle.get<String>(AppNavHost.DEVICE_ID_KEY))

    val state = MutableStateFlow(State(deviceId))

    @Immutable
    data class State(val deviceId: String)
}
