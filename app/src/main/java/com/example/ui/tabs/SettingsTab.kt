package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.DerivAccount
import com.example.ui.theme.*

@Composable
fun SettingsTab(
    account: DerivAccount,
    onSaveToken: (token: String, isDemo: Boolean) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tokenInput by remember(account.apiToken) { mutableStateOf(account.apiToken) }
    var isDemoSelected by remember(account.isDemo) { mutableStateOf(account.isDemo) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GlassDarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Deriv Account Connection Card in Frosted Glass styling
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorderBright, RoundedCornerShape(20.dp))
                    .testTag("settings_account_card"),
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
                            text = "DERIV API CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GlassRed,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (account.isConnected) GlassEmerald.copy(alpha = 0.25f) else GlassRed.copy(alpha = 0.25f))
                                .border(1.dp, if (account.isConnected) GlassEmerald.copy(alpha = 0.5f) else GlassRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (account.isConnected) "CONNECTED" else "DISCONNECTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (account.isConnected) GlassEmerald else GlassRed
                                )
                            )
                        }
                    }

                    // Demo / Real Toggle Switch in Frosted Capsule
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = { isDemoSelected = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDemoSelected) GlassCyan else Color.Transparent
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "DEMO ACCOUNT (VRTC)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (isDemoSelected) Color.Black else TextSecondary
                                    )
                                )
                            }
                        }

                        Surface(
                            onClick = { isDemoSelected = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isDemoSelected) GlassEmerald else Color.Transparent
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "REAL ACCOUNT (CR)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (!isDemoSelected) Color.Black else TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    // Token input field
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Deriv API Token", color = TextSecondary) },
                        placeholder = { Text("Enter token or leave blank for instant demo sandbox", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassRed,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("deriv_api_token_input")
                    )

                    Button(
                        onClick = { onSaveToken(tokenInput, isDemoSelected) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_token_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE & AUTHENTICATE TOKEN", fontWeight = FontWeight.Black)
                    }

                    Text(
                        text = "💡 You can obtain your API token from Deriv: App Settings > API Token > Check 'Read' and 'Trade'.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }
            }
        }

        // Live Diagnostic Metrics in Frosted Container
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ENGINE TELEMETRY & WEBSOCKET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Endpoint:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                        Text("wss://ws.derivws.com/v3 (App ID: 1089)", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Connection Latency:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                        Text("${account.latencyMs} ms", style = MaterialTheme.typography.bodySmall.copy(color = GlassEmerald, fontWeight = FontWeight.Black))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Account ID:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                        Text(account.accountId, style = MaterialTheme.typography.bodySmall.copy(color = GlassCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Market Ticks Processed:", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                        Text("${account.totalTicksProcessed}", style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Accumulator Mechanics Guide
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HOW ACCUMULATORS WORK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GlassAmber,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "1. Payout Formula: Payout = Stake × (1 + Growth Rate)^Ticks\n" +
                                "2. Knockout Barrier: If tick price breaches the upper or lower boundary corridor, the contract terminates immediately.\n" +
                                "3. Demon Power Index (DPI): Evaluates rolling variance and stability to trigger trades when knock-out probability is statistically lowest.\n" +
                                "4. Auto-Cashout: Closes the contract automatically as soon as your target ticks or profit threshold is reached.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            lineHeight = 18.sp,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Danger Zone: Clear History
        item {
            OutlinedButton(
                onClick = onClearAllData,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GlassRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassRed.copy(alpha = 0.6f))
            ) {
                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("CLEAR ALL LOCAL HISTORY & CACHE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

