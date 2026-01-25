package com.abdullahsolutions.pricecalculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            startFloatingService()
        } else {
            Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)
        enableEdgeToEdge()
        setContent {
            PriceCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D1117)
                ) {
                    var showSettings by remember { mutableStateOf(false) }

                    if (showSettings) {
                        SettingsScreen(
                            settingsManager = settingsManager,
                            onBack = { showSettings = false }
                        )
                    } else {
                        LauncherScreen(
                            onLaunchFloat = { checkOverlayPermissionAndStart() },
                            onOpenSettings = { showSettings = true },
                            hasPermission = Settings.canDrawOverlays(this),
                            settingsManager = settingsManager
                        )
                    }
                }
            }
        }
    }

    private fun checkOverlayPermissionAndStart() {
        if (Settings.canDrawOverlays(this)) {
            startFloatingService()
        } else {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingCalculatorService::class.java)
        startService(intent)

        // Minimize the app to show the floating calculator
        moveTaskToBack(true)
    }
}

@Composable
fun PriceCalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF58A6FF),
            surface = Color(0xFF161B22),
            background = Color(0xFF0D1117),
            onSurface = Color(0xFFE6EDF3),
            onBackground = Color(0xFFE6EDF3)
        ),
        content = content
    )
}

@Composable
fun LauncherScreen(
    onLaunchFloat: () -> Unit,
    onOpenSettings: () -> Unit,
    hasPermission: Boolean,
    settingsManager: SettingsManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117),
                        Color(0xFF161B22),
                        Color(0xFF0D1117)
                    )
                )
            )
            .padding(32.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color(0xFF161B22),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📊",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "PRICE CALCULATOR",
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = Color(0xFFE6EDF3)
            )
        )

        Text(
            text = "Floating Overlay Mode",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp,
                color = Color(0xFF8B949E)
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Description
        Text(
            text = "Launch a floating calculator that stays on top of your trading apps. View your charts while calculating profit and loss targets.",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color(0xFF8B949E),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Launch button
        Button(
            onClick = onLaunchFloat,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF238636)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "LAUNCH FLOATING CALCULATOR",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Settings button
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF8B949E)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF30363D), Color(0xFF30363D))
                )
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "SETTINGS",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            )
        }

        if (!hasPermission) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "⚠️ Overlay permission required",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFFF85149)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info cards - now dynamic based on settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                emoji = "📈",
                title = "Profit",
                values = "+${settingsManager.profit1}%, +${settingsManager.profit2}%",
                color = Color(0xFF3FB950)
            )

            InfoCard(
                modifier = Modifier.weight(1f),
                emoji = "📉",
                title = "Stop Loss",
                values = "-${settingsManager.loss1}%, -${settingsManager.loss2}%",
                color = Color(0xFFF85149)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Max Trade Value card
        InfoCard(
            modifier = Modifier.fillMaxWidth(),
            emoji = "💰",
            title = "Max Trade",
            values = "$${String.format("%.2f", settingsManager.maxTradeValue)}",
            color = Color(0xFF58A6FF)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Abdullah Solutions",
            style = TextStyle(
                fontSize = 12.sp,
                color = Color(0xFF484F58),
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit
) {
    var profit1 by remember { mutableStateOf(settingsManager.profit1.toString()) }
    var profit2 by remember { mutableStateOf(settingsManager.profit2.toString()) }
    var loss1 by remember { mutableStateOf(settingsManager.loss1.toString()) }
    var loss2 by remember { mutableStateOf(settingsManager.loss2.toString()) }
    var maxTradeValue by remember { mutableStateOf(settingsManager.maxTradeValue.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117),
                        Color(0xFF161B22),
                        Color(0xFF0D1117)
                    )
                )
            )
            .padding(32.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                style = TextStyle(
                    fontSize = 24.sp,
                    color = Color(0xFF58A6FF)
                ),
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "SETTINGS",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFE6EDF3)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Customize your profit targets and stop loss percentages",
            style = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF8B949E),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profit Section
        Text(
            text = "PROFIT TARGETS",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF3FB950)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PercentageInput(
                modifier = Modifier.weight(1f),
                label = "Target 1",
                value = profit1,
                onValueChange = { profit1 = it },
                isProfit = true
            )

            PercentageInput(
                modifier = Modifier.weight(1f),
                label = "Target 2",
                value = profit2,
                onValueChange = { profit2 = it },
                isProfit = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Loss Section
        Text(
            text = "STOP LOSS",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFF85149)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PercentageInput(
                modifier = Modifier.weight(1f),
                label = "Stop 1",
                value = loss1,
                onValueChange = { loss1 = it },
                isProfit = false
            )

            PercentageInput(
                modifier = Modifier.weight(1f),
                label = "Stop 2",
                value = loss2,
                onValueChange = { loss2 = it },
                isProfit = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Max Trade Value Section
        Text(
            text = "MAX TRADE VALUE",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF58A6FF)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        CurrencyInput(
            modifier = Modifier.fillMaxWidth(),
            label = "Maximum amount per trade",
            value = maxTradeValue,
            onValueChange = { maxTradeValue = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save button
        Button(
            onClick = {
                profit1.toIntOrNull()?.let { settingsManager.profit1 = it }
                profit2.toIntOrNull()?.let { settingsManager.profit2 = it }
                loss1.toIntOrNull()?.let { settingsManager.loss1 = it }
                loss2.toIntOrNull()?.let { settingsManager.loss2 = it }
                maxTradeValue.toDoubleOrNull()?.let { settingsManager.maxTradeValue = it }
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF238636)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "SAVE SETTINGS",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reset to defaults button
        OutlinedButton(
            onClick = {
                settingsManager.resetToDefaults()
                profit1 = SettingsManager.DEFAULT_PROFIT_1.toString()
                profit2 = SettingsManager.DEFAULT_PROFIT_2.toString()
                loss1 = SettingsManager.DEFAULT_LOSS_1.toString()
                loss2 = SettingsManager.DEFAULT_LOSS_2.toString()
                maxTradeValue = SettingsManager.DEFAULT_MAX_TRADE_VALUE.toString()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF8B949E)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF30363D), Color(0xFF30363D))
                )
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "RESET TO DEFAULTS",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Note: Restart the floating calculator to apply changes",
            style = TextStyle(
                fontSize = 12.sp,
                color = Color(0xFF484F58),
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun PercentageInput(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isProfit: Boolean
) {
    val accentColor = if (isProfit) Color(0xFF3FB950) else Color(0xFFF85149)

    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color(0xFF8B949E)
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Only allow numbers
                if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6EDF3)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = {
                Text(
                    text = "%",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = accentColor
                    )
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color(0xFF30363D),
                cursorColor = accentColor
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun CurrencyInput(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color(0xFF8B949E)
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Allow numbers and one decimal point
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6EDF3)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            prefix = {
                Text(
                    text = "$",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF58A6FF)
                    )
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF58A6FF),
                unfocusedBorderColor = Color(0xFF30363D),
                cursorColor = Color(0xFF58A6FF)
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    values: String,
    color: Color
) {
    Column(
        modifier = modifier
            .background(
                Color(0xFF161B22),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 24.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        )

        Text(
            text = values,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color(0xFF8B949E)
            )
        )
    }
}
