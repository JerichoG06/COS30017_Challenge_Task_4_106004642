package com.jerich06g.Gote.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_courses")
data class SavedCourse(
    @PrimaryKey(autoGenerate = true)
    val savedCourseId: Long = 0,
    val courseName: String
)