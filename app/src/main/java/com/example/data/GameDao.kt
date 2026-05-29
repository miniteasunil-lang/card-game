package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE type = :type ORDER BY timestamp DESC")
    fun getGamesByType(type: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: Int): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games")
    suspend fun deleteAllGames()
}
