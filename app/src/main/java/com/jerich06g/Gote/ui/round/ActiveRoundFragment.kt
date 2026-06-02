package com.jerich06g.Gote.ui.round

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.jerich06g.Gote.R
import com.jerich06g.Gote.data.AppDatabase
import com.jerich06g.Gote.data.repository.GoteRepository
import com.jerich06g.Gote.ui.viewmodels.ActiveRoundViewModel

class ActiveRoundFragment : Fragment() {

    private val viewModel: ActiveRoundViewModel by viewModels()
    private lateinit var adapter: HoleAdapter
    private lateinit var repository: GoteRepository
    private var playerInitials: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_active_round, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        repository = GoteRepository(
            db.roundDao(), db.holeDao(), db.playerDao(),
            db.scoreDao(), db.savedCourseDao()
        )

        val courseName = arguments?.getString("courseName") ?: ""
        playerInitials = arguments?.getStringArrayList("playerInitials") ?: arrayListOf()
        val preloadedPars = arguments?.getIntegerArrayList("preloadedPars") ?: arrayListOf()
        val preloadedDistances = arguments?.getIntegerArrayList("preloadedDistances") ?: arrayListOf()

        val preloadedHoles = preloadedPars.indices.map { i ->
            com.jerich06g.Gote.data.entities.SavedHole(
                savedCourseId = 0,
                holeNumber = i + 1,
                par = preloadedPars[i],
                distance = preloadedDistances[i]
            )
        }

        viewModel.initRound(courseName, playerInitials, preloadedHoles, repository)

        view.findViewById<TextView>(R.id.tvRoundTitle).text =
            "$courseName - ${playerInitials.joinToString(", ")}"

        buildHeader()

        val rv = view.findViewById<RecyclerView>(R.id.rvHoles)
        adapter = HoleAdapter(
            holes = mutableListOf(),
            playerInitials = playerInitials,
            onDeleteHole = { position -> viewModel.deleteHole(position) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val scrollView = view.findViewById<android.widget.HorizontalScrollView>(R.id.horizontalScroll)
        val bottomBar = view.findViewById<LinearLayout>(R.id.bottomBar)
        val limeContainer = view.findViewById<LinearLayout>(R.id.limeContainer)

        viewModel.holes.observe(viewLifecycleOwner) { holes ->
            android.util.Log.d("GOTE", "Observer fired, hole count: ${holes.size}")
            adapter.updateHoles(holes)
        }

        // Back button with warning
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Leave Round")
                .setMessage("All edits made will be lose. Are you sure you want to exit?")
                .setPositiveButton("Leave") { _, _ ->
                    findNavController().navigate(R.id.homeFragment)
                }
                .setNegativeButton("Stay", null)
                .show()
        }

        view.findViewById<MaterialButton>(R.id.btnAddHole).setOnClickListener {
            viewModel.addHole()
        }

        view.findViewById<MaterialButton>(R.id.btnSaveRound).setOnClickListener {
            val holes = viewModel.holes.value ?: emptyList()

            if (holes.isEmpty()) {
                Toast.makeText(requireContext(), "Add at least one hole", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val invalid = holes.any { it.par !in 1..9 }
            if (invalid) {
                Toast.makeText(requireContext(), "All holes need a valid par (1-9)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val missingScores = holes.any { hole ->
                hole.playerScores.size < playerInitials.size ||
                        hole.playerScores.any { it <= 0 }
            }
            if (missingScores) {
                Toast.makeText(requireContext(), "All players need a score for every hole", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveRound { success, _, holeRows ->
                requireActivity().runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "Round saved!", Toast.LENGTH_SHORT).show()
                        // Check if course should be saved
                        viewModel.checkAndPromptSaveCourse(courseName, holeRows) { alreadySaved ->
                            if (!alreadySaved) {
                                showSaveCourseDialog(courseName, holeRows)
                            } else {
                                findNavController().navigate(R.id.historyFragment)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error saving round", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun buildHeader() {
        val headerContainer = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            background = android.graphics.drawable.ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.gote_dark_green)
            )
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
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

        headerContainer.addView(headerCell("Hole", 42))
        headerContainer.addView(headerCell("Dist", 64))
        headerContainer.addView(headerCell("Par", 56))

        playerInitials.forEach { initials ->
            headerContainer.addView(headerCell(initials, 72))
        }

        headerContainer.addView(android.view.View(requireContext()).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(36.dpToPx(), 1)
        })

        val scrollView = view?.findViewById<android.widget.HorizontalScrollView>(R.id.horizontalScroll)
        val inner = scrollView?.getChildAt(0) as? android.widget.LinearLayout
        inner?.addView(headerContainer, 0)
    }

    private fun showSaveCourseDialog(courseName: String, holes: List<com.jerich06g.Gote.ui.viewmodels.HoleRow>) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Save Course")
            .setMessage("Save \"$courseName\" as a course template for future rounds?")
            .setPositiveButton("Save") { _, _ ->
                viewModel.saveCourse(courseName, holes)
                findNavController().navigate(R.id.historyFragment)
            }
            .setNegativeButton("Not Now") { _, _ ->
                findNavController().navigate(R.id.historyFragment)
            }.show()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}