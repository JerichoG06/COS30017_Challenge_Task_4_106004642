package com.jerich06g.Gote.data.repository

import com.jerich06g.Gote.data.dao.*
import com.jerich06g.Gote.data.entities.*

data class RoundSummary(
    val round: Round,
    val players: List<Player>,
    val holes: List<Hole>,
    val scores: List<Score>
)
class GoteRepository (
    private val roundDao: RoundDao,
    private val holeDao: HoleDao,
    private val playerDao: PlayerDao,
    private val scoreDao: ScoreDao,
    private val savedCourseDao: SavedCourseDao
) {
    // Rounds
    val allRounds = roundDao.getALLRounds()
    val recentRounds = roundDao.getRecentRounds()
    val favouriteRounds = roundDao.getFavouriteRounds()

    suspend fun insertRound(round: Round) = roundDao.insertRound(round)
    suspend fun setFavourite(roundId: Long, isFav: Boolean) = roundDao.setFavourite(roundId, isFav)

    suspend fun getRoundSummary(roundId: Long): RoundSummary {
        val round = roundDao.getRoundById(roundId)
        val players = playerDao.getPlayersForRound(roundId)
        val holes = holeDao.getHolesForRound(roundId)
        val scores = scoreDao.getScoresForRound(roundId)
        return RoundSummary(round, players, holes, scores)
    }

    // Holes
    suspend fun insertHoles(holes: List<Hole>) = holeDao.insertHoles(holes)
    suspend fun getHolesForRound(round: Long) = holeDao.getHolesForRound(round)
    suspend fun insertHoleForRound(hole: Hole) = holeDao.insertHole(hole)

    // Players
    suspend fun insertPlayers(players: List<Player>) = playerDao.insertPlayers(players)
    suspend fun getPlayersForRound(roundId: Long) = playerDao.getPlayersForRound(roundId)

    // Scores
    suspend fun insertScores(scores: List<Score>) = scoreDao.insertScores(scores)
    suspend fun getScoresForRound(roundId: Long) = scoreDao.getScoresForRound(roundId)

    // Saved Courses
    suspend fun searchCourses(query: String) = savedCourseDao.searchCourses(query)
    suspend fun getCourseByName(name: String) = savedCourseDao.getCourseByName(name)
    suspend fun getHolesForCourse(courseId: Long) = savedCourseDao.getHolesForCourse(courseId)

    suspend fun getHoleCountForRound(roundId: Long) = holeDao.getHolesForRound(roundId).size
    suspend fun getTotalParForRound(roundId: Long) = holeDao.getHolesForRound(roundId).sumOf { it.par }

    suspend fun saveCourse(courseName: String, holes: List<Hole>) {
        val existing = savedCourseDao.getCourseByName(courseName)
        if (existing == null) {
            val courseId = savedCourseDao.insertSavedCourse(SavedCourse(courseName = courseName))
            val savedHoles = holes.map {
                SavedHole(
                    savedCourseId = courseId,
                    holeNumber = it.holeNumber,
                    par = it.par,
                    distance = it.distance
                )
            }
            savedCourseDao.insertSavedHoles(savedHoles)
        }
    }
}