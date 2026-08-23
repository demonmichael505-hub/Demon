package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketIndex
import com.example.data.model.TickPoint
import com.example.ui.components.LiveAccumulatorChart
import com.example.ui.theme.*
import kotlin.math.pow

@Composable
fun LiveAnalysisTab(
    selectedSymbol: String,
    recentTicks: List<TickPoint>,
    onSelectSymbol: (String) -> Unit,
    onExecuteTrade: (symbol: String, stake: Double, growthRate: Double, targetTicks: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var customGrowthRate by remember { mutableStateOf(0.03) }
    var customStake by remember { mutableStateOf("10.0") }
    var customTargetTicks by remember { mutableStateOf(8) }

    val growthRates = listOf(0.01, 0.02, 0.03, 0.04, 0.05)

    // Simulation calculation
    val stakeVal = customStake.toDoubleOrNull() ?: 10.0
    val simMultiplier = (1.0 + customGrowthRate).pow(customTargetTicks.toDouble())
    val simPayout = stakeVal * simMultiplier
    val simProfit = simPayout - stakeVal

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GlassDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Market Indices Carousel in Frosted Glass Pills
        item {
            Text(
                text = "SELECT DERIV SYNTHETIC INDEX",
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
                items(MarketIndex.ALL_INDICES) { index ->
                    val isSelected = index.symbol == selectedSymbol
                    Surface(
                        onClick = { onSelectSymbol(index.symbol) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) GlassRed else GlassSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.3f) else GlassBorder
                        ),
                        modifier = Modifier.testTag("index_chip_${index.symbol}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = index.name.replace(" Index", "").replace("Volatility ", "V"),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            )
                            Text(
                                text = index.volatilityLevel + " Vol",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }

        // Live Real-Time Accumulator Canvas Chart
        item {
            LiveAccumulatorChart(
                symbol = selectedSymbol,
                ticks = recentTicks,
                growthRate = customGrowthRate
            )
        }

        // Accumulator Growth Rate Selector (1% - 5%)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCUMULATOR GROWTH RATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "+${(customGrowthRate * 100).toInt()}% / Tick",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GlassAmber,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        growthRates.forEach { rate ->
                            val isSelected = customGrowthRate == rate
                            Surface(
                                onClick = { customGrowthRate = rate },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) GlassAmber else GlassSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color.White.copy(alpha = 0.4f) else GlassBorderSubtle
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${(rate * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) Color.Black else TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = when (customGrowthRate) {
                            0.01 -> "🛡️ 1% Ultra Safe: Widest barrier corridor (±3.2%). Lowest knockout probability."
                            0.02 -> "⚖️ 2% Stable: Balanced safe run for 8-15 ticks accumulation."
                            0.03 -> "⚡ 3% Demon Standard: Recommended sweet spot for momentum scalping."
                            0.04 -> "🔥 4% High Yield: Fast exponential compound trajectory."
                            else -> "🚀 5% Hyper Growth: Maximum compounding speed. Requires fast target exit (3-5 ticks)."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }
            }
        }

        // Interactive Compound Profit Simulator
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCUMULATOR LAB SIMULATOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GlassCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = GlassCyan, modifier = Modifier.size(18.dp))
                    }

                    // Sliders and Inputs
                    OutlinedTextField(
                        value = customStake,
                        onValueChange = { customStake = it },
                        label = { Text("Stake Amount ($ USD)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_stake_input")
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Ticks to Auto-Cashout:", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            Text("$customTargetTicks Ticks", style = MaterialTheme.typography.bodySmall.copy(color = GlassEmerald, fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = customTargetTicks.toFloat(),
                            onValueChange = { customTargetTicks = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 28,
                            colors = SliderDefaults.colors(
                                thumbColor = GlassCyan,
                                activeTrackColor = GlassCyan,
                                inactiveTrackColor = GlassSurface
                            )
                        )
                    }

                    // Simulator Result Dashboard in Frosted Capsule
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GlassSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MULTIPLIER", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Text(String.format("%.3fx", simMultiplier), style = MaterialTheme.typography.bodyLarge.copy(color = Color.White, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EST PAYOUT", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Text("$$${String.format("%.2f", simPayout)}", style = MaterialTheme.typography.bodyLarge.copy(color = GlassEmerald, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("EST NET PROFIT", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Text("+$${String.format("%.2f", simProfit)}", style = MaterialTheme.typography.bodyLarge.copy(color = GlassEmerald, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace))
                            }
                        }
                    }

                    // Execute Now Button
                    Button(
                        onClick = {
                            val stake = customStake.toDoubleOrNull() ?: 10.0
                            onExecuteTrade(selectedSymbol, stake, customGrowthRate, customTargetTicks)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("launch_custom_accu_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START ACCUMULATOR ($customTargetTicks TICKS @ ${(customGrowthRate * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}

