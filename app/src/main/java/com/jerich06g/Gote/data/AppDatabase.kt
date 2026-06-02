package com.jerich06g.Gote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jerich06g.Gote.data.dao.*
import com.jerich06g.Gote.data.entities.*

@Database(
    entities = [
        Round::class,
        Hole::class,
        Player::class,
        Score::class,
        SavedCourse::class,
        SavedHole::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roundDao(): RoundDao
    abstract fun holeDao(): HoleDao
    abstract fun playerDao(): PlayerDao
    abstract fun scoreDao(): ScoreDao
    abstract fun savedCourseDao(): SavedCourseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gote_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}