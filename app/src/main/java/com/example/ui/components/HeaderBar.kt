package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sensors
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
import com.example.data.model.DerivAccount
import com.example.ui.theme.*

@Composable
fun HeaderBar(
    account: DerivAccount,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        color = GlassDarkBg,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header Brand matching Design HTML
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FrostedLogoGradient)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Demon Signal Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "DEMON SIGNAL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (account.isConnected) GlassEmerald.copy(alpha = alphaAnim) else GlassRed)
                            )
                            Text(
                                text = "MARKET LIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }
                    }
                }

                // Balance & Mode Chip (Frosted Glass Pill)
                Surface(
                    onClick = onAccountClick,
                    shape = RoundedCornerShape(24.dp),
                    color = GlassSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderBright),
                    modifier = Modifier.testTag("account_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (account.isDemo) GlassCyan.copy(alpha = 0.2f) else GlassEmerald.copy(alpha = 0.2f))
                                .border(1.dp, if (account.isDemo) GlassCyan.copy(alpha = 0.4f) else GlassEmerald.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (account.isDemo) "DEMO" else "REAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (account.isDemo) GlassCyan else GlassEmerald,
                                    fontSize = 9.sp
                                )
                            )
                        }

                        Text(
                            text = "$${String.format("%,.2f", account.balance)}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            // Connection telemetry strip in frosted glass capsule
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GlassSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "WS Status",
                                tint = if (account.isConnected) GlassEmerald else GlassAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = account.pingStatus,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Text(
                            text = "•  ${account.latencyMs}ms",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Text(
                        text = "${account.totalTicksProcessed} ticks synced",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

