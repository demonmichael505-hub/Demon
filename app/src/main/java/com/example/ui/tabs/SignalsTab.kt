package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccumulatorSignal
import com.example.data.model.SignalStatus
import com.example.ui.theme.*
import kotlin.math.pow

@Composable
fun SignalsTab(
    signals: List<AccumulatorSignal>,
    onExecuteSignal: (symbol: String, stake: Double, growthRate: Double, targetTicks: Int, signalId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All Signals") }
    val filters = listOf("All Signals", "🔥 Hot (90%+ DPI)", "1% Ultra Safe", "3% Squeeze", "5% Burst")

    var signalToExecute by remember { mutableStateOf<AccumulatorSignal?>(null) }
    var tradeStake by remember { mutableStateOf("10.0") }
    var targetTicksOverride by remember { mutableStateOf(8) }

    val filteredSignals = signals.filter { signal ->
        when (selectedFilter) {
            "🔥 Hot (90%+ DPI)" -> signal.confidenceScore >= 90
            "1% Ultra Safe" -> signal.growthRate <= 0.01
            "3% Squeeze" -> signal.growthRate in 0.02..0.035
            "5% Burst" -> signal.growthRate >= 0.04
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GlassDarkBg)
    ) {
        // Filter Chips Row in Frosted Glass Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) GlassRed else GlassSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.3f) else GlassBorder
                    )
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    )
                }
            }
        }

        // AI Market Rationale Frosted Glass Hero Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, GlassBorderBright, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FrostedHeroGradient)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FrostedButtonGradient)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.AutoGraph, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DEMON RADAR AI ANALYSIS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GlassOrange,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = "Real-time barrier compression scanner active across 13 Deriv Synthetic Indices. Signals update continuously as variance cycles shift.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Signals List
        if (filteredSignals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Scanning Synthetic Indices...",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "High safety accumulator signals will appear here automatically when volatility corridors compress.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 4.dp)
            ) {
                items(filteredSignals, key = { it.id }) { signal ->
                    SignalCard(
                        signal = signal,
                        onExecuteClick = {
                            signalToExecute = signal
                            targetTicksOverride = signal.targetTicks
                        }
                    )
                }
            }
        }
    }

    // Execution Confirmation Dialog in Frosted Glass styling
    if (signalToExecute != null) {
        val signal = signalToExecute!!
        val stakeDouble = tradeStake.toDoubleOrNull() ?: 10.0
        val multiplier = (1.0 + signal.growthRate).pow(targetTicksOverride.toDouble())
        val estPayout = stakeDouble * multiplier
        val estProfit = estPayout - stakeDouble

        AlertDialog(
            onDismissRequest = { signalToExecute = null },
            containerColor = Color(0xFF16161A),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, GlassBorderBright, RoundedCornerShape(20.dp)),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = GlassRed)
                    Text(
                        text = "Execute Accumulator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${signal.symbolName} (${signal.symbol})",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GlassCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Stake input
                    OutlinedTextField(
                        value = tradeStake,
                        onValueChange = { tradeStake = it },
                        label = { Text("Stake Amount ($ USD)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassRed,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("stake_input_field")
                    )

                    // Target Ticks Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Ticks (Auto-Cashout):", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("$targetTicksOverride Ticks", style = MaterialTheme.typography.bodySmall.copy(color = GlassEmerald, fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = targetTicksOverride.toFloat(),
                            onValueChange = { targetTicksOverride = it.toInt() },
                            valueRange = 2f..25f,
                            steps = 22,
                            colors = SliderDefaults.colors(
                                thumbColor = GlassEmerald,
                                activeTrackColor = GlassEmerald,
                                inactiveTrackColor = GlassSurfaceElevated
                            )
                        )
                    }

                    // Payout preview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GlassSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Growth Rate:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                                Text("+${(signal.growthRate * 100).toInt()}% / tick", style = MaterialTheme.typography.bodySmall.copy(color = GlassAmber, fontWeight = FontWeight.Bold))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expected Multiplier:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                                Text(String.format("%.3fx", multiplier), style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontFamily = FontFamily.Monospace))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Target Payout / Profit:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                                Text("$$${String.format("%.2f", estPayout)} (+$${String.format("%.2f", estProfit)})", style = MaterialTheme.typography.bodySmall.copy(color = GlassEmerald, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stake = tradeStake.toDoubleOrNull() ?: 10.0
                        onExecuteSignal(
                            signal.symbol,
                            stake,
                            signal.growthRate,
                            targetTicksOverride,
                            signal.id
                        )
                        signalToExecute = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_execute_button")
                ) {
                    Text("START ACCUMULATOR", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { signalToExecute = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SignalCard(
    signal: AccumulatorSignal,
    onExecuteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHot = signal.confidenceScore >= 90

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isHot) GlassRed.copy(alpha = 0.5f) else GlassBorder,
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (signal.status == SignalStatus.ACTIVE) GlassEmerald else TextMuted)
                    )
                    Text(
                        text = signal.symbolName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }

                // Frosted Glass Confidence Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isHot) GlassRed.copy(alpha = 0.2f) else GlassCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isHot) GlassRed.copy(alpha = 0.5f) else GlassCyan.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isHot) GlassRed else GlassCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${signal.confidenceScore}% DPI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = if (isHot) GlassRed else GlassCyan
                            )
                        )
                    }
                }
            }

            // Specs Row in Frosted Glass Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GROWTH", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    Text("+${(signal.growthRate * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(color = GlassAmber, fontWeight = FontWeight.Black))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TARGET TICKS", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    Text("${signal.targetTicks} Ticks", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Black))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BARRIER SAFETY", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    Text("${signal.barrierSafetyPercent}%", style = MaterialTheme.typography.bodyMedium.copy(color = GlassEmerald, fontWeight = FontWeight.Black))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EST PAYOUT", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    Text("${signal.estimatedPayoutMultiplier}x", style = MaterialTheme.typography.bodyMedium.copy(color = GlassCyan, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace))
                }
            }

            // Indicator Rationale
            Text(
                text = "⚡ ${signal.indicatorRationale}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )

            // Execute Button matching Frosted Button Gradient
            Button(
                onClick = onExecuteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("execute_signal_button_${signal.symbol}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHot) GlassRed else Color(0x26FFFFFF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (!isHot) androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBright) else null
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "EXECUTE ACCU (${signal.targetTicks} TICKS @ ${(signal.growthRate * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

