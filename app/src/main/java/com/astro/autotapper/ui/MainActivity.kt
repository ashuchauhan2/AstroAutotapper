package com.astro.autotapper.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.astro.autotapper.databinding.ActivityMainBinding
import com.astro.autotapper.service.OverlayService
import com.astro.autotapper.ui.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            viewModel.onOverlayPermissionGranted()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.apply {
            btnStartOverlay.setOnClickListener {
                viewModel.onStartOverlayClicked()
            }
            
            btnPermissions.setOnClickListener {
                requestOverlayPermission()
            }
            
            btnAccessibilitySettings.setOnClickListener {
                openAccessibilitySettings()
            }
            
            // Long press for debug info
            btnPermissions.setOnLongClickListener {
                showDebugInfo()
                true
            }
            
            // Long press accessibility button to test overlay directly
            btnAccessibilitySettings.setOnLongClickListener {
                testOverlayDirectly()
                true
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
    
    private fun updateUI(state: MainViewModel.UiState) {
        binding.apply {
            btnStartOverlay.isEnabled = state.canStartOverlay
            btnPermissions.isEnabled = !state.hasOverlayPermission
            
            tvPermissionStatus.text = when {
                state.hasOverlayPermission && state.hasAccessibilityPermission -> 
                    "✅ All permissions granted"
                state.hasOverlayPermission -> 
                    "⚠️ Accessibility permission needed"
                else -> 
                    "❌ Overlay permission needed"
            }
        }
    }
    
    private fun handleEvent(event: MainViewModel.Event) {
        when (event) {
            is MainViewModel.Event.RequestOverlayPermission -> {
                requestOverlayPermission()
            }
            is MainViewModel.Event.StartOverlayService -> {
                startOverlayService()
                minimizeApp()
            }
            is MainViewModel.Event.ShowError -> {
                showErrorDialog(event.message)
            }
        }
    }
    
    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }
    
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
    
    private fun startOverlayService() {
        try {
            android.util.Log.d("MainActivity", "Starting overlay service")
            val intent = Intent(this, OverlayService::class.java)
            startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error starting overlay service", e)
            showErrorDialog("Failed to start overlay service: ${e.message}")
        }
    }
    
    private fun minimizeApp() {
        moveTaskToBack(true)
    }
    
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission Required")
            .setMessage("Overlay permission is required for the app to work properly.")
            .setPositiveButton("Try Again") { _, _ ->
                requestOverlayPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showDebugInfo() {
        val overlayPermission = Settings.canDrawOverlays(this)
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        val serviceInfo = enabledServices.joinToString("\n") { 
            "${it.resolveInfo?.serviceInfo?.packageName}/${it.resolveInfo?.serviceInfo?.name}"
        }
        
        val debugMessage = """
            Debug Info:
            Overlay Permission: $overlayPermission
            Package Name: $packageName
            
            Enabled Accessibility Services:
            $serviceInfo
            
            Looking for: $packageName/com.astro.autotapper.service.AutoTapAccessibilityService
        """.trimIndent()
        
        android.util.Log.d("MainActivity", debugMessage)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Debug Information")
            .setMessage(debugMessage)
            .setPositiveButton("Copy to Clipboard") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Debug Info", debugMessage)
                clipboard.setPrimaryClip(clip)
            }
            .setNegativeButton("OK", null)
            .show()
    }
    
    private fun testOverlayDirectly() {
        android.util.Log.d("MainActivity", "Testing overlay directly...")
        try {
            val intent = Intent(this, OverlayService::class.java)
            startService(intent)
            android.util.Log.d("MainActivity", "Overlay service started directly")
            
            MaterialAlertDialogBuilder(this)
                .setTitle("Overlay Test")
                .setMessage("Look for a red TEST OVERLAY button on your screen. Check logcat for details.")
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error starting overlay directly", e)
            MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage("Failed to start overlay: ${e.message}")
                .setPositiveButton("OK", null)
                .show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("MainActivity", "onResume - checking permissions")
        viewModel.checkPermissions()
    }
}
