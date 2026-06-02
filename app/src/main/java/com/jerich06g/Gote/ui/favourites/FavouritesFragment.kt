package com.jerich06g.Gote.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jerich06g.Gote.R
import com.jerich06g.Gote.data.AppDatabase
import com.jerich06g.Gote.data.repository.GoteRepository
import com.jerich06g.Gote.ui.history.RoundCardAdapter
import com.jerich06g.Gote.ui.history.RoundDetailBottomSheet
import com.jerich06g.Gote.ui.viewmodels.FavouritesViewModel

class FavouritesFragment : Fragment() {

    private val viewModel: FavouritesViewModel by viewModels()
    private lateinit var adapter: RoundCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favourites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buildHeader(view)

        val db = AppDatabase.getDatabase(requireContext())
        val repository = GoteRepository(
            db.roundDao(), db.holeDao(), db.playerDao(),
            db.scoreDao(), db.savedCourseDao()
        )
        viewModel.init(repository)

        val rv = view.findViewById<RecyclerView>(R.id.rvFavourites)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyFavourites)

        adapter = RoundCardAdapter(
            rounds = emptyList(),
            roundStats = emptyMap(),
            onCardClick = { round ->
                viewModel.loadRoundSummary(round.roundId)
            },
            onFavouriteToggle = { round ->
                viewModel.unfavourite(round.roundId)
            }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        viewModel.favouriteRounds.observe(viewLifecycleOwner) { rounds ->
            viewModel.loadStatsForRounds(rounds)
            tvEmpty.visibility = if (rounds.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.roundStats.observe(viewLifecycleOwner) { stats ->
            adapter.updateRounds(viewModel.favouriteRounds.value ?: emptyList(), stats)
        }

        viewModel.selectedSummary.observe(viewLifecycleOwner) { summary ->
            summary ?: return@observe
            val sheet = RoundDetailBottomSheet()
            sheet.roundSummary = summary
            sheet.show(parentFragmentManager, "RoundDetail")
        }
    }
    private fun buildHeader(view: View) {
        val root = view as android.widget.LinearLayout
        val header = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
        }

        fun headerCell(text: String, widthDp: Int) = android.widget.TextView(requireContext()).apply {
            this.text = text
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            setTypeface(resources.getFont(R.font.goldman_bold))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                widthDp.dpToPx(),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        header.addView(headerCell("Course", 120))
        header.addView(headerCell("Date", 90))
        header.addView(headerCell("Holes", 60))
        header.addView(headerCell("Par", 60))

        // Insert header at position 2 (after the two TextViews)
        root.addView(header, 2)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}