package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.HeaderBar
import com.example.ui.components.LiveContractCard
import com.example.ui.tabs.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector) {
    SIGNALS("Signals", Icons.Default.Radar),
    CHART("Analysis", Icons.Default.ShowChart),
    BOT("Auto-Bot", Icons.Default.SmartToy),
    HISTORY("History", Icons.Default.ReceiptLong),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DemonSignalApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DemonSignalApp(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.SIGNALS) }

    val account by viewModel.accountState.collectAsStateWithLifecycle()
    val selectedSymbol by viewModel.selectedSymbol.collectAsStateWithLifecycle()
    val recentTicks by viewModel.recentTicks.collectAsStateWithLifecycle()
    val liveSignals by viewModel.liveSignals.collectAsStateWithLifecycle()
    val activeContract by viewModel.activeContract.collectAsStateWithLifecycle()
    val botSettings by viewModel.botSettings.collectAsStateWithLifecycle()
    val tradeHistory by viewModel.tradeHistory.collectAsStateWithLifecycle()
    val totalProfit by viewModel.totalProfit.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalTradesCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GlassDarkBg,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    containerColor = Color(0xFF1E1E24),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(1.dp, GlassBorderBright, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        topBar = {
            HeaderBar(
                account = account,
                onAccountClick = { selectedTab = AppTab.SETTINGS }
            )
        },
        bottomBar = {
            Surface(
                color = Color(0x2B0A0A0B),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = GlassBorder,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 10.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = GlassRed,
                                indicatorColor = Color(0x33EF4444),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active Running / Cashed Out Contract Floating HUD
            AnimatedVisibility(
                visible = activeContract != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                activeContract?.let { contract ->
                    LiveContractCard(
                        contract = contract,
                        onManualCashOut = { viewModel.manualCashOut() },
                        onDismiss = { viewModel.dismissActiveContractCard() }
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    AppTab.SIGNALS -> {
                        SignalsTab(
                            signals = liveSignals,
                            onExecuteSignal = { sym, stake, growth, target, sigId ->
                                viewModel.executeAccumulatorTrade(
                                    symbol = sym,
                                    stake = stake,
                                    growthRate = growth,
                                    targetTicks = target,
                                    isBot = false,
                                    signalId = sigId
                                )
                            }
                        )
                    }
                    AppTab.CHART -> {
                        LiveAnalysisTab(
                            selectedSymbol = selectedSymbol,
                            recentTicks = recentTicks,
                            onSelectSymbol = { viewModel.selectSymbol(it) },
                            onExecuteTrade = { sym, stake, growth, target ->
                                viewModel.executeAccumulatorTrade(
                                    symbol = sym,
                                    stake = stake,
                                    growthRate = growth,
                                    targetTicks = target,
                                    isBot = false
                                )
                            }
                        )
                    }
                    AppTab.BOT -> {
                        AutoBotTab(
                            settings = botSettings,
                            onToggleBot = { viewModel.toggleAutoBot(it) },
                            onUpdateSettings = { viewModel.updateBotSettings(it) },
                            onApplyPreset = { viewModel.applyPresetStrategy(it) }
                        )
                    }
                    AppTab.HISTORY -> {
                        HistoryTab(
                            trades = tradeHistory,
                            totalProfit = totalProfit,
                            totalCount = totalCount,
                            onClearHistory = { viewModel.clearAllHistory() }
                        )
                    }
                    AppTab.SETTINGS -> {
                        SettingsTab(
                            account = account,
                            onSaveToken = { token, isDemo -> viewModel.setDerivApiToken(token, isDemo) },
                            onClearAllData = { viewModel.clearAllHistory() }
                        )
                    }
                }
            }
        }
    }
}

