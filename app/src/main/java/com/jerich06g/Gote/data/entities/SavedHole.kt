package com.jerich06g.Gote.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_holes",
    foreignKeys = [ForeignKey(
        entity = SavedCourse::class,
        parentColumns = ["savedCourseId"],
        childColumns = ["savedCourseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("savedCourseId")]
)
data class SavedHole(
    @PrimaryKey(autoGenerate = true)
    val savedHoleId: Long = 0,
    val savedCourseId: Long,
    val holeNumber: Int,
    val par: Int,
    val distance: Int
)