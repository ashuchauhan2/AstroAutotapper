package com.astro.autotapper.ui.viewmodel

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astro.autotapper.repository.PermissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val permissionRepository: PermissionRepository
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()
    
    data class UiState(
        val hasOverlayPermission: Boolean = false,
        val hasAccessibilityPermission: Boolean = false,
        val canStartOverlay: Boolean = false
    )
    
    sealed class Event {
        object RequestOverlayPermission : Event()
        object StartOverlayService : Event()
        data class ShowError(val message: String) : Event()
    }
    
    init {
        checkPermissions()
    }
    
    fun checkPermissions() {
        val hasOverlay = Settings.canDrawOverlays(getApplication())
        val hasAccessibility = permissionRepository.isAccessibilityServiceEnabled()
        
        _uiState.value = _uiState.value.copy(
            hasOverlayPermission = hasOverlay,
            hasAccessibilityPermission = hasAccessibility,
            canStartOverlay = hasOverlay && hasAccessibility
        )
    }
    
    fun onStartOverlayClicked() {
        viewModelScope.launch {
            when {
                !_uiState.value.hasOverlayPermission -> {
                    _events.emit(Event.RequestOverlayPermission)
                }
                !_uiState.value.hasAccessibilityPermission -> {
                    _events.emit(Event.ShowError("Please enable accessibility service first"))
                }
                else -> {
                    _events.emit(Event.StartOverlayService)
                }
            }
        }
    }
    
    fun onOverlayPermissionGranted() {
        checkPermissions()
    }
}

