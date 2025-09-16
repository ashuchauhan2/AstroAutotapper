package com.astro.autotapper.service

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class OverlayTouchListener(
    private val windowManager: WindowManager,
    private val layoutParams: WindowManager.LayoutParams
) : View.OnTouchListener {
    
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(view, layoutParams)
                return true
            }
        }
        return false
    }
}








