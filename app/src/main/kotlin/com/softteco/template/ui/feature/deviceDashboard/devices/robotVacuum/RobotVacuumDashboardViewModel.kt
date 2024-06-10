package com.softteco.template.ui.feature.deviceDashboard.devices.robotVacuum

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.softteco.template.navigation.AppNavHost.DEVICE_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class RobotVacuumDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val deviceId = checkNotNull(savedStateHandle.get<String>(DEVICE_ID_KEY))

    val state = MutableStateFlow(State(deviceId))

    @Immutable
    data class State(val deviceId: String)
}
