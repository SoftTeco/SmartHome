package com.softteco.template.ui.feature.devicesearch

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softteco.template.navigation.AppNavHost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DeviceSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state = MutableStateFlow(
        State(deviceName = checkNotNull(savedStateHandle.get<String>(AppNavHost.DEVICE_NAME_KEY)))
    ).stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        State()
    )

    @Immutable
    data class State(
        val dismissSnackBar: () -> Unit = {},
        val deviceName: String = ""
    )
}
