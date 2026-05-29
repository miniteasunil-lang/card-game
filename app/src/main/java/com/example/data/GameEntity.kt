package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "CHESS" or "CARD"
    val title: String, // e.g., "Casual Match" or "Weekly Poke Night"
    val player1: String = "", // White / Player 1 (Chess) or Team 1 Player 1 (Card)
    val player2: String = "", // Black / Player 2 (Chess) or Team 1 Player 2 (Card)
    val player3: String = "", // Team 2 Player 1 (Card)
    val player4: String = "", // Team 2 Player 2 (Card)
    val score1: Int = 0, // Total score for Player 1 / Team 1
    val score2: Int = 0, // Total score for Player 2 / Team 2
    val resultText: String = "", // e.g., "White Won" or "Team 1 Victory" or "Draw"
    val timestamp: Long = System.currentTimeMillis(),
    
    // Comma-separated scores per round (for Card Game)
    val team1RoundsCsv: String = "", 
    val team2RoundsCsv: String = ""
) {
    fun getTeam1Rounds(): List<Int> {
        if (team1RoundsCsv.isBlank()) return emptyList()
        return team1RoundsCsv.split(",").mapNotNull { it.toIntOrNull() }
    }

    fun getTeam2Rounds(): List<Int> {
        if (team2RoundsCsv.isBlank()) return emptyList()
        return team2RoundsCsv.split(",").mapNotNull { it.toIntOrNull() }
    }
}
