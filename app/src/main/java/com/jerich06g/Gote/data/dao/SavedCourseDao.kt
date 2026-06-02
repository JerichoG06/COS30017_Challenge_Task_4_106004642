package com.jerich06g.Gote.data.dao

import androidx.room.*
import com.jerich06g.Gote.data.entities.SavedCourse
import com.jerich06g.Gote.data.entities.SavedHole

@Dao
interface SavedCourseDao {
    @Insert
    suspend fun insertSavedCourse(course: SavedCourse): Long

    @Insert
    suspend fun insertSavedHoles(holes: List<SavedHole>)

    @Delete
    suspend fun deleteSavedCourse(course: SavedCourse)

    @Query("SELECT * FROM saved_courses WHERE courseName LIKE :query || '%'")
    suspend fun searchCourses(query: String): List<SavedCourse>

    @Query("SELECT * FROM saved_courses WHERE courseName = :name LIMIT 1")
    suspend fun getCourseByName(name: String): SavedCourse?

    @Query("SELEct * FROM saved_holes WHERE savedCourseId = :courseId ORDER BY holeNumber ASC")
    suspend fun getHolesForCourse(courseId: Long): List<SavedHole>
}