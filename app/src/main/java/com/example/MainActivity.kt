package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.GameEntity
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.ScoreBoardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppHost()
            }
        }
    }
}

// Simple sealed class representing navigation destinations
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Chess : Screen("chess")
    object Card : Screen("card")
    object History : Screen("history")
    object Settings : Screen("settings")
}

@Composable
fun MainAppHost() {
    val viewModel: ScoreBoardViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
    // Manage Scaffold inner contents
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            CustomBottomBar(
                currentScreen = currentScreen,
                onNavigate = { screen -> currentScreen = screen }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToChess = { currentScreen = Screen.Chess },
                    onNavigateToCard = { currentScreen = Screen.Card },
                    onNavigateToHistory = { currentScreen = Screen.History }
                )
                Screen.Chess -> ChessScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.Home }
                )
                Screen.Card -> CardScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.Home }
                )
                Screen.History -> HistoryScreen(
                    viewModel = viewModel
                )
                Screen.Settings -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    // Custom styled curved bottom navigation to match the visual mockups
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isGamesActive = currentScreen == Screen.Home || currentScreen == Screen.Chess || currentScreen == Screen.Card
            
            // Games Bar Option
            NavigationTabItem(
                icon = Icons.Default.Home,
                label = "Games",
                isActive = isGamesActive,
                testTag = "tab_games",
                onClick = { onNavigate(Screen.Home) }
            )
            
            // History Bar Option
            NavigationTabItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = "History",
                isActive = currentScreen == Screen.History,
                testTag = "tab_history",
                onClick = { onNavigate(Screen.History) }
            )
            
            // Settings Bar Option
            NavigationTabItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isActive = currentScreen == Screen.Settings,
                testTag = "tab_settings",
                onClick = { onNavigate(Screen.Settings) }
            )
        }
    }
}

