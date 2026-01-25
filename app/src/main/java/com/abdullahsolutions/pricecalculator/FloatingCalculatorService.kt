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
    private lateinit var settingsManager: SettingsManager

    private val df = DecimalFormat("#,##0.0000")

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        settingsManager = SettingsManager(this)

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
            // Keep non-focusable initially so background apps can still receive keyboard input
            // Use FLAG_NOT_TOUCH_MODAL to allow touches outside to pass through
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
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
        val profit1Value = floatingView.findViewById<TextView>(R.id.value_profit_1)
        val profit2Value = floatingView.findViewById<TextView>(R.id.value_profit_2)
        val loss1Value = floatingView.findViewById<TextView>(R.id.value_loss_1)
        val loss2Value = floatingView.findViewById<TextView>(R.id.value_loss_2)
        val maxUnitsValue = floatingView.findViewById<TextView>(R.id.value_max_units)
        val maxTotalValue = floatingView.findViewById<TextView>(R.id.value_max_total)
        val labelMaxTradeSection = floatingView.findViewById<TextView>(R.id.label_max_trade_section)

        // Setup labels from settings
        val labelProfit1 = floatingView.findViewById<TextView>(R.id.label_profit_1)
        val labelProfit2 = floatingView.findViewById<TextView>(R.id.label_profit_2)
        val labelLoss1 = floatingView.findViewById<TextView>(R.id.label_loss_1)
        val labelLoss2 = floatingView.findViewById<TextView>(R.id.label_loss_2)

        // Get percentage values from settings
        val profit1Pct = settingsManager.profit1
        val profit2Pct = settingsManager.profit2
        val loss1Pct = settingsManager.loss1
        val loss2Pct = settingsManager.loss2
        val maxTradeValue = settingsManager.maxTradeValue

        // Update max trade section label
        labelMaxTradeSection.text = "MAX TRADE ($${String.format("%.2f", maxTradeValue)})"

        // Update labels with current settings
        labelProfit1.text = "+${profit1Pct}%"
        labelProfit2.text = "+${profit2Pct}%"
        labelLoss1.text = "-${loss1Pct}%"
        labelLoss2.text = "-${loss2Pct}%"

        // Calculate multipliers
        val profit1Mult = 1 + (profit1Pct / 100.0)
        val profit2Mult = 1 + (profit2Pct / 100.0)
        val loss1Mult = 1 - (loss1Pct / 100.0)
        val loss2Mult = 1 - (loss2Pct / 100.0)

        inputField.addTextChangedListener { text ->
            val value = text.toString().toDoubleOrNull()
            if (value != null && value > 0) {
                profit1Value.text = df.format(value * profit1Mult)
                profit2Value.text = df.format(value * profit2Mult)
                loss1Value.text = df.format(value * loss1Mult)
                loss2Value.text = df.format(value * loss2Mult)

                // Calculate max units and max total
                val maxUnits = (maxTradeValue / value).toInt()
                val maxTotal = maxUnits * value
                maxUnitsValue.text = maxUnits.toString()
                maxTotalValue.text = "$${String.format("%.2f", maxTotal)}"
            } else {
                profit1Value.text = "—"
                profit2Value.text = "—"
                loss1Value.text = "—"
                loss2Value.text = "—"
                maxUnitsValue.text = "—"
                maxTotalValue.text = "—"
            }
        }

        // When user taps the input field, make window focusable so keyboard appears
        inputField.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && expandedView.visibility == View.VISIBLE) {
                // Make focusable so keyboard can appear for our input
                params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                windowManager.updateViewLayout(floatingView, params)
                inputField.requestFocus()
            }
            false // Don't consume the event, let EditText handle it normally
        }

        // When input loses focus, make window non-focusable so background apps can use keyboard
        inputField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && expandedView.visibility == View.VISIBLE) {
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                windowManager.updateViewLayout(floatingView, params)
            }
        }

        // Detect touches outside the floating calculator to release focus
        floatingView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE && expandedView.visibility == View.VISIBLE) {
                // User touched outside, make non-focusable so background apps can get keyboard
                inputField.clearFocus()
                params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                windowManager.updateViewLayout(floatingView, params)
            }
            false
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
