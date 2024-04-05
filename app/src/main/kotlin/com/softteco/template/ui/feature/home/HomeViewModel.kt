package com.softteco.template.ui.feature.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.softteco.template.data.device.Device
import com.softteco.template.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _navDestination = MutableSharedFlow<Screen>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navDestination = _navDestination.asSharedFlow()

    private val devices = MutableStateFlow<List<Device>>(emptyList())

    val state = MutableStateFlow(State(devices.value))

    @Immutable
    data class State(
        val devices: List<Device> = emptyList()
    )
}
