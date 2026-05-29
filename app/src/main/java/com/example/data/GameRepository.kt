package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()
    
    fun getGamesByType(type: String): Flow<List<GameEntity>> {
        return gameDao.getGamesByType(type)
    }

    suspend fun getGameById(id: Int): GameEntity? {
        return gameDao.getGameById(id)
    }

    suspend fun insertGame(game: GameEntity): Long {
        return gameDao.insertGame(game)
    }

    suspend fun deleteGame(game: GameEntity) {
        gameDao.deleteGame(game)
    }

    suspend fun clearAll() {
        gameDao.deleteAllGames()
    }
}
