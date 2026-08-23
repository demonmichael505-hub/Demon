package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TradeEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryTab(
    trades: List<TradeEntity>,
    totalProfit: Double?,
    totalCount: Int,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All Trades") }
    val filters = listOf("All Trades", "Won Only", "Knocked Out", "🤖 Auto-Bot", "🎯 Manual")

    val wonTrades = trades.count { it.outcome == "WON" }
    val winRate = if (trades.isNotEmpty()) (wonTrades.toDouble() / trades.size) * 100 else 0.0
    val netProfit = totalProfit ?: 0.0

    val filteredList = trades.filter { trade ->
        when (selectedFilter) {
            "Won Only" -> trade.outcome == "WON"
            "Knocked Out" -> trade.outcome != "WON"
            "🤖 Auto-Bot" -> trade.isBot
            "🎯 Manual" -> !trade.isBot
            else -> true
        }
    }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GlassDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Analytics KPI Grid in Frosted Glass Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(20.dp))
                    .testTag("history_kpi_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCUMULATOR AUDIT & PERFORMANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        if (trades.isNotEmpty()) {
                            IconButton(
                                onClick = onClearHistory,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear History", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // KPI Capsules Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("NET P&L", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                            Text(
                                text = "${if (netProfit >= 0) "+" else ""}$${String.format("%.2f", netProfit)}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (netProfit >= 0) GlassEmerald else GlassRed,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WIN RATE", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                            Text(
                                text = "${String.format("%.1f", winRate)}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (winRate >= 50) GlassEmerald else GlassAmber,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL TRADES", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                            Text(
                                text = "$totalCount",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        onClick = { selectedFilter = filter },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) GlassCyan.copy(alpha = 0.25f) else GlassSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GlassCyan else GlassBorder
                        )
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) GlassCyan else TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Trades List
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Text("No trades recorded yet", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                        Text("Executed accumulator trades will appear in this ledger.", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { trade ->
                val isWon = trade.outcome == "WON"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (trade.isBot) GlassPurple.copy(alpha = 0.25f) else GlassCyan.copy(alpha = 0.25f))
                                        .border(1.dp, if (trade.isBot) GlassPurple.copy(alpha = 0.5f) else GlassCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (trade.isBot) "BOT" else "MANUAL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (trade.isBot) GlassPurple else GlassCyan
                                        )
                                    )
                                }
                                Text(
                                    text = trade.symbolName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                            }

                            Text(
                                text = "${trade.ticksHeld}/${trade.targetTicks} Ticks @ +${(trade.growthRate * 100).toInt()}% • ${dateFormat.format(Date(trade.timestamp))}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Right Column
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${if (trade.profit >= 0) "+" else ""}$${String.format("%.2f", trade.profit)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (isWon) GlassEmerald else GlassRed,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = if (isWon) "✓ WON (${String.format("%.2f", trade.payout)})" else "💀 KNOCKED OUT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (isWon) GlassEmerald else GlassRed,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

