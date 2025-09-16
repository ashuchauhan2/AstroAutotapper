package com.astro.autotapper.repository

import com.astro.autotapper.model.TapTarget
import com.astro.autotapper.service.AutoTapAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoTapRepository @Inject constructor() {
    
    private val _currentTarget = MutableStateFlow<TapTarget?>(null)
    val currentTarget: StateFlow<TapTarget?> = _currentTarget.asStateFlow()
    
    private val _timerInterval = MutableStateFlow(5L) // Default 5 seconds
    val timerInterval: StateFlow<Long> = _timerInterval.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    fun setTarget(target: TapTarget) {
        _currentTarget.value = target
    }
    
    fun setTimerInterval(intervalSeconds: Long) {
        _timerInterval.value = intervalSeconds
    }
    
    fun startAutoTap() {
        val target = _currentTarget.value
        val interval = _timerInterval.value
        
        if (target != null && interval > 0) {
            AutoTapAccessibilityService.startAutoTap(target, interval * 1000) // Convert to milliseconds
            _isRunning.value = AutoTapAccessibilityService.isAutoTapRunning()
        }
    }
    
    fun stopAutoTap() {
        AutoTapAccessibilityService.stopAutoTap()
        _isRunning.value = AutoTapAccessibilityService.isAutoTapRunning()
    }
    
    fun canStart(): Boolean {
        return _currentTarget.value != null && 
               _timerInterval.value > 0 && 
               AutoTapAccessibilityService.isServiceRunning()
    }
    
    fun isAccessibilityServiceRunning(): Boolean {
        return AutoTapAccessibilityService.isServiceRunning()
    }
    
    fun refreshRunningState() {
        _isRunning.value = AutoTapAccessibilityService.isAutoTapRunning()
    }
}
