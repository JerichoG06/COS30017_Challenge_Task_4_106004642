package com.jerich06g.Gote.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jerich06g.Gote.R
import com.jerich06g.Gote.data.entities.Round
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoundCardAdapter(
    private var rounds: List<Round>,
    private var roundStats: Map<Long, Pair<Int, Int>> = emptyMap(),
    private val onCardClick: (Round) -> Unit,
    private val onFavouriteToggle: (Round) -> Unit
) : RecyclerView.Adapter<RoundCardAdapter.RoundCardViewHolder>() {

    inner class RoundCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvHoleCount: TextView = view.findViewById(R.id.tvHoleCount)
        val tvPlayerScores: TextView = view.findViewById(R.id.tvPlayerScores)
        val btnFavourite: ImageButton = view.findViewById(R.id.btnFavourite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_round_card, parent, false)
        return RoundCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoundCardViewHolder, position: Int) {
        val round = rounds[position]
        holder.tvCourseName.text = round.courseName
        val sdf = SimpleDateFormat("d/M/yy", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(round.date))

        val stats = roundStats[round.roundId]
        holder.tvHoleCount.text = stats?.first?.toString() ?: "-"
        holder.tvPlayerScores.text = stats?.second?.toString() ?: "-"
        holder.btnFavourite.setImageResource(
            if (round.isFavourite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
        holder.btnFavourite.setOnClickListener { onFavouriteToggle(round) }
        holder.itemView.setOnClickListener { onCardClick(round) }
    }

    override fun getItemCount() = rounds.size

    fun updateRounds(newRounds: List<Round>, newStats: Map<Long, Pair<Int, Int>> = emptyMap()) {
        rounds = newRounds
        roundStats = newStats
        notifyDataSetChanged()
    }
}