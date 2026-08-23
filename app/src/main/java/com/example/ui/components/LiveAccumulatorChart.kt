package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketIndex
import com.example.data.model.TickPoint
import com.example.ui.theme.*

@Composable
fun LiveAccumulatorChart(
    symbol: String,
    ticks: List<TickPoint>,
    growthRate: Double,
    modifier: Modifier = Modifier
) {
    val index = MarketIndex.findBySymbol(symbol)
    val latestPrice = ticks.lastOrNull()?.price ?: 1000.0
    val firstPrice = ticks.firstOrNull()?.price ?: latestPrice
    val priceDelta = latestPrice - firstPrice
    val deltaPercent = if (firstPrice > 0) (priceDelta / firstPrice) * 100 else 0.0
    val isPositive = priceDelta >= 0

    // Barrier tolerance based on growth rate
    val barrierTolerance = when {
        growthRate <= 0.01 -> 0.032
        growthRate <= 0.02 -> 0.022
        growthRate <= 0.03 -> 0.015
        growthRate <= 0.04 -> 0.010
        else -> 0.007
    }
    val upperBarrier = latestPrice * (1.0 + barrierTolerance)
    val lowerBarrier = latestPrice * (1.0 - barrierTolerance)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .testTag("live_accumulator_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Chart Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = index.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GlassSurfaceHighlight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBright)
                        ) {
                            Text(
                                text = "${(growthRate * 100).toInt()}% ACCU",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GlassRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                    Text(
                        text = index.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.2f", latestPrice),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isPositive) GlassEmerald else GlassRed
                        )
                    )
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.2f", priceDelta)} (${String.format("%.3f", deltaPercent)}%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isPositive) GlassEmerald else GlassRed
                        )
                    )
                }
            }

            // Canvas Chart in Frosted obsidian container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(14.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
                    val w = size.width
                    val h = size.height

                    if (ticks.size < 2) {
                        drawLine(
                            color = GlassBorder,
                            start = Offset(0f, h / 2),
                            end = Offset(w, h / 2),
                            strokeWidth = 2f
                        )
                        return@Canvas
                    }

                    val prices = ticks.map { it.price }
                    val minPrice = (prices.minOrNull() ?: 1000.0) * (1.0 - barrierTolerance * 1.2)
                    val maxPrice = (prices.maxOrNull() ?: 1000.0) * (1.0 + barrierTolerance * 1.2)
                    val range = if (maxPrice - minPrice > 0.0001) maxPrice - minPrice else 1.0

                    fun priceToY(price: Double): Float {
                        val norm = (price - minPrice) / range
                        return (h - (norm * h)).toFloat().coerceIn(4f, h - 4f)
                    }

                    // Draw Horizontal Grid Lines & Barrier Bounds
                    val upperY = priceToY(upperBarrier)
                    val lowerY = priceToY(lowerBarrier)

                    // Barrier Corridor fill
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                GlassRed.copy(alpha = 0.08f),
                                GlassEmerald.copy(alpha = 0.03f),
                                GlassRed.copy(alpha = 0.08f)
                            )
                        ),
                        topLeft = Offset(0f, upperY),
                        size = androidx.compose.ui.geometry.Size(w, (lowerY - upperY).coerceAtLeast(10f))
                    )

                    // Knock-out upper barrier line (dashed)
                    drawLine(
                        color = GlassRed.copy(alpha = 0.7f),
                        start = Offset(0f, upperY),
                        end = Offset(w, upperY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )

                    // Knock-out lower barrier line (dashed)
                    drawLine(
                        color = GlassRed.copy(alpha = 0.7f),
                        start = Offset(0f, lowerY),
                        end = Offset(w, lowerY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )

                    // Build path for ticks
                    val path = Path()
                    val fillPath = Path()
                    val stepX = w / (ticks.size - 1).coerceAtLeast(1)

                    ticks.forEachIndexed { i, tick ->
                        val x = i * stepX
                        val y = priceToY(tick.price)
                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.lineTo(0f, h)
                    fillPath.close()

                    // Draw area gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                GlassCyan.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw line
                    drawPath(
                        path = path,
                        color = GlassCyan,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw glowing live dot on latest tick
                    val lastX = (ticks.size - 1) * stepX
                    val lastY = priceToY(latestPrice)

                    drawCircle(
                        color = GlassCyan.copy(alpha = 0.3f),
                        radius = 9f,
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(lastX, lastY)
                    )
                }

                // Overlay Barrier Labels
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "▲ UPPER KNOCKOUT: ${String.format("%.2f", upperBarrier)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GlassRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "▼ LOWER KNOCKOUT: ${String.format("%.2f", lowerBarrier)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GlassRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            // Quick Info Strip in Frosted Capsule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = GlassEmerald, modifier = Modifier.size(14.dp))
                    Text("Tick Interval: 1.0s", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = GlassAmber, modifier = Modifier.size(14.dp))
                    Text("Corridor: ±${String.format("%.2f", barrierTolerance * 100)}%", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                }
            }
        }
    }
}

