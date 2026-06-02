package com.jerich06g.Gote.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds")
data class Round (
    @PrimaryKey(autoGenerate = true)
    val roundId: Long = 0,
    val courseName: String,
    val date: Long = System.currentTimeMillis(),
    val isFavourite: Boolean = false
)
