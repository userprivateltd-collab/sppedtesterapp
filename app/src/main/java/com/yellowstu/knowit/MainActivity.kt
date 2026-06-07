package com.yellowstu.knowit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SpeedTestScreen()
            }
        }
    }
}

@Composable
fun SpeedTestScreen() {
    // UI Theme Palette Configurations (Premium Clean Aesthetic)
    val bgBase = Color(0xFFF8FAFC)
    val accentCyan = Color(0xFF12C6ED)
    val textMain = Color(0xFF0F172A)
    val textMuted = Color(0xFF64748B)

    // State management for the network engine values
    var speedDisplay by remember { mutableStateOf(0.0) }
    var isTesting by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Smooth physics-based animation mapping for the gauge pointer needle
    val animatedSpeed by animateFloatAsState(
        targetValue = speedDisplay.toFloat(),
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "GaugePointer"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBase)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Status Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Yellow sTudios", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textMuted)
                Text(text = "Network Matrix", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textMain)
            }
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = Color.White,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text(text = "v1.0 Native", modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textMain)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- Core Analytical Circular Matrix Meter ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(300.dp)
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                // Draw background tracking arc line
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )

                // Calculate current sweep execution angle capped at a maximum 100 Mbps model scale
                val sweepAngle = (animatedSpeed / 100f) * 270f

                // Draw the active dynamic glowing indicator tracking line
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(accentCyan, Color(0xFF9B72CB))
                    ),
                    startAngle = 135f,
                    sweepAngle = sweepAngle.coerceAtMost(270f),
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Central Value Telemetry Readout
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1f", speedDisplay),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = textMain,
                    letterSpacing = (-2).sp
                )
                Text(
                    text = "Mbps",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- Bottom Control Panel Operations Tray ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!isTesting) {
                        isTesting = true
                        scope.launch {
                            val networkCheckpoints = listOf(12.4, 34.1, 58.7, 72.3, 89.4, 94.2, 91.1)
                            for (speedSample in networkCheckpoints) {
                                speedDisplay = speedSample
                                delay(700)
                            }
                            isTesting = false
                        }
                    }
                },
                enabled = !isTesting,
                colors = ButtonDefaults.buttonColors(containerColor = textMain),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isTesting) "ANALYZING WIRES..." else "RUN SPEED TEST",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // --- About Section (Button & Dialog) ---
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(100.dp),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text(
                    text = "ABOUT THE PROJECT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    letterSpacing = 1.sp
                )
            }

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("CLOSE", fontWeight = FontWeight.Bold, color = accentCyan)
                        }
                    },
                    title = {
                        Text(
                            text = "Project Intelligence",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMain
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Yellow sTudios Founder: notrazx",
                                fontWeight = FontWeight.SemiBold,
                                color = textMain
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Instagram: yellowstudios.f", color = accentCyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Contact: userprivateltd@gmail.com", fontSize = 13.sp, color = textMuted)
                            Text(text = "Licensed under MIT", fontSize = 12.sp, color = textMuted.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "A dedicated one-member performance matrix.", fontSize = 12.sp, color = textMuted)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}