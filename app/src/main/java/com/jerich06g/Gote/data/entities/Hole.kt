package com.jerich06g.Gote.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "holes",
    foreignKeys = [ForeignKey(
        entity = Round::class,
        parentColumns = ["roundId"],
        childColumns = ["roundId"],
        onDelete = ForeignKey.CASCADE //auto delete child if parent is deleted
    )],
)
data class Hole(
    @PrimaryKey(autoGenerate = true)
    val holeId: Long = 0,
    val roundId: Long,
    val holeNumber: Int,
    val par: Int,
    val distance: Int
)