package com.astro.autotapper.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.astro.autotapper.ui.TargetSelectionActivity
import com.astro.autotapper.ui.TimerDialogActivity
import com.astro.autotapper.model.TapTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class OverlayService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var displayMetrics: DisplayMetrics
    
    // UI Components
    private lateinit var btnClose: android.widget.Button
    private lateinit var btnSelectTarget: android.widget.Button
    private lateinit var btnTimer: android.widget.Button
    private lateinit var btnStartStop: android.widget.Button
    
    // Separate target selection overlay
    private var targetSelectionView: View? = null
    private var targetSelectionParams: WindowManager.LayoutParams? = null
    private lateinit var targetCrosshair: android.widget.ImageView
    
    // Countdown timer overlay
    private var countdownView: View? = null
    private var countdownParams: WindowManager.LayoutParams? = null
    private lateinit var countdownText: android.widget.TextView
    private var countdownTimer: android.os.CountDownTimer? = null
    
    // State
    private var currentTarget: android.graphics.PointF? = null
    private var timerInterval: Long = 5L // seconds
    private var isAutoTapRunning: Boolean = false
    private var isTargetSelectionMode: Boolean = false
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        try {
                    android.util.Log.d("OverlayService", "Creating overlay service")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        android.util.Log.d("OverlayService", "Display metrics: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}, density: ${displayMetrics.density}")
        
        // Basic display info logging
        
        createOverlay()
            android.util.Log.d("OverlayService", "Overlay service created successfully")
        } catch (e: Exception) {
            android.util.Log.e("OverlayService", "Error creating overlay service", e)
            stopSelf()
        }
    }
    
    private fun createOverlay() {
        try {
            android.util.Log.d("OverlayService", "Creating modern AutoTapper overlay")
            
            // Create only the control panel
            overlayView = createModernControlPanel()
            
            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 20
                y = 100
            }
            
            android.util.Log.d("OverlayService", "Adding modern overlay to window manager")
            windowManager.addView(overlayView, layoutParams)
            
            setupClickListeners()
            updateUI()
            createCountdownTimer()
            
            android.util.Log.d("OverlayService", "Modern overlay created successfully")
        } catch (e: Exception) {
            android.util.Log.e("OverlayService", "Error creating overlay", e)
            android.util.Log.e("OverlayService", "Stack trace: ${android.util.Log.getStackTraceString(e)}")
            throw e
        }
    }
    
    private fun createModernControlPanel(): android.widget.LinearLayout {
        return android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            background = createRoundedBackground()
            setPadding(30, 30, 30, 30) // Increased padding for larger UI
            elevation = 10f
            
            // Create modern buttons with clearer text
            btnClose = createModernButton("EXIT", "#FF5722")
            btnSelectTarget = createModernButton("TARGET", "#2196F3")
            btnTimer = createModernButton("TIMER", "#FF9800")
            btnStartStop = createModernButton("START", "#4CAF50")
            
            addView(btnClose)
            addView(btnSelectTarget)
            addView(btnTimer)
            addView(btnStartStop)
        }
    }
    
    private fun createModernButton(text: String, colorHex: String): android.widget.Button {
        return android.widget.Button(this).apply {
            this.text = text
            textSize = 12f // Further reduced for perfect fit
            setTextColor(android.graphics.Color.WHITE)
            background = createRoundedButtonBackground(colorHex)
            elevation = 6f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            // Keep the larger button size but reduce text
            layoutParams = android.widget.LinearLayout.LayoutParams(160, 70).apply { // Width: 160dp, Height: 70dp
                setMargins(0, 12, 0, 12)
                gravity = android.view.Gravity.CENTER
            }
            // Ensure text is centered
            gravity = android.view.Gravity.CENTER
            // Further reduce padding to maximize text space
            setPadding(8, 10, 8, 10)
            // Ensure text is always visible
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
        }
    }
    
    private fun createRoundedButtonBackground(colorHex: String): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(android.graphics.Color.parseColor(colorHex))
            cornerRadius = 12f // Rounded corners like Material Design
            setStroke(2, android.graphics.Color.WHITE)
        }
    }
    
    private fun createCircularButtonBackground(colorHex: String): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(android.graphics.Color.parseColor(colorHex))
            setStroke(2, android.graphics.Color.WHITE)
        }
    }
    
    private fun createRoundedBackground(): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(android.graphics.Color.parseColor("#E6000000"))
            cornerRadius = 20f
            setStroke(2, android.graphics.Color.parseColor("#33FFFFFF"))
        }
    }
    
    private fun createCrosshairDrawable(): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(android.graphics.Color.parseColor("#80FF0000"))
            setStroke(4, android.graphics.Color.RED)
        }
    }
    
    private fun setupClickListeners() {
        android.util.Log.d("OverlayService", "Setting up click listeners")
        
        btnClose.setOnClickListener {
            android.util.Log.d("OverlayService", "Close button clicked")
            stopSelf()
        }
        
        btnSelectTarget.setOnClickListener {
            android.util.Log.d("OverlayService", "Select Target button clicked")
            enterTargetSelectionMode()
        }
        
        btnTimer.setOnClickListener { view ->
            android.util.Log.d("OverlayService", "Timer button clicked - current interval: ${timerInterval}s")
            showTimerInputDialog()
        }
        
        btnStartStop.setOnClickListener {
            android.util.Log.d("OverlayService", "Start/Stop button clicked")
            toggleAutoTap()
        }
        
        // Add touch debugging for timer button
        btnTimer.setOnTouchListener { view, event ->
            android.util.Log.d("OverlayService", "Timer button touched - action: ${event.action}")
            false // Return false to allow click event to proceed
        }
        
        android.util.Log.d("OverlayService", "Click listeners set up successfully")
    }
    
    private fun enterTargetSelectionMode() {
        isTargetSelectionMode = true
        createTargetSelectionOverlay()
        android.widget.Toast.makeText(this, "Tap anywhere to set target location", android.widget.Toast.LENGTH_LONG).show()
    }
    
    private fun exitTargetSelectionMode() {
        isTargetSelectionMode = false
        removeTargetSelectionOverlay()
        updateUI()
    }
    
    private fun createTargetSelectionOverlay() {
        // Create fullscreen overlay for target selection
        val targetOverlay = android.widget.FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#80000000"))
            setOnTouchListener { view, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    // Use raw coordinates for consistency with gesture dispatch
                    val x = event.rawX
                    val y = event.rawY
                    
                    android.util.Log.d("OverlayService", "Touch at raw coords: ($x, $y)")
                    
                    // Move crosshair to touch location
                    updateCrosshairPosition(event.x, event.y)
                    
                    // Show confirmation dialog
                    showTargetConfirmationDialog(x, y)
                    true
                } else false
            }
        }
        
        // Create crosshair
        targetCrosshair = android.widget.ImageView(this).apply {
            setImageDrawable(createCrosshairDrawable())
            layoutParams = android.widget.FrameLayout.LayoutParams(60, 60).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        
        // Create instruction text
        val instructionText = android.widget.TextView(this).apply {
            text = "Tap anywhere to set target location"
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(20, 20, 20, 20)
            background = createRoundedBackground()
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
                setMargins(0, 100, 0, 0)
            }
        }
        
        targetOverlay.addView(targetCrosshair)
        targetOverlay.addView(instructionText)
        
        targetSelectionParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        targetSelectionView = targetOverlay
        windowManager.addView(targetSelectionView, targetSelectionParams)
    }
    
    private fun removeTargetSelectionOverlay() {
        targetSelectionView?.let { view ->
            windowManager.removeView(view)
            targetSelectionView = null
            targetSelectionParams = null
        }
    }
    
    private fun updateCrosshairPosition(x: Float, y: Float) {
        targetCrosshair.layoutParams = (targetCrosshair.layoutParams as android.widget.FrameLayout.LayoutParams).apply {
            leftMargin = (x - 30).toInt() // Center the 60px crosshair
            topMargin = (y - 30).toInt()
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
        }
        targetCrosshair.requestLayout()
    }
    
    private fun showTargetConfirmationDialog(x: Float, y: Float) {
        // Create confirmation dialog overlay
        val confirmationOverlay = android.widget.FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#00000000")) // Transparent
        }
        
        // Create confirmation dialog
        val dialogContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            background = createRoundedBackground()
            setPadding(50, 50, 50, 50) // Much more padding
            elevation = 12f
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        
        // Title
        val titleText = android.widget.TextView(this).apply {
            text = "Confirm Target"
            textSize = 20f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        
        // Coordinates text  
        val coordText = android.widget.TextView(this).apply {
            text = "Position: (${x.toInt()}, ${y.toInt()})"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }
        
        // Button container
        val buttonContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
        }
        
        // Exit button
        val exitButton = android.widget.Button(this).apply {
            text = "Exit"
            textSize = 16f // Further reduced for better fit
            setTextColor(android.graphics.Color.WHITE)
            background = createRectangularButtonBackground("#FF5722")
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding(18, 12, 18, 12) // Further reduced padding
            // Keep button size but reduce text
            layoutParams = android.widget.LinearLayout.LayoutParams(200, 80).apply {
                setMargins(0, 0, 30, 0)
            }
            // Add text shadow for better visibility
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
            setOnClickListener {
                // Just remove the confirmation dialog, keep target selection open
                confirmationOverlay.parent?.let {
                    windowManager.removeView(confirmationOverlay)
                }
            }
        }
        
        // Confirm button
        val confirmButton = android.widget.Button(this).apply {
            text = "OK"
            textSize = 16f // Further reduced for better fit
            setTextColor(android.graphics.Color.WHITE)
            background = createRectangularButtonBackground("#4CAF50")
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding(18, 12, 18, 12) // Further reduced padding
            // Keep button size but reduce text
            layoutParams = android.widget.LinearLayout.LayoutParams(200, 80)
            // Add text shadow for better visibility
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
            setOnClickListener {
                setTarget(x, y)
                // Remove both confirmation and target selection overlays
                confirmationOverlay.parent?.let {
                    windowManager.removeView(confirmationOverlay)
                }
                exitTargetSelectionMode()
            }
        }
        
        buttonContainer.addView(exitButton)
        buttonContainer.addView(confirmButton)
        
        dialogContainer.addView(titleText)
        dialogContainer.addView(coordText)
        dialogContainer.addView(buttonContainer)
        
        confirmationOverlay.addView(dialogContainer)
        
        // Add confirmation overlay on top
        val confirmationParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(confirmationOverlay, confirmationParams)
    }
    
    private fun setTarget(x: Float, y: Float) {
        currentTarget = android.graphics.PointF(x, y)
        android.util.Log.d("OverlayService", "Target set to: ($x, $y)")
        android.widget.Toast.makeText(this, "Target set: (${x.toInt()}, ${y.toInt()})", android.widget.Toast.LENGTH_SHORT).show()
        updateUI()
    }
    
    private fun showTimerInputDialog() {
        android.util.Log.d("OverlayService", "Showing timer input dialog")
        
        // Create dialog overlay
        val dialogOverlay = android.widget.FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.parseColor("#80000000")) // Semi-transparent dark
        }
        
        // Create dialog container
        val dialogContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            background = createRoundedBackground()
            setPadding(100, 100, 100, 100) // Even more padding
            elevation = 12f
            layoutParams = android.widget.FrameLayout.LayoutParams(
                800, // Much larger width for bigger buttons
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        
        // Instruction text
        val instructionText = android.widget.TextView(this).apply {
            text = "Enter an interval in seconds here"
            textSize = 26f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 50)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        // Input field
        val inputField = android.widget.EditText(this).apply {
            setText(timerInterval.toString())
            textSize = 22f
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            hint = "0"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            background = createInputFieldBackground()
            setPadding(40, 60, 40, 60)
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                200
            ).apply {
                setMargins(0, 0, 0, 50)
            }
            selectAll() // Select all text for easy editing
            // Enable focus for input
            isFocusable = true
            isFocusableInTouchMode = true
        }
        
        // Button container
        val buttonContainer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
        }
        
        // Exit button
        val exitButton = android.widget.Button(this).apply {
            text = "Exit"
            textSize = 18f // Further reduced for better fit
            setTextColor(android.graphics.Color.WHITE)
            background = createRectangularButtonBackground("#FF5722")
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding(25, 18, 25, 18) // Further reduced padding
            // Keep larger button size but reduce text
            layoutParams = android.widget.LinearLayout.LayoutParams(250, 100).apply {
                setMargins(0, 0, 40, 0)
            }
            // Add text shadow for visibility
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
            setOnClickListener {
                windowManager.removeView(dialogOverlay)
            }
        }
        
        // Confirm button
        val confirmButton = android.widget.Button(this).apply {
            text = "OK"
            textSize = 18f // Further reduced for better fit
            setTextColor(android.graphics.Color.WHITE)
            background = createRectangularButtonBackground("#4CAF50")
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding(25, 18, 25, 18) // Further reduced padding
            // Keep larger button size but reduce text
            layoutParams = android.widget.LinearLayout.LayoutParams(250, 100)
            // Add text shadow for visibility
            setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
            setOnClickListener {
                val inputText = inputField.text.toString()
                val newInterval = inputText.toLongOrNull()
                
                if (newInterval != null && newInterval > 0) {
                    setTimerInterval(newInterval)
                    windowManager.removeView(dialogOverlay)
                } else {
                    android.widget.Toast.makeText(this@OverlayService, "Please enter a valid number", android.widget.Toast.LENGTH_SHORT).show()
                    inputField.requestFocus()
                }
            }
        }
        
        buttonContainer.addView(exitButton)
        buttonContainer.addView(confirmButton)
        
        // Add all components to dialog
        dialogContainer.addView(instructionText)
        dialogContainer.addView(inputField)
        dialogContainer.addView(buttonContainer)
        
        dialogOverlay.addView(dialogContainer)
        
        // Add dialog overlay - Make it properly focusable for keyboard input
        val dialogParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            0, // Remove all blocking flags
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(dialogOverlay, dialogParams)
        
        // Set up input field to be focusable and force keyboard
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            inputField.requestFocus()
            inputField.requestFocusFromTouch()
            
            val inputMethodManager = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            
            // Try multiple methods to show keyboard
            inputMethodManager.showSoftInput(inputField, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            inputMethodManager.showSoftInput(inputField, android.view.inputmethod.InputMethodManager.SHOW_FORCED)
            
            // Make the input field click programmatically to trigger keyboard
            inputField.performClick()
        }, 300)
    }
    
    private fun createInputFieldBackground(): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(android.graphics.Color.parseColor("#33FFFFFF"))
            cornerRadius = 10f
            setStroke(2, android.graphics.Color.parseColor("#66FFFFFF"))
        }
    }
    
    private fun createRectangularButtonBackground(colorHex: String): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(android.graphics.Color.parseColor(colorHex))
            cornerRadius = 15f
            setStroke(2, android.graphics.Color.WHITE)
        }
    }
    
    private fun setTimerInterval(newInterval: Long) {
        val oldInterval = timerInterval
        timerInterval = newInterval
        android.util.Log.d("OverlayService", "Timer interval changed from ${oldInterval}s to ${timerInterval}s")
        updateUI()
        
        // If auto-tap is running, restart countdown with new interval
        if (isAutoTapRunning) {
            startCountdown()
        }
        
        android.widget.Toast.makeText(this, "Timer set to ${timerInterval} seconds", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun toggleAutoTap() {
        if (isAutoTapRunning) {
            stopAutoTap()
        } else {
            startAutoTap()
        }
    }
    
    private fun startAutoTap() {
        currentTarget?.let { target ->
            val targetPoint = TapTarget(target.x, target.y)
            AutoTapAccessibilityService.startAutoTap(targetPoint, timerInterval * 1000)
            isAutoTapRunning = true
            updateUI()
            startCountdown() // Start the countdown timer
            android.widget.Toast.makeText(this, "Auto-tap started", android.widget.Toast.LENGTH_SHORT).show()
            android.util.Log.d("OverlayService", "Auto-tap started at (${target.x}, ${target.y}) every ${timerInterval}s")
        } ?: run {
            android.widget.Toast.makeText(this, "Please select a target first", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopAutoTap() {
        AutoTapAccessibilityService.stopAutoTap()
        isAutoTapRunning = false
        updateUI()
        stopCountdown() // Stop the countdown timer
        android.widget.Toast.makeText(this, "Auto-tap stopped", android.widget.Toast.LENGTH_SHORT).show()
        android.util.Log.d("OverlayService", "Auto-tap stopped")
    }
    
    private fun updateUI() {
        android.util.Log.d("OverlayService", "Updating UI - timer: ${timerInterval}s, target: ${currentTarget != null}, running: $isAutoTapRunning")
        
        // Update target button
        btnSelectTarget.text = if (currentTarget != null) "TARGET ✓" else "TARGET"
        
        // Update timer button to show current interval
        btnTimer.text = "${timerInterval}s"
        android.util.Log.d("OverlayService", "Timer button text set to: ${btnTimer.text}")
        
        // Update start/stop button
        btnStartStop.text = if (isAutoTapRunning) "STOP" else "START"
        btnStartStop.background = createRoundedButtonBackground(
            if (isAutoTapRunning) "#F44336" else "#4CAF50"
        )
        
        // Enable/disable start button based on target selection
        btnStartStop.isEnabled = currentTarget != null || isAutoTapRunning
        
        android.util.Log.d("OverlayService", "UI update completed")
    }
    
    private fun createDragTouchListener(): android.view.View.OnTouchListener {
        return object : android.view.View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            
            override fun onTouch(view: android.view.View, event: android.view.MotionEvent): Boolean {
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    android.view.MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(view, layoutParams)
                        return true
                    }
                }
                return false
            }
        }
    }
    
    private fun createCountdownTimer() {
        try {
            android.util.Log.d("OverlayService", "Creating countdown timer overlay")
            
            // Create countdown container
            val countdownContainer = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                background = createCountdownBackground()
                setPadding(20, 12, 20, 12)
                elevation = 8f
            }
            
            // Create countdown text
            countdownText = android.widget.TextView(this).apply {
                text = "Ready"
                textSize = 18f
                setTextColor(android.graphics.Color.WHITE)
                typeface = android.graphics.Typeface.MONOSPACE
                gravity = android.view.Gravity.CENTER
                setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
            }
            
            countdownContainer.addView(countdownText)
            
            // Create layout parameters for countdown
            countdownParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 50  // Position it away from the main control panel
                y = 200
            }
            
            countdownView = countdownContainer
            countdownContainer.setOnTouchListener(createCountdownDragListener())
            
            windowManager.addView(countdownView, countdownParams)
            
            android.util.Log.d("OverlayService", "Countdown timer overlay created successfully")
        } catch (e: Exception) {
            android.util.Log.e("OverlayService", "Error creating countdown timer", e)
        }
    }
    
    private fun createCountdownBackground(): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(android.graphics.Color.parseColor("#CC000000")) // Semi-transparent black
            cornerRadius = 12f
            setStroke(2, android.graphics.Color.parseColor("#FF4CAF50")) // Green border
        }
    }
    
    private fun createCountdownDragListener(): android.view.View.OnTouchListener {
        return object : android.view.View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            
            override fun onTouch(view: android.view.View, event: android.view.MotionEvent): Boolean {
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        countdownParams?.let { params ->
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                        }
                        return true
                    }
                    android.view.MotionEvent.ACTION_MOVE -> {
                        countdownParams?.let { params ->
                            params.x = initialX + (event.rawX - initialTouchX).toInt()
                            params.y = initialY + (event.rawY - initialTouchY).toInt()
                            windowManager.updateViewLayout(view, params)
                        }
                        return true
                    }
                }
                return false
            }
        }
    }
    
    private fun removeCountdownTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
        countdownView?.let { view ->
            windowManager.removeView(view)
            countdownView = null
            countdownParams = null
        }
    }
    
    private fun startCountdown() {
        countdownTimer?.cancel()
        
        val intervalMs = timerInterval * 1000
        countdownTimer = object : android.os.CountDownTimer(intervalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                countdownText.text = "${secondsRemaining}s"
            }
            
            override fun onFinish() {
                countdownText.text = "Tap!"
                // Restart the countdown for the next tap
                if (isAutoTapRunning) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (isAutoTapRunning) {
                            startCountdown()
                        }
                    }, 500) // Brief pause to show "Tap!" message
                }
            }
        }.start()
    }
    
    private fun stopCountdown() {
        countdownTimer?.cancel()
        countdownTimer = null
        countdownText.text = "Ready"
    }

    // Modern overlay with integrated target selection and timer controls
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        
        // Clean up overlays
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        removeTargetSelectionOverlay()
        removeCountdownTimer()
        
        // Stop auto-tap if running
        if (isAutoTapRunning) {
            AutoTapAccessibilityService.stopAutoTap()
        }
    }
}

