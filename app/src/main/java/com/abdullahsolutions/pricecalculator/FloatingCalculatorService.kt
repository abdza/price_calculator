package com.abdullahsolutions.pricecalculator

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import java.text.DecimalFormat

class FloatingCalculatorService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var collapsedView: View
    private lateinit var expandedView: View
    
    private val df = DecimalFormat("#,##0.0000")

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_calculator, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        windowManager.addView(floatingView, params)

        collapsedView = floatingView.findViewById(R.id.collapsed_view)
        expandedView = floatingView.findViewById(R.id.expanded_view)

        // Initially show collapsed view
        collapsedView.visibility = View.VISIBLE
        expandedView.visibility = View.GONE

        // Setup click to expand
        collapsedView.setOnClickListener {
            collapsedView.visibility = View.GONE
            expandedView.visibility = View.VISIBLE
            // Make focusable when expanded so keyboard works
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            windowManager.updateViewLayout(floatingView, params)
        }

        // Setup collapse button
        floatingView.findViewById<ImageButton>(R.id.btn_collapse).setOnClickListener {
            expandedView.visibility = View.GONE
            collapsedView.visibility = View.VISIBLE
            // Remove focus when collapsed
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(floatingView, params)
        }

        // Setup close button
        floatingView.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            stopSelf()
        }

        // Setup calculator logic
        val inputField = floatingView.findViewById<EditText>(R.id.input_price)
        val profit20 = floatingView.findViewById<TextView>(R.id.value_profit_20)
        val profit10 = floatingView.findViewById<TextView>(R.id.value_profit_10)
        val loss20 = floatingView.findViewById<TextView>(R.id.value_loss_20)
        val loss30 = floatingView.findViewById<TextView>(R.id.value_loss_30)

        inputField.addTextChangedListener { text ->
            val value = text.toString().toDoubleOrNull()
            if (value != null) {
                profit20.text = df.format(value * 1.20)
                profit10.text = df.format(value * 1.10)
                loss20.text = df.format(value * 0.80)
                loss30.text = df.format(value * 0.70)
            } else {
                profit20.text = "—"
                profit10.text = "—"
                loss20.text = "—"
                loss30.text = "—"
            }
        }

        // Setup drag functionality
        setupDrag(floatingView.findViewById(R.id.drag_handle), params)
        setupDrag(collapsedView, params)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDrag(dragView: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val touchSlop = 10 // Minimum movement to be considered a drag

        dragView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()

                    // Only start dragging if moved beyond touch slop
                    if (!isDragging && (kotlin.math.abs(deltaX) > touchSlop || kotlin.math.abs(deltaY) > touchSlop)) {
                        isDragging = true
                    }

                    if (isDragging) {
                        params.x = initialX + deltaX
                        params.y = initialY + deltaY
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // It was a tap, not a drag - trigger click
                        view.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
