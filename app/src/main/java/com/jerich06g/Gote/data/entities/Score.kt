package com.jerich06g.Gote.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = Hole::class,
            parentColumns = ["holeId"],
            childColumns = ["holeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["playerId"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("holeId"), Index("playerId")]
)
data class Score(
    @PrimaryKey(autoGenerate = true)
    val scoreId: Long = 0,
    val holeId: Long,
    val playerId: Long,
    val strokes: Int
)