@Composable
fun NavigationTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ----------------------------------------------------
// 1. HOME SCREEN / GAME SELECTORS
// ----------------------------------------------------
@Composable
fun HomeScreen(
    viewModel: ScoreBoardViewModel,
    onNavigateToChess: () -> Unit,
    onNavigateToCard: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val recentGames by viewModel.allGames.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header Section
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Imroz Score Board",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { viewModel.clearHistory() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart All",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Welcome back!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ready to start a new match? Select your game to begin tallying scores.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }

        // Card Game Card Selector
        item {
            GameSelectorButton(
                title = "Card Game",
                description = "Multiplayer points, rounds tracking, and automatic winner calculation.",
                backgroundColor = Color(0xFFC0ECDA).copy(alpha = 0.2f),
                iconColor = Color(0xFF3E6658),
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBUjXuZLm7Id9PQBA8pkC85fA_KgI4TXbJt_TM_wUM6pj2Q0TZNvJiCvGm8QXZ-NMASt6DeXDh4qwaLOcG_ih4oK3vJw6Q1hDjr8s_A1BZGdgqNM8VaJ8-ib2zoRXd124hmxeBkkX2nlSiRHGQ9_U3fC1abtBYClkG3Xa-yeJZXW2XsdNIi3dsfUVJh29SlphsN7aZwIBihtKEgMj6DkbY7Ws2OASQEmSmBNxu0vccEATGw3pHSUFGYiGaE8PE8tfxh2IBOvVc79lwX",
                testTag = "select_card_game",
                onClick = onNavigateToCard
            )
        }

        // Chess Game Card Selector
        item {
            GameSelectorButton(
                title = "Chess",
                description = "Dual clocks, move history, and Elo rating updates for two players.",
                backgroundColor = Color(0xFF5D9CEC).copy(alpha = 0.15f),
                iconColor = Color(0xFF075FAB),
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuARiO-nWNZ2tdRIYoEdYbNmmK6J0Mh4uDJoTyMqQdvDY9IFYGw5chUEbRMXVy7wKLAyDkhlI5aqaRtMQpUemqMVdB8gyZUbhkEH3iZH5tjOgjbPSFmZCwvzBLhC0iZTlbdFfFZ3SFukl-D1QRp-9rkrTLcwP71oNO_neL-Z35Fgj7EkKvPAkwNTNrXBuOtmp_hffaW3FglYL6WTGenTR7nSjSMRDzCBZnQHJSIUfKSob2v07Jo7wPKlmNtjibn3s54Sa8F5xdSKJdFJ",
                testTag = "select_chess_game",
                onClick = onNavigateToChess
            )
        }

        // Quick History header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "QUICK HISTORY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onNavigateToHistory() }
                        .padding(horizontal = 8.dp)
                )
            }
        }

        // List of history logs (up to 3 items on home)
        if (recentGames.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEEF5F7))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matches recorded yet.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("empty_history_label")
                        )
                    }
                }
            }
        } else {
            items(recentGames.take(3)) { game ->
                HistoryItemRow(game = game)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GameSelectorButton(
    title: String,
    description: String,
    backgroundColor: Color,
    iconColor: Color,
    imageUrl: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFEEF5F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (title == "Chess") Icons.Default.Face else Icons.Default.Star,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image area loading hotlink
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor.copy(alpha = 0.1f))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "$title banner image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun HistoryItemRow(game: GameEntity) {
    val isChess = game.type == "CHESS"
    val borderStrokeColor = if (isChess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val timestampStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(game.timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFEEF5F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Left color accent bar
                    drawRoundRect(
                        color = borderStrokeColor,
                        size = androidx.compose.ui.geometry.Size(6.dp.toPx(), size.height),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.resultText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "$timestampStr •  ${game.title}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "outcome indicators",
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ----------------------------------------------------
// 2. CHESS SCREEN (PLAYER SETUP & SINGLE SCORE TICKER)
// ----------------------------------------------------
@Composable
fun ChessScreen(
    viewModel: ScoreBoardViewModel,
    onBack: () -> Unit
) {
    val p1Name by viewModel.chessP1Name.collectAsStateWithLifecycle()
    val p2Name by viewModel.chessP2Name.collectAsStateWithLifecycle()
    val isMatchActive by viewModel.isChessMatchActive.collectAsStateWithLifecycle()
    val stats by viewModel.chessStats.collectAsStateWithLifecycle()
    val recentMatches by viewModel.allGames.collectAsStateWithLifecycle()
    
    val chessGames = remember(recentMatches) { recentMatches.filter { it.type == "CHESS" } }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Screen Title header
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back home",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Chess Score Board",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { viewModel.resetChessTracker() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset match form",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Match Setup Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("chess_setup_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFFEEF5F7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Match Setup",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Player 1 input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Player 1 (White)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        OutlinedTextField(
                            value = p1Name,
                            onValueChange = { viewModel.setChessP1Name(it) },
                            placeholder = { Text("Enter name") },
                            modifier = Modifier
                                .testTag("chess_p1_input")
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    // Player 2 input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Player 2 (Black)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        OutlinedTextField(
                            value = p2Name,
                            onValueChange = { viewModel.setChessP2Name(it) },
                            placeholder = { Text("Enter name") },
                            modifier = Modifier
                                .testTag("chess_p2_input")
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    Button(
                        onClick = { viewModel.startChessMatch() },
                        modifier = Modifier
                            .testTag("start_match_button")
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Start Match",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Stats Bento Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wins box
                ChessStatCard(
                    title = "Wins",
                    value = stats.wins.toString(),
                    containerColor = Color(0xFFE8F2FF),
                    textColor = Color(0xFF075FAB),
                    modifier = Modifier.weight(1f)
                )
                // Losses box
                ChessStatCard(
                    title = "Losses",
                    value = stats.losses.toString(),
                    containerColor = Color(0xFFFFECEE),
                    textColor = Color(0xFFBA1A1A),
                    modifier = Modifier.weight(1f)
                )
                // Draws box
                ChessStatCard(
                    title = "Draws",
                    value = stats.draws.toString(),
                    containerColor = Color(0xFFE8F7F0),
                    textColor = Color(0xFF3E6658),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Score Recorder section
        if (isMatchActive) {
            item {
                Card(
                    modifier = Modifier
                        .testTag("match_active_recorder")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFEEF5F7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Record Result",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Player 1 Winner
                        ChessRecorderButton(
                            label = "${p1Name.ifBlank { "Player 1" }} Win",
                            borderColor = MaterialTheme.colorScheme.primaryContainer,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { viewModel.endChessMatch(1) }
                        )
                        
                        // Player 2 Winner
                        ChessRecorderButton(
                            label = "${p2Name.ifBlank { "Player 2" }} Win",
                            borderColor = MaterialTheme.colorScheme.secondaryContainer,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = { viewModel.endChessMatch(2) }
                        )
                        
                        // Draw Match
                        ChessRecorderButton(
                            label = "Draw Match",
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { viewModel.endChessMatch(0) }
                        )
                    }
                }
            }
        }

        // Match History listing
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Last 10 Games",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        if (chessGames.isEmpty()) {
            item {
                Text(
                    text = "No matches recorded yet.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
        } else {
            items(chessGames.take(10)) { game ->
                HistoryItemRow(game = game)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChessStatCard(
    title: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun ChessRecorderButton(
    label: String,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


// ----------------------------------------------------
// 3. CARD GAME SCREEN (DETAILED SCORE SHEETS)
// ----------------------------------------------------
@Composable
fun CardScreen(
    viewModel: ScoreBoardViewModel,
    onBack: () -> Unit
) {
    val t1Name by viewModel.cardTeam1Name.collectAsStateWithLifecycle()
    val t2Name by viewModel.cardTeam2Name.collectAsStateWithLifecycle()
    val p1 by viewModel.cardP1.collectAsStateWithLifecycle()
    val p2 by viewModel.cardP2.collectAsStateWithLifecycle()
    val p3 by viewModel.cardP3.collectAsStateWithLifecycle()
    val p4 by viewModel.cardP4.collectAsStateWithLifecycle()
    
    val t1Rounds by viewModel.team1RoundScores.collectAsStateWithLifecycle()
    val t2Rounds by viewModel.team2RoundScores.collectAsStateWithLifecycle()
    
    val t1Total = t1Rounds.sum()
    val t2Total = t2Rounds.sum()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Card Game Board",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { viewModel.resetCardTracker() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset table scores",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Columns Area for Team 1 and Team 2
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Team 1 Column
                    Column(modifier = Modifier.weight(1f)) {
                        CardSetupBox(
                            teamTitle = t1Name,
                            onTeamTitleChange = { viewModel.setCardTeam1Name(it) },
                            p1 = p1,
                            onP1Change = { viewModel.setCardPlayer1(it) },
                            p2 = p2,
                            onP2Change = { viewModel.setCardPlayer2(it) },
                            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            playerLabel1 = "Player 1",
                            playerLabel2 = "Player 2"
                        )
                    }

                    // Team 2 Column
                    Column(modifier = Modifier.weight(1f)) {
                        CardSetupBox(
                            teamTitle = t2Name,
                            onTeamTitleChange = { viewModel.setCardTeam2Name(it) },
                            p1 = p3,
                            onP1Change = { viewModel.setCardPlayer3(it) },
                            p2 = p4,
                            onP2Change = { viewModel.setCardPlayer4(it) },
                            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            playerLabel1 = "Player 3",
                            playerLabel2 = "Player 4"
                        )
                    }
                }
            }

            // Rounds List
            val maxRounds = maxOf(t1Rounds.size, t2Rounds.size)
            items(maxRounds) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Team 1 Round Item
                    Column(modifier = Modifier.weight(1f)) {
                        if (index < t1Rounds.size) {
                            RoundScoreAdjuster(
                                roundNumber = index + 1,
                                value = t1Rounds[index],
                                onValueChange = { viewModel.setCardScoreDirectly(1, index, it) },
                                onAdjust = { amount -> viewModel.adjustCardScore(1, index, amount) },
                                btnColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Team 2 Round Item
                    Column(modifier = Modifier.weight(1f)) {
                        if (index < t2Rounds.size) {
                            RoundScoreAdjuster(
                                roundNumber = index + 1,
                                value = t2Rounds[index],
                                onValueChange = { viewModel.setCardScoreDirectly(2, index, it) },
                                onAdjust = { amount -> viewModel.adjustCardScore(2, index, amount) },
                                btnColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Add Round Button (Dashed Outline)
            item {
                Box(
                    modifier = Modifier
                        .testTag("add_round_button")
                        .fillMaxWidth()
                        .height(64.dp)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                            )
                            drawRoundRect(
                                color = Color(0xFF727782),
                                style = stroke,
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.addNewRound() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "add icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ADD NEW ROUND",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(180.dp)) // Leave space for sticky bottom
            }
        }

        // Floating Sticky Bottom Tally
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFE8EFF1))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Totals header layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Team 1 Total
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${t1Name.uppercase()} TOTAL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = t1Total.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (t1Total > t2Total) {
                            Text(
                                text = "Winning",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .testTag("winning_badge_t1")
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Team 2 Total
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${t2Name.uppercase()} TOTAL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = t2Total.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (t2Total > t1Total) {
                            Text(
                                text = "Winning",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .testTag("winning_badge_t2")
                                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.saveCardGame()
                    },
                    modifier = Modifier
                        .testTag("save_game_button")
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "SAVE GAME",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CardSetupBox(
    teamTitle: String,
    onTeamTitleChange: (String) -> Unit,
    p1: String,
    onP1Change: (String) -> Unit,
    p2: String,
    onP2Change: (String) -> Unit,
    borderColor: Color,
    playerLabel1: String,
    playerLabel2: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Team Name editable
            OutlinedTextField(
                value = teamTitle,
                onValueChange = onTeamTitleChange,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            
            HorizontalDivider(color = Color(0xFFEEF5F7), thickness = 1.dp)
            
            // Player Inputs
            OutlinedTextField(
                value = p1,
                onValueChange = onP1Change,
                placeholder = { Text(playerLabel1, fontSize = 12.sp) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, textAlign = TextAlign.Center),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFEEF5F7),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            OutlinedTextField(
                value = p2,
                onValueChange = onP2Change,
                placeholder = { Text(playerLabel2, fontSize = 12.sp) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, textAlign = TextAlign.Center),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFEEF5F7),
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun RoundScoreAdjuster(
    roundNumber: Int,
    value: Int,
    onValueChange: (Int) -> Unit,
    onAdjust: (Int) -> Unit,
    btnColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC1C6D3).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "ROUND $roundNumber",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            
            // Score decrement / input / increment
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // -10 Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF5F7))
                        .clickable { onAdjust(-10) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "-10",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // score text display
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = {
                        val parsed = it.toIntOrNull() ?: 0
                        onValueChange(parsed)
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(48.dp)
                        .height(44.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // +10 Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(btnColor.copy(alpha = 0.2f))
                        .clickable { onAdjust(10) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+10",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = btnColor
                    )
                }
            }

            // +50 Shortcut button below
            Button(
                onClick = { onAdjust(50) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = btnColor.copy(alpha = 0.82f)
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Text(
                    text = "+50",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// ----------------------------------------------------
// 4. HISTORY SCREEN
// ----------------------------------------------------
@Composable
fun HistoryScreen(viewModel: ScoreBoardViewModel) {
    val historyItems by viewModel.allGames.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Game History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Full logs of all recorded games and scores.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )
        
        HorizontalDivider(color = Color(0xFFE8EFF1))
        
        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("empty_history_box"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No games saved yet.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(historyItems) { game ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(2.dp, Color(0xFFEEF5F7)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Game Type badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (game.type == "CHESS") Color(0xFFE8F2FF) else Color(0xFFE8F7F0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = game.type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (game.type == "CHESS") Color(0xFF075FAB) else Color(0xFF3E6658)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { viewModel.deleteGame(game) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete record",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Text(
                                text = game.resultText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            HorizontalDivider(color = Color(0xFFEEF5F7))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = game.title,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm, MMM d", Locale.getDefault()).format(Date(game.timestamp)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ----------------------------------------------------
// 5. SETTINGS SCREEN
// ----------------------------------------------------
@Composable
fun SettingsScreen(viewModel: ScoreBoardViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showDialog = false
                    }
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Reset Application Data") },
            text = { Text("Are you sure you want to delete all chess and card scoreboard histories? This action is permanent.") }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Configure preferences & maintain scoreboard data.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )
        
        HorizontalDivider(color = Color(0xFFE8EFF1))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Option 1: Clean History
        SettingsRowItem(
            icon = Icons.Default.Delete,
            title = "Delete Saved Game Data",
            subtitle = "Perform general cleanup of Room database records.",
            testTag = "btn_clear_data",
            onClick = { showDialog = true }
        )
        
        // Option 2: App details
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFEEF5F7))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "About Imroz Score Board",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "A material UI local score tracker styled precisely to provide premium game score tallying ergonomics.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Text(
                    text = "Version 1.0.0",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEEF5F7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
