package com.astro.autotapper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astro.autotapper.model.TapTarget
import com.astro.autotapper.repository.AutoTapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TargetSelectionViewModel @Inject constructor(
    private val autoTapRepository: AutoTapRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()
    
    data class UiState(
        val selectedTarget: TapTarget? = null
    )
    
    sealed class Event {
        object TargetConfirmed : Event()
    }
    
    fun setSelectedTarget(target: TapTarget) {
        _uiState.value = _uiState.value.copy(selectedTarget = target)
    }
    
    fun confirmTarget() {
        val target = _uiState.value.selectedTarget
        if (target != null) {
            autoTapRepository.setTarget(target)
            viewModelScope.launch {
                _events.emit(Event.TargetConfirmed)
            }
        }
    }
}








