package com.astro.autotapper.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.astro.autotapper.databinding.ActivityTimerDialogBinding
import com.astro.autotapper.ui.viewmodel.TimerDialogViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TimerDialogActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTimerDialogBinding
    private val viewModel: TimerDialogViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.apply {
            btnCancel.setOnClickListener {
                finish()
            }
            
            btnApply.setOnClickListener {
                val interval = etInterval.text.toString().toLongOrNull()
                if (interval != null && interval > 0) {
                    viewModel.setTimerInterval(interval)
                }
            }
            
            // Preset buttons
            btn1s.setOnClickListener { setPresetInterval(1) }
            btn3s.setOnClickListener { setPresetInterval(3) }
            btn5s.setOnClickListener { setPresetInterval(5) }
            btn10s.setOnClickListener { setPresetInterval(10) }
            btn30s.setOnClickListener { setPresetInterval(30) }
            btn60s.setOnClickListener { setPresetInterval(60) }
        }
    }
    
    private fun setPresetInterval(seconds: Long) {
        binding.etInterval.setText(seconds.toString())
        viewModel.setTimerInterval(seconds)
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
        
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                handleEvent(event)
            }
        }
    }
    
    private fun updateUI(state: TimerDialogViewModel.UiState) {
        binding.etInterval.setText(state.currentInterval.toString())
    }
    
    private fun handleEvent(event: TimerDialogViewModel.Event) {
        when (event) {
            is TimerDialogViewModel.Event.IntervalUpdated -> {
                finish()
            }
        }
    }
}








