package com.abdullahsolutions.pricecalculator

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "price_calculator_settings"

        private const val KEY_PROFIT_1 = "profit_1"
        private const val KEY_PROFIT_2 = "profit_2"
        private const val KEY_LOSS_1 = "loss_1"
        private const val KEY_LOSS_2 = "loss_2"
        private const val KEY_MAX_TRADE_VALUE = "max_trade_value"

        // Default values
        const val DEFAULT_PROFIT_1 = 20
        const val DEFAULT_PROFIT_2 = 10
        const val DEFAULT_LOSS_1 = 20
        const val DEFAULT_LOSS_2 = 30
        const val DEFAULT_MAX_TRADE_VALUE = 50.0
    }

    var profit1: Int
        get() = prefs.getInt(KEY_PROFIT_1, DEFAULT_PROFIT_1)
        set(value) = prefs.edit().putInt(KEY_PROFIT_1, value).apply()

    var profit2: Int
        get() = prefs.getInt(KEY_PROFIT_2, DEFAULT_PROFIT_2)
        set(value) = prefs.edit().putInt(KEY_PROFIT_2, value).apply()

    var loss1: Int
        get() = prefs.getInt(KEY_LOSS_1, DEFAULT_LOSS_1)
        set(value) = prefs.edit().putInt(KEY_LOSS_1, value).apply()

    var loss2: Int
        get() = prefs.getInt(KEY_LOSS_2, DEFAULT_LOSS_2)
        set(value) = prefs.edit().putInt(KEY_LOSS_2, value).apply()

    var maxTradeValue: Double
        get() = prefs.getFloat(KEY_MAX_TRADE_VALUE, DEFAULT_MAX_TRADE_VALUE.toFloat()).toDouble()
        set(value) = prefs.edit().putFloat(KEY_MAX_TRADE_VALUE, value.toFloat()).apply()

    fun resetToDefaults() {
        profit1 = DEFAULT_PROFIT_1
        profit2 = DEFAULT_PROFIT_2
        loss1 = DEFAULT_LOSS_1
        loss2 = DEFAULT_LOSS_2
        maxTradeValue = DEFAULT_MAX_TRADE_VALUE
    }
}
