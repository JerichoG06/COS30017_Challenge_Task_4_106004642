package com.jerich06g.Gote.ui.round

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jerich06g.Gote.R
import com.jerich06g.Gote.ui.viewmodels.HoleRow

class HoleAdapter (
    private var holes: MutableList<HoleRow>,
    private val playerInitials: List<String>,
    private val onDeleteHole: (position: Int) -> Unit
) : RecyclerView.Adapter<HoleAdapter.HoleViewHolder>() {
    inner class HoleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHoleNumber: TextView = view.findViewById(R.id.tvHoleNumber)
        val etDistance: TextInputEditText = view.findViewById(R.id.etDistance)
        val etPar: TextInputEditText = view.findViewById(R.id.etPar)
        val playerScoresContainer: LinearLayout = view.findViewById(R.id.playerScoresContainer)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteHole)
        val tilPar: TextInputLayout = view.findViewById(R.id.tilPar)
        val tilDistance: TextInputLayout = view.findViewById(R.id.tilDistance)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HoleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hole_row, parent,false)
        return HoleViewHolder(view)
    }

    override fun onBindViewHolder(holder: HoleViewHolder, position: Int) {
        val hole = holes[position]
        holder.tvHoleNumber.text = hole.holeNumber.toString()
        // Par
        holder.etPar.removeTextChangedListener(holder.etPar.tag as? TextWatcher)
        holder.etPar.setText(if (hole.par == 0) "" else hole.par.toString())
        val parWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull() ?: 0
                holes[holder.bindingAdapterPosition].par = v
                holder.tilPar.error = if (v !in 1..9) "1-9" else null
            }
        }
        holder.etPar.tag = parWatcher
        holder.etPar.addTextChangedListener(parWatcher)

        // Distance
        holder.etDistance.removeTextChangedListener(holder.etDistance.tag as? TextWatcher)
        holder.etDistance.setText(if (hole.distance == 0) "" else hole.distance.toString())
        val distWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val v = s?.toString()?.toIntOrNull() ?: 0
                holes[holder.bindingAdapterPosition].distance = v
                holder.tilDistance.error = if (v < 0) "Invalid" else null
            }
        }
        holder.etDistance.tag = distWatcher
        holder.etDistance.addTextChangedListener(distWatcher)

        // Plater score columns (build dynamically
        holder.playerScoresContainer.removeAllViews()
        playerInitials.forEachIndexed { playerIndex, initials ->
            val til = TextInputLayout(holder.itemView.context).apply {
                hint = initials
                layoutParams = LinearLayout.LayoutParams(72.dpToPx(holder.itemView.context),
                    LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        marginStart = 4.dpToPx(holder.itemView.context)
                }
            }
            val et = TextInputEditText(til.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                maxLines = 1
                val score = hole.playerScores.getOrElse(playerIndex) { 0 }
                setText(if (score == 0) "" else score.toString())
            }
            val scoreWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val v = s?.toString()?.toIntOrNull() ?: 0
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_ID.toInt()) {
                        while (holes[pos].playerScores.size <= playerIndex) {
                            holes[pos].playerScores.add(0)
                        }
                        holes[pos].playerScores[playerIndex] = v
                    }
                }
            }
            et.addTextChangedListener(scoreWatcher)
            til.addView(et)
            holder.playerScoresContainer.addView(til)
        }

        // Delete
        holder.btnDelete.setOnClickListener {
            onDeleteHole(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount() = holes.size

    fun updateHoles(newHoles: MutableList<HoleRow>) {
        android.util.Log.d("GOTE", "updateHoles called, size: ${newHoles.size}")
        holes = newHoles
        notifyDataSetChanged()
    }

    // Extension to convert dp to px
    private fun Int.dpToPx(context: android.content.Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}