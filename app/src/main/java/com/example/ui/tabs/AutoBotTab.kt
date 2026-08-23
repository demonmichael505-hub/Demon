package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.data.model.BotSettings
import com.example.data.model.MarketIndex
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoBotTab(
    settings: BotSettings,
    onToggleBot: (Boolean) -> Unit,
    onUpdateSettings: (BotSettings) -> Unit,
    onApplyPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        "Demon Scalp (5-Tick)",
        "Safe Compound 1% Ultra",
        "Aggressive 5% Burst",
        "Balanced 2% Radar"
    )

    var stakeInput by remember(settings.baseStake) { mutableStateOf(settings.baseStake.toString()) }
    var stopLossInput by remember(settings.dailyStopLoss) { mutableStateOf(settings.dailyStopLoss.toString()) }
    var takeProfitInput by remember(settings.dailyTakeProfit) { mutableStateOf(settings.dailyTakeProfit.toString()) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GlassDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Master Bot Status Banner in Frosted Glass styling
        item {
            val isActive = settings.isAutoTrading
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (isActive) GlassRed.copy(alpha = pulseAlpha) else GlassBorderBright,
                        RoundedCornerShape(20.dp)
                    )
                    .testTag("bot_status_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassSurfaceElevated
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isActive) FrostedHeroGradient else Brush.verticalGradient(listOf(Color(0x10FFFFFF), Color.Transparent)))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) FrostedButtonGradient
                                            else Brush.radialGradient(listOf(GlassSurfaceHighlight, GlassSurface))
                                        )
                                        .border(1.dp, if (isActive) Color.White.copy(alpha = 0.4f) else GlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else TextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (isActive) "DEMON AUTO-BOT ACTIVE" else "DEMON BOT STANDBY",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = if (isActive) GlassRed else TextSecondary
                                        )
                                    )
                                    Text(
                                        text = if (isActive) "Scanning & executing high DPI signals" else "Turn on switch to automate executions",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = isActive,
                                onCheckedChange = { onToggleBot(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GlassRed,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = GlassSurface
                                ),
                                modifier = Modifier.testTag("bot_toggle_switch")
                            )
                        }

                        // Bot Active Specs Grid in Frosted Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassSurface)
                                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("STRATEGY", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Text(settings.activeStrategy, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Black))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MIN DPI", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Text("${settings.minConfidence}%", style = MaterialTheme.typography.bodySmall.copy(color = GlassCyan, fontWeight = FontWeight.Black))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TARGET", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Text("${settings.targetTicks} Ticks", style = MaterialTheme.typography.bodySmall.copy(color = GlassEmerald, fontWeight = FontWeight.Black))
                            }
                        }
                    }
                }
            }
        }

        // Strategy Presets
        item {
            Text(
                text = "STRATEGY PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isCurrent = settings.activeStrategy == preset
                    Surface(
                        onClick = { onApplyPreset(preset) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) GlassPurple.copy(alpha = 0.3f) else GlassSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) GlassPurple else GlassBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (isCurrent) GlassPurple else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Medium,
                                    color = if (isCurrent) Color.White else TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Active Markets to Auto-Trade
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "MONITORED SYNTHETIC INDICES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Select which indices the Demon Auto-Bot scans for accumulator signals:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )

                    val popularIndices = listOf("1HZ100V", "1HZ75V", "1HZ50V", "1HZ25V", "1HZ10V", "R_100", "R_75", "R_50")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularIndices.forEach { sym ->
                            val isSelected = settings.activeSymbols.contains(sym)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val updated = if (isSelected) {
                                        if (settings.activeSymbols.size > 1) settings.activeSymbols - sym else settings.activeSymbols
                                    } else {
                                        settings.activeSymbols + sym
                                    }
                                    onUpdateSettings(settings.copy(activeSymbols = updated))
                                },
                                shape = RoundedCornerShape(10.dp),
                                label = { Text(sym, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GlassRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = GlassSurface,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) Color.White.copy(alpha = 0.3f) else GlassBorderSubtle,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }
            }
        }

        // Risk Management & Execution Rules
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RISK MANAGEMENT & SAFEGUARDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GlassRed,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )

                    // Base Stake & Target Ticks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stakeInput,
                            onValueChange = {
                                stakeInput = it
                                it.toDoubleOrNull()?.let { st -> onUpdateSettings(settings.copy(baseStake = st)) }
                            },
                            label = { Text("Base Stake ($)", color = TextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlassRed,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = settings.targetTicks.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { t -> onUpdateSettings(settings.copy(targetTicks = t.coerceIn(2, 30))) }
                            },
                            label = { Text("Target Ticks", color = TextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlassRed,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Daily Stop Loss & Take Profit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stopLossInput,
                            onValueChange = {
                                stopLossInput = it
                                it.toDoubleOrNull()?.let { sl -> onUpdateSettings(settings.copy(dailyStopLoss = sl)) }
                            },
                            label = { Text("Daily Stop Loss ($)", color = TextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlassRed,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = takeProfitInput,
                            onValueChange = {
                                takeProfitInput = it
                                it.toDoubleOrNull()?.let { tp -> onUpdateSettings(settings.copy(dailyTakeProfit = tp)) }
                            },
                            label = { Text("Daily Take Profit ($)", color = TextSecondary, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GlassEmerald,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Min Confidence Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Minimum Signal Confidence (DPI):", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("${settings.minConfidence}%", style = MaterialTheme.typography.bodySmall.copy(color = GlassCyan, fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = settings.minConfidence.toFloat(),
                            onValueChange = { onUpdateSettings(settings.copy(minConfidence = it.toInt())) },
                            valueRange = 75f..98f,
                            steps = 22,
                            colors = SliderDefaults.colors(
                                thumbColor = GlassCyan,
                                activeTrackColor = GlassCyan,
                                inactiveTrackColor = GlassSurface
                            )
                        )
                    }

                    // Martingale Toggle in Frosted Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Martingale Stake Multiplier", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            Text("Multiply stake (${settings.martingaleMultiplier}x) after knockout loss", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp))
                        }
                        Switch(
                            checked = settings.isMartingaleEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(isMartingaleEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GlassAmber
                            )
                        )
                    }
                }
            }
        }
    }
}

