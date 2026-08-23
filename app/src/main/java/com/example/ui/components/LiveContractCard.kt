package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.LiveAccumulatorContract
import com.example.data.model.TradeStatus
import com.example.ui.theme.*

@Composable
fun LiveContractCard(
    contract: LiveAccumulatorContract,
    onManualCashOut: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = contract.status == TradeStatus.RUNNING
    val isWon = contract.status == TradeStatus.CASHED_OUT
    val isKnockedOut = contract.status == TradeStatus.KNOCKED_OUT

    val borderColor = when {
        isWon -> GlassEmerald
        isKnockedOut -> GlassRed
        else -> GlassCyan
    }

    val glowBrush = when {
        isWon -> Brush.verticalGradient(listOf(Color(0x3322C55E), Color(0x0522C55E), Color.Transparent))
        isKnockedOut -> Brush.verticalGradient(listOf(Color(0x33EF4444), Color(0x05EF4444), Color.Transparent))
        else -> Brush.verticalGradient(listOf(Color(0x3338BDF8), Color(0x0538BDF8), Color.Transparent))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .testTag("live_contract_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glowBrush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Header Row
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (contract.isBot) GlassPurple.copy(alpha = 0.2f) else GlassCyan.copy(alpha = 0.2f))
                                .border(1.dp, if (contract.isBot) GlassPurple.copy(alpha = 0.5f) else GlassCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (contract.isBot) "🤖 AUTO-BOT" else "🎯 MANUAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = if (contract.isBot) GlassPurple else GlassCyan
                                )
                            )
                        }

                        Text(
                            text = contract.symbolName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }

                    // Status pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isWon -> GlassEmerald.copy(alpha = 0.2f)
                            isKnockedOut -> GlassRed.copy(alpha = 0.2f)
                            else -> GlassCyan.copy(alpha = 0.2f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                isWon -> GlassEmerald.copy(alpha = 0.4f)
                                isKnockedOut -> GlassRed.copy(alpha = 0.4f)
                                else -> GlassCyan.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp,
                                    color = GlassCyan
                                )
                            }
                            Text(
                                text = when {
                                    isWon -> "✓ CASHED OUT"
                                    isKnockedOut -> "💀 KNOCKED OUT"
                                    else -> "LIVE ACCUMULATING"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = when {
                                        isWon -> GlassEmerald
                                        isKnockedOut -> GlassRed
                                        else -> GlassCyan
                                    }
                                )
                            )
                        }
                    }
                }

                // Primary Metrics Grid (Frosted Glass Container)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("STAKE", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                        Text(
                            text = "$${String.format("%.2f", contract.stake)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GROWTH RATE", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                        Text(
                            text = "+${(contract.growthRate * 100).toInt()}% / tick",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = GlassAmber,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("CURRENT PAYOUT", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                        Text(
                            text = "$${String.format("%.2f", contract.currentPayout)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (contract.currentProfit >= 0) GlassEmerald else GlassRed,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                // Tick Accumulation Visual Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Accumulated: ${contract.currentTicks} / ${contract.targetTicks} Ticks",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${String.format("%.3fx", contract.multiplier)} Multiplier",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GlassEmerald,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    val progress = (contract.currentTicks.toFloat() / contract.targetTicks.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isKnockedOut) GlassRed else GlassEmerald,
                        trackColor = GlassSurfaceElevated,
                    )
                }

                // Barrier Corridor Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassSurface)
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Barrier: [${String.format("%.2f", contract.lowerBarrier)} - ${String.format("%.2f", contract.upperBarrier)}]",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "Spot: ${String.format("%.2f", contract.currentSpot)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Action Controls
                if (isRunning) {
                    Button(
                        onClick = onManualCashOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("manual_cashout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassEmerald,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TAKE PROFIT / CASH OUT ($${String.format("%.2f", contract.currentProfit)})",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isWon) "🎉 Final Profit: +$${String.format("%.2f", contract.currentProfit)}" else "💀 Stake Lost: -$${String.format("%.2f", contract.stake)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isWon) GlassEmerald else GlassRed
                            )
                        )

                        TextButton(onClick = onDismiss) {
                            Text("Dismiss", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

