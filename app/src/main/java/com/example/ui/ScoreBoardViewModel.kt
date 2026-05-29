package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameEntity
import com.example.data.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScoreBoardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository

    // Theme Settings Management
    private val sharedPrefs = application.getSharedPreferences("imroz_settings", android.content.Context.MODE_PRIVATE)
    
    private val _themeMode = MutableStateFlow(
        run {
            val saved = sharedPrefs.getString("theme_mode", null)
            if (saved != null) {
                try {
                    com.example.ui.theme.ThemeMode.valueOf(saved)
                } catch (e: Exception) {
                    com.example.ui.theme.ThemeMode.SYSTEM
                }
            } else {
                com.example.ui.theme.ThemeMode.SYSTEM
            }
        }
    )
    val themeMode: StateFlow<com.example.ui.theme.ThemeMode> = _themeMode

    fun setThemeMode(mode: com.example.ui.theme.ThemeMode) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode.name).apply()
    }

    init {
        val gameDao = AppDatabase.getDatabase(application).gameDao()
        repository = GameRepository(gameDao)
    }

    // Database flow for all games
    val allGames: StateFlow<List<GameEntity>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chess tracker state
    private val _chessP1Name = MutableStateFlow("Player 1")
    val chessP1Name: StateFlow<String> = _chessP1Name

    private val _chessP2Name = MutableStateFlow("Player 2")
    val chessP2Name: StateFlow<String> = _chessP2Name

    private val _isChessMatchActive = MutableStateFlow(false)
    val isChessMatchActive: StateFlow<Boolean> = _isChessMatchActive

    // Calculated chess statistics
    val chessStats = allGames.map { games ->
        val chessGames = games.filter { it.type == "CHESS" }
        var wins = 0
        var losses = 0
        var draws = 0
        chessGames.forEach { game ->
            when {
                game.resultText.contains("defeated", ignoreCase = true) -> {
                    // Check if Player 1 won or Player 2 won
                    if (game.resultText.startsWith(game.player1, ignoreCase = true)) {
                        wins++
                    } else {
                        losses++
                    }
                }
                game.resultText.contains("draw", ignoreCase = true) -> {
                    draws++
                }
                else -> draws++
            }
        }
        ChessStats(wins, losses, draws)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChessStats(0, 0, 0))

    // Card Tracker state
    private val _cardTeam1Name = MutableStateFlow("TEAM 1")
    val cardTeam1Name: StateFlow<String> = _cardTeam1Name

    private val _cardTeam2Name = MutableStateFlow("TEAM 2")
    val cardTeam2Name: StateFlow<String> = _cardTeam2Name

    private val _cardP1 = MutableStateFlow("")
    val cardP1: StateFlow<String> = _cardP1

    private val _cardP2 = MutableStateFlow("")
    val cardP2: StateFlow<String> = _cardP2

    private val _cardP3 = MutableStateFlow("")
    val cardP3: StateFlow<String> = _cardP3

    private val _cardP4 = MutableStateFlow("")
    val cardP4: StateFlow<String> = _cardP4

    // Lists of scores for each round
    private val _team1RoundScores = MutableStateFlow(listOf(0))
    val team1RoundScores: StateFlow<List<Int>> = _team1RoundScores

    private val _team2RoundScores = MutableStateFlow(listOf(0))
    val team2RoundScores: StateFlow<List<Int>> = _team2RoundScores

    // UI actions for Chess Tracker
    fun setChessP1Name(name: String) {
        _chessP1Name.value = name
    }

    fun setChessP2Name(name: String) {
        _chessP2Name.value = name
    }

    fun startChessMatch() {
        _isChessMatchActive.value = true
    }

    fun endChessMatch(result: Int) { // 1 = P1 Win, 2 = P2 Win, 0 = Draw
        viewModelScope.launch {
            val p1 = _chessP1Name.value.ifBlank { "Player 1" }
            val p2 = _chessP2Name.value.ifBlank { "Player 2" }
            
            val resultText = when (result) {
                1 -> "$p1 defeated $p2"
                2 -> "$p2 defeated $p1"
                else -> "Draw match"
            }

            val game = GameEntity(
                type = "CHESS",
                title = "$p1 vs $p2",
                player1 = p1,
                player2 = p2,
                score1 = if (result == 1) 1 else 0,
                score2 = if (result == 2) 1 else 0,
                resultText = resultText
            )
            repository.insertGame(game)
            _isChessMatchActive.value = false
        }
    }

    fun resetChessTracker() {
        _chessP1Name.value = ""
        _chessP2Name.value = ""
        _isChessMatchActive.value = false
    }

    // UI actions for Card Tracker
    fun setCardTeam1Name(name: String) {
        _cardTeam1Name.value = name
    }

    fun setCardTeam2Name(name: String) {
        _cardTeam2Name.value = name
    }

    fun setCardPlayer1(name: String) = _cardP1.update { name }
    fun setCardPlayer2(name: String) = _cardP2.update { name }
    fun setCardPlayer3(name: String) = _cardP3.update { name }
    fun setCardPlayer4(name: String) = _cardP4.update { name }

    fun adjustCardScore(team: Int, roundIndex: Int, amount: Int) {
        if (team == 1) {
            val currentList = _team1RoundScores.value.toMutableList()
            if (roundIndex in currentList.indices) {
                currentList[roundIndex] = currentList[roundIndex] + amount
                _team1RoundScores.value = currentList
            }
        } else {
            val currentList = _team2RoundScores.value.toMutableList()
            if (roundIndex in currentList.indices) {
                currentList[roundIndex] = currentList[roundIndex] + amount
                _team2RoundScores.value = currentList
            }
        }
    }

    fun setCardScoreDirectly(team: Int, roundIndex: Int, score: Int) {
        if (team == 1) {
            val currentList = _team1RoundScores.value.toMutableList()
            if (roundIndex in currentList.indices) {
                currentList[roundIndex] = score
                _team1RoundScores.value = currentList
            }
        } else {
            val currentList = _team2RoundScores.value.toMutableList()
            if (roundIndex in currentList.indices) {
                currentList[roundIndex] = score
                _team2RoundScores.value = currentList
            }
        }
    }

    fun addNewRound() {
        _team1RoundScores.value = _team1RoundScores.value + 0
        _team2RoundScores.value = _team2RoundScores.value + 0
    }

    fun resetCardTracker() {
        _cardTeam1Name.value = "TEAM 1"
        _cardTeam2Name.value = "TEAM 2"
        _cardP1.value = ""
        _cardP2.value = ""
        _cardP3.value = ""
        _cardP4.value = ""
        _team1RoundScores.value = listOf(0)
        _team2RoundScores.value = listOf(0)
    }

    fun saveCardGame() {
        viewModelScope.launch {
            val team1 = _cardTeam1Name.value.ifBlank { "TEAM 1" }
            val team2 = _cardTeam2Name.value.ifBlank { "TEAM 2" }
            val sum1 = _team1RoundScores.value.sum()
            val sum2 = _team2RoundScores.value.sum()

            val resultText = when {
                sum1 > sum2 -> "$team1 won with $sum1 points!"
                sum2 > sum1 -> "$team2 won with $sum2 points!"
                else -> "$team1 and $team2 tied at $sum1 points!"
            }

            val p1Combined = listOfNotNull(
                _cardP1.value.takeIf { it.isNotBlank() },
                _cardP2.value.takeIf { it.isNotBlank() }
            ).joinToString(", ").ifBlank { "Players T1" }

            val p2Combined = listOfNotNull(
                _cardP3.value.takeIf { it.isNotBlank() },
                _cardP4.value.takeIf { it.isNotBlank() }
            ).joinToString(", ").ifBlank { "Players T2" }

            val game = GameEntity(
                type = "CARD",
                title = "$team1 vs $team2",
                player1 = p1Combined,
                player2 = p2Combined,
                score1 = sum1,
                score2 = sum2,
                resultText = resultText,
                team1RoundsCsv = _team1RoundScores.value.joinToString(","),
                team2RoundsCsv = _team2RoundScores.value.joinToString(",")
            )
            repository.insertGame(game)
            resetCardTracker()
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            repository.deleteGame(game)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

data class ChessStats(val wins: Int, val losses: Int, val draws: Int)
