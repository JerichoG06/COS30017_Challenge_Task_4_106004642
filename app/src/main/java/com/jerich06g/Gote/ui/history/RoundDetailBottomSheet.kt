package com.jerich06g.Gote.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jerich06g.Gote.R
import com.jerich06g.Gote.data.entities.Hole
import com.jerich06g.Gote.data.entities.Player
import com.jerich06g.Gote.data.entities.Round
import com.jerich06g.Gote.data.entities.Score
import com.jerich06g.Gote.data.repository.RoundSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoundDetailBottomSheet : BottomSheetDialogFragment() {
    var roundSummary: RoundSummary? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_round_detail_bottom_sheet,container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val summary = roundSummary ?: return
        view.findViewById<TextView>(R.id.tvBottomSheetTitle).text = summary.round.courseName

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        view.findViewById<TextView>(R.id.tvBottomSheetDate).text =
            sdf.format(Date(summary.round.date))

        val container = view.findViewById<LinearLayout>(R.id.bottomSheetContent)
        buildContent(container, summary)
    }

    private fun buildContent(container: LinearLayout, summary: RoundSummary) {
        val content = requireContext()

        // Header row: Hole | Par | Dist | P1 | P2 ...
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        addCell(headerRow, "Hole", bold = true, widthDp = 32)
        addCell(headerRow, "Par", bold = true, widthDp = 48)
        addCell(headerRow, "Dist", bold = true, widthDp = 56)
        summary.players.forEach { player ->
            addCell(headerRow, player.initials, bold = true, widthDp = 56)
        }
        container.addView(headerRow)

        // One row per hole
        summary.holes.forEach { hole ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            addCell(row, hole.holeNumber.toString(), widthDp = 32)
            addCell(row, hole.par.toString(), widthDp = 48)
            addCell(row, hole.distance.toString(), widthDp = 56)

            summary.players.forEach { player ->
                val score = summary.scores.find {
                    it.holeId == hole.holeId && it.playerId == player.playerId
                }
                addCell(row, score?.strokes?.toString() ?: "-", widthDp = 56)
            }
            container.addView(row)
        }

        // Totals row
        val totalsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        addCell(totalsRow, "Total", bold = true, widthDp = 80)
        val totalPar = summary.holes.sumOf { it.par }
        addCell(totalsRow, totalPar.toString(), bold = true, widthDp = 56)

        summary.players.forEach { player ->
            val totalScore = summary.scores
                .filter { it.playerId == player.playerId }
                .sumOf { it.strokes }
            addCell(totalsRow, totalScore.toString(), bold = true, widthDp = 56)
        }
        container.addView(totalsRow)
    }
    private fun addCell(
        row: LinearLayout,
        text: String,
        bold: Boolean = false,
        widthDp: Int
    ){
        val tv = TextView(requireContext()).apply {
            this.text = text
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                (widthDp * resources.displayMetrics.density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(1, 4, 2, 4) }
        }
        row.addView(tv)
    }
}