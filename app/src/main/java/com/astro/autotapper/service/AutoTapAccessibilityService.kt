package com.astro.autotapper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.astro.autotapper.model.TapTarget

class AutoTapAccessibilityService : AccessibilityService() {
    
    private val handler = Handler(Looper.getMainLooper())
    private var tapRunnable: Runnable? = null
    private var isRunning = false
    
    companion object {
        private const val TAG = "AutoTapAccessibilityService"
        private var instance: AutoTapAccessibilityService? = null
        
        fun startAutoTap(target: TapTarget, intervalMillis: Long) {
            Log.d(TAG, "Attempting to start auto tap at (${target.x}, ${target.y}) with interval ${intervalMillis}ms")
            instance?.startTapping(target, intervalMillis)
        }
        
        fun stopAutoTap() {
            Log.d(TAG, "Stopping auto tap")
            instance?.stopTapping()
        }
        
        fun isServiceRunning(): Boolean {
            return instance != null
        }
        
        fun isAutoTapRunning(): Boolean {
            return instance?.isRunning ?: false
        }
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
        instance = this
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Accessibility service destroyed")
        super.onDestroy()
        instance = null
        stopTapping()
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to handle accessibility events for our use case
        // Just log that we're receiving events to confirm service is working
        if (event != null) {
            Log.v(TAG, "Received accessibility event: ${event.eventType}")
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
        stopTapping()
    }
    
    private fun startTapping(target: TapTarget, intervalMillis: Long) {
        Log.d(TAG, "Starting tapping at (${target.x}, ${target.y}) every ${intervalMillis}ms")
        stopTapping() // Stop any existing tapping
        
        tapRunnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    performTap(target.x, target.y)
                    handler.postDelayed(this, intervalMillis)
                }
            }
        }
        
        isRunning = true
        handler.post(tapRunnable!!)
    }
    
    private fun stopTapping() {
        Log.d(TAG, "Stopping tapping")
        isRunning = false
        tapRunnable?.let { handler.removeCallbacks(it) }
        tapRunnable = null
    }
    
    private fun performTap(x: Float, y: Float) {
        try {
            // Simple coordinate validation without excessive logging
            val displayMetrics = resources.displayMetrics
            val clampedX = x.coerceIn(0f, displayMetrics.widthPixels.toFloat())
            val clampedY = y.coerceIn(0f, displayMetrics.heightPixels.toFloat())
            
            Log.d(TAG, "Performing tap at ($clampedX, $clampedY)")
            
            val path = Path().apply {
                moveTo(clampedX, clampedY)
            }
            
            val gestureBuilder = GestureDescription.Builder()
            val stroke = GestureDescription.StrokeDescription(path, 0, 100)
            gestureBuilder.addStroke(stroke)
            
            val gesture = gestureBuilder.build()
            
            val startTime = System.currentTimeMillis()
            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    val duration = System.currentTimeMillis() - startTime
                    Log.d(TAG, "Tap completed successfully at ($clampedX, $clampedY) in ${duration}ms")
                }
                
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    val duration = System.currentTimeMillis() - startTime
                    Log.w(TAG, "Tap was cancelled at ($clampedX, $clampedY) after ${duration}ms")
                }
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing tap at ($x, $y)", e)
        }
    }
}
