package com.astro.autotapper.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.WindowInsets

object CoordinateUtils {
    private const val TAG = "CoordinateUtils"

    /**
     * Gets the real screen size including navigation bars and status bars
     */
    fun getRealScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            size.x = bounds.width()
            size.y = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            display.getRealSize(size)
        }
        
        Log.d(TAG, "Real screen size: ${size.x}x${size.y}")
        return size
    }

    /**
     * Gets the usable screen size (excluding system UI)
     */
    fun getUsableScreenSize(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            size.x = metrics.bounds.width() - insets.left - insets.right
            size.y = metrics.bounds.height() - insets.top - insets.bottom
        } else {
            @Suppress("DEPRECATION")
            display.getSize(size)
        }
        
        Log.d(TAG, "Usable screen size: ${size.x}x${size.y}")
        return size
    }

    /**
     * Validates coordinates are within screen bounds and clamps if necessary
     */
    fun validateAndClampCoordinates(context: Context, x: Float, y: Float): Pair<Float, Float> {
        // Use simple display metrics instead of complex window metrics
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = android.util.DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        val clampedX = x.coerceIn(0f, displayMetrics.widthPixels.toFloat())
        val clampedY = y.coerceIn(0f, displayMetrics.heightPixels.toFloat())
        
        if (clampedX != x || clampedY != y) {
            Log.w(TAG, "Coordinates clamped from ($x, $y) to ($clampedX, $clampedY)")
        } else {
            Log.d(TAG, "Coordinates validated: ($x, $y)")
        }
        
        return Pair(clampedX, clampedY)
    }

    /**
     * Gets display density for coordinate scaling if needed
     */
    fun getDisplayDensity(context: Context): Float {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        
        Log.d(TAG, "Display density: ${displayMetrics.density}")
        return displayMetrics.density
    }

    /**
     * Logs detailed coordinate and display information for debugging
     */
    fun logDisplayInfo(context: Context, tag: String) {
        val realSize = getRealScreenSize(context)
        val usableSize = getUsableScreenSize(context)
        val density = getDisplayDensity(context)
        
        val info = """
            Display Information:
            - Real size: ${realSize.x}x${realSize.y}
            - Usable size: ${usableSize.x}x${usableSize.y}
            - Density: $density
            - System UI offset: ${realSize.x - usableSize.x}x${realSize.y - usableSize.y}
        """.trimIndent()
        
        Log.d(tag, info)
    }
}

