package com.jerich06g.Gote.data.dao

import androidx.room.*
import com.jerich06g.Gote.data.entities.Score

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(score: Score)

    @Insert
    suspend fun insertScores(scores: List<Score>)

    @Update
    suspend fun updateScore(score: Score)

    @Query("SELECT * FROM scores WHERE holeId = :holeId")
    suspend fun getScoresForHole(holeId: Long): List<Score>

    @Query("SELECT * FROM scores WHERE playerId = :playerId")
    suspend fun getScoresForPlayer(playerId: Long): List<Score>

    @Query("""
        SELECT s.* From scores s
        INNER JOIN holes h ON s.holeId = h.holeId
        WHERE h.roundId = :roundId
    """)
    suspend fun getScoresForRound(roundId: Long): List<Score>
}