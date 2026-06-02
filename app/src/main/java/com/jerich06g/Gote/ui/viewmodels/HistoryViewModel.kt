package com.jerich06g.Gote.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerich06g.Gote.data.entities.Round
import com.jerich06g.Gote.data.repository.GoteRepository
import com.jerich06g.Gote.data.repository.RoundSummary
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private lateinit var repository: GoteRepository

    lateinit var recentRounds: LiveData<List<Round>>

    private val _selectedSummary = MutableLiveData<RoundSummary?>()
    val selectedSummary: LiveData<RoundSummary?> get() = _selectedSummary

    private val _roundStats = MutableLiveData<Map<Long, Pair<Int, Int>>>()
    val roundStats: LiveData<Map<Long, Pair<Int, Int>>> get() = _roundStats

    fun init(repo: GoteRepository) {
        repository = repo
        recentRounds = repo.recentRounds
        android.util.Log.d("GOTE", "HistoryViewModel init, recentRounds assigned: $recentRounds")
    }

    fun loadStatsForRounds(rounds: List<Round>) {
        viewModelScope.launch {
            val stats = rounds.associate { round ->
                val holeCount = repository.getHoleCountForRound(round.roundId)
                val totalPar = repository.getTotalParForRound(round.roundId)
                round.roundId to Pair(holeCount, totalPar)
            }
            _roundStats.postValue(stats)
        }
    }
    fun loadRoundSummary(roundId: Long) {
        viewModelScope.launch {
            val summary = repository.getRoundSummary(roundId)
            _selectedSummary.postValue(summary)
        }
    }

    fun setFavourite(roundId: Long, isFav: Boolean) {
        viewModelScope.launch {
            repository.setFavourite(roundId, isFav)
        }
    }
}