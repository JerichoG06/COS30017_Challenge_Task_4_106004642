package com.jerich06g.Gote.data.dao

import androidx.room.*
import com.jerich06g.Gote.data.entities.Hole

@Dao
interface HoleDao{
    @Insert
    suspend fun insertHole(hole: Hole): Long

    @Insert
    suspend fun insertHoles(holes: List<Hole>)

    @Update
    suspend fun updateHole(hole: Hole)

    @Delete
    suspend fun deleteHole(hole: Hole)

    @Query("SELECT * FROM holes WHERE roundId = :roundId ORDER BY holeNumber ASC")
    suspend fun getHolesForRound(roundId: Long): List<Hole>
}