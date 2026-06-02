package com.jerich06g.Gote.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerich06g.Gote.data.entities.*
import com.jerich06g.Gote.data.repository.GoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HoleRow(
    var holeNumber: Int,
    var par: Int = 4,
    var distance: Int = 0,
    val playerScores: MutableList<Int> = mutableListOf()
)

class ActiveRoundViewModel : ViewModel() {
    private val _holes = MutableLiveData<MutableList<HoleRow>>(mutableListOf())
    val holes: LiveData<MutableList<HoleRow>> get() = _holes

    var courseName: String = ""
    var playerInitials: List<String> = emptyList()
    lateinit var repository: GoteRepository

    fun initRound(
        course: String,
        players: List<String>,
        preloadedHoles: List<SavedHole>,
        repo: GoteRepository
    ) {
        courseName = course
        playerInitials = players
        repository = repo

        if (preloadedHoles.isNotEmpty()) {
            val rows = preloadedHoles.map { saved ->
                HoleRow(
                    holeNumber = saved.holeNumber,
                    par = saved.par,
                    distance = saved.distance,
                    playerScores = MutableList(players.size) { 0 }
                )
            }.toMutableList()
            _holes.value = rows
        } else {
            addHole()
        }
    }

    fun addHole() {
        val current = _holes.value ?: mutableListOf()
        android.util.Log.d("GOTE", "addHole called on ViewModel ${this.hashCode()}, before size: ${current.size}")
        current.add(
            HoleRow(
                holeNumber = current.size + 1,
                playerScores = MutableList(playerInitials.size) { 0 }
            )
        )
        _holes.value = current.toMutableList()
        android.util.Log.d("GOTE", "addHole done, new size: ${_holes.value?.size}")
    }

    fun deleteHole(position: Int) {
        val current = _holes.value ?: return
        current.removeAt(position)
        // Re-index hole numbers
        current.forEachIndexed { index, hole ->
           current[index] = hole.copy(holeNumber = index + 1)
        }
        _holes.value = current.toMutableList()
    }

    fun saveRound(onComplete: (success: Boolean, roundId: Long, holes: List<HoleRow>) -> Unit) {
        val holes = _holes.value ?: emptyList()
        if (holes.isEmpty()) {
            onComplete(false, -1, emptyList())
            return
        }

        viewModelScope.launch {
            try {
                // 1. Insert round
                val round = Round(courseName = courseName)
                val roundId = repository.insertRound(round)

                // 2. Insert players
                val playerEntities = playerInitials.map { Player(roundId = roundId, initials = it) }
                val playerIds = repository.insertPlayers(playerEntities)

                // 3. Insert holes + scores
                holes.forEach { row ->
                    val hole = Hole(
                        roundId = roundId,
                        holeNumber = row.holeNumber,
                        par = row.par,
                        distance = row.distance
                    )
                    // insertHole returns new holdId
                    val holeId = repository.insertHoleForRound(hole)

                    row.playerScores.forEachIndexed { index, strokes ->
                        if (index < playerIds.size) {
                            repository.insertScores(
                                listOf(
                                    Score(
                                        holeId = holeId,
                                        playerId = playerIds[index],
                                        strokes = strokes
                                    )
                                )
                            )
                        }
                    }
                }

                onComplete(true, roundId, holes)
            } catch (e: Exception) {
                onComplete(false, -1, emptyList())
            }
        }
    }
    fun checkAndPromptSaveCourse(
        courseName: String,
        holes: List<HoleRow>,
        onResult: (alreadySaved: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val existing = repository.getCourseByName(courseName)
            withContext(kotlinx.coroutines.Dispatchers.Main) {
                onResult(existing != null)
            }
        }
    }

    fun saveCourse(courseName: String, holes: List<HoleRow>) {
        viewModelScope.launch {
            val holeEntities = holes.map {
                com.jerich06g.Gote.data.entities.Hole(
                    roundId = 0,
                    holeNumber = it.holeNumber,
                    par = it.par,
                    distance = it.distance
                )
            }
            repository.saveCourse(courseName, holeEntities)
        }
    }
}