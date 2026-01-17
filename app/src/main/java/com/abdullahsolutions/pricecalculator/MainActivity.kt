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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    
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
        enableEdgeToEdge()
        setContent {
            PriceCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D1117)
                ) {
                    LauncherScreen(
                        onLaunchFloat = { checkOverlayPermissionAndStart() },
                        hasPermission = Settings.canDrawOverlays(this)
                    )
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
    hasPermission: Boolean
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
        
        // Info cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                modifier = Modifier.weight(1f),
                emoji = "📈",
                title = "Profit",
                values = "+20%, +10%",
                color = Color(0xFF3FB950)
            )
            
            InfoCard(
                modifier = Modifier.weight(1f),
                emoji = "📉",
                title = "Stop Loss",
                values = "-20%, -30%",
                color = Color(0xFFF85149)
            )
        }
        
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
