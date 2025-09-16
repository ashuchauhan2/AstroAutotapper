
package com.astro.autotapper.ui

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.astro.autotapper.databinding.ActivityTargetSelectionBinding
import com.astro.autotapper.model.TapTarget
import com.astro.autotapper.ui.viewmodel.TargetSelectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TargetSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTargetSelectionBinding
    private val viewModel: TargetSelectionViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTargetSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.apply {
            btnCancel.setOnClickListener {
                finish()
            }
            
            btnConfirm.setOnClickListener {
                viewModel.confirmTarget()
            }
            
            targetSelectionOverlay.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val target = TapTarget(event.rawX, event.rawY)
                    android.util.Log.d("TargetSelection", "Target selected at: (${event.rawX}, ${event.rawY})")
                    viewModel.setSelectedTarget(target)
                    true
                } else {
                    false
                }
            }
        }
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
    
    private fun updateUI(state: TargetSelectionViewModel.UiState) {
        binding.apply {
            btnConfirm.isEnabled = state.selectedTarget != null
            
            if (state.selectedTarget != null) {
                tvInstruction.text = "Target: (${state.selectedTarget.x.toInt()}, ${state.selectedTarget.y.toInt()})"
                
                // Show crosshair at selected position
                crosshair.visibility = android.view.View.VISIBLE
                crosshair.x = state.selectedTarget.x - crosshair.width / 2
                crosshair.y = state.selectedTarget.y - crosshair.height / 2
            } else {
                tvInstruction.text = "Tap anywhere on the screen to set target location"
                crosshair.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun handleEvent(event: TargetSelectionViewModel.Event) {
        when (event) {
            is TargetSelectionViewModel.Event.TargetConfirmed -> {
                finish()
            }
        }
    }
}
