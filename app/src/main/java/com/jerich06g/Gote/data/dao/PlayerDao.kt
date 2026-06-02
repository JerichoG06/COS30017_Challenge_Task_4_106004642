package com.jerich06g.Gote.data.dao

import androidx.room.*
import com.jerich06g.Gote.data.entities.Player

@Dao
interface PlayerDao{
    @Insert
    suspend fun insertPlayer(player: Player): Long

    @Insert
    suspend fun insertPlayers(players: List<Player>): List<Long>

    @Delete
    suspend fun deletePlayer(player: Player)

    @Query("SELECT * FROM players WHERE roundId = :roundId")
    suspend fun getPlayersForRound(roundId: Long): List<Player>
}