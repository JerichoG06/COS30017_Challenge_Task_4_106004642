package com.jerich06g.Gote.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    foreignKeys = [ForeignKey(
        entity = Round::class,
        parentColumns = ["roundId"],
        childColumns = ["roundId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("roundId")]
)
data class Player(
    @PrimaryKey(autoGenerate = true)
    val playerId: Long = 0,
    val roundId: Long,
    val initials: String
)