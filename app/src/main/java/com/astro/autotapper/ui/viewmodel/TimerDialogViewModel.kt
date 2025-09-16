package com.astro.autotapper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class TimerDialogViewModel @Inject constructor(
    private val autoTapRepository: AutoTapRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()
    
    data class UiState(
        val currentInterval: Long = 5L
    )
    
    sealed class Event {
        object IntervalUpdated : Event()
    }
    
    init {
        viewModelScope.launch {
            autoTapRepository.timerInterval.collect { interval ->
                _uiState.value = _uiState.value.copy(currentInterval = interval)
            }
        }
    }
    
    fun setTimerInterval(intervalSeconds: Long) {
        autoTapRepository.setTimerInterval(intervalSeconds)
        viewModelScope.launch {
            _events.emit(Event.IntervalUpdated)
        }
    }
}








