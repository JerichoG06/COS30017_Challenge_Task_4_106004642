package com.jerich06g.Gote.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jerich06g.Gote.data.entities.Round

@Dao
interface RoundDao {
    @Insert
    suspend fun insertRound(round: Round): Long

    @Update
    suspend fun updateRound(round: Round)

    @Delete
    suspend fun deleteRound(round: Round)

    @Query("SELECT * FROM rounds ORDER BY date DESC")
    fun getALLRounds(): LiveData<List<Round>>

    @Query("SELECT * FROM rounds WHERE isFavourite = 1 ORDER BY date DESC")
    fun getFavouriteRounds(): LiveData<List<Round>>

    @Query("SELECT * FROM rounds ORDER BY date DESC LIMIT 5")
    fun getRecentRounds(): LiveData<List<Round>>

    @Query("UPDATE rounds SET isFavourite = :isFav WHERE roundId = :roundId")
    suspend fun setFavourite(roundId: Long, isFav: Boolean)

    @Query("SELECT * FROM rounds WHERE roundId = :roundId")
    suspend fun getRoundById(roundId: Long): Round
}