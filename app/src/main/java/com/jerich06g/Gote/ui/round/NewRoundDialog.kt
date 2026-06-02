package com.jerich06g.Gote.ui.round

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jerich06g.Gote.R
import com.jerich06g.Gote.data.AppDatabase
import com.jerich06g.Gote.data.entities.SavedHole
import com.jerich06g.Gote.data.repository.GoteRepository
import kotlinx.coroutines.launch

class NewRoundDialog : DialogFragment() {
    // Callback to pass course name + players back to HomeFragment
    var onRoundStarted: ((courseName: String, players: List<String>, preloadedHoles: List<SavedHole>) -> Unit)? = null

    private lateinit var repository: GoteRepository
    private val playerFields = mutableListOf<TextInputLayout>()
    private var preloadedHoles: List<SavedHole> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val db = AppDatabase.getDatabase(requireContext())
        repository = GoteRepository(
            db.roundDao(), db.holeDao(), db.playerDao(), db.scoreDao(), db.savedCourseDao()
        )
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_round, null)

        val etCourseName = view.findViewById<TextInputEditText>(R.id.etCourseName)
        val tvCourseMatch = view.findViewById<TextView>(R.id.tvCourseMatch)
        val playerContainer = view.findViewById<LinearLayout>(R.id.playerFieldsContainer)

        // Add first player field by default
        addPlayerField(playerContainer)

        // TextWatcher for saved course lookup
        etCourseName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    lifecycleScope.launch {
                        val matches = repository.searchCourses(query)
                        if (matches.isNotEmpty()) {
                            val match = matches.first()
                            tvCourseMatch.visibility = View.VISIBLE
                            tvCourseMatch.text =
                                "Load saved course: ${match.courseName}? Tap to load."
                            tvCourseMatch.setOnClickListener {
                                lifecycleScope.launch {
                                    preloadedHoles =
                                        repository.getHolesForCourse(match.savedCourseId)
                                    tvCourseMatch.text =
                                        "✓ ${match.courseName} loaded (${preloadedHoles.size} holes"
                                    tvCourseMatch.setOnClickListener(null)
                                }
                            }
                        } else {
                            tvCourseMatch.visibility = View.GONE
                            preloadedHoles = emptyList()
                        }
                    }
                } else {
                    tvCourseMatch.visibility = View.GONE
                    preloadedHoles = emptyList()
                }
            }
        })

        // Add player button
        view.findViewById<Button>(R.id.btnAddPlayer).setOnClickListener {
            addPlayerField(playerContainer)
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
        view.findViewById<Button>(R.id.btnStartRound).setOnClickListener {
            if (validateAndStart(etCourseName, playerContainer, dialog)) {
                // handled inside validateAndStart
            }
        }
        return dialog
    }
    private fun addPlayerField(container: LinearLayout) {
        val til = TextInputLayout(requireContext()).apply {
            hint = "Player ${playerFields.size + 1} Initials"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }
        val et = TextInputEditText(til.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            inputType = android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            filters = arrayOf(android.text.InputFilter.LengthFilter(4))
        }
        til.addView(et)
        container.addView(til)
        playerFields.add(til)
    }
    private fun validateAndStart(
        etCourseName: TextInputEditText,
        playerContainer: LinearLayout,
        dialog: AlertDialog
    ): Boolean {
        var valid = true

        // Validate course name
        val courseName = etCourseName.text?.toString()?.trim() ?: ""
        val tilCourse = etCourseName.parent.parent as? TextInputLayout
        if (courseName.isEmpty()) {
            tilCourse?.error = "Course name required"
            valid = false
        } else {
            tilCourse?.error = null
        }

        // Validate players
        val playerInitials = mutableListOf<String>()
        val seen = mutableListOf<String>()
        for (til in playerFields) {
            val et = til.editText
            val initials = et?.text?.toString()?.trim()?.uppercase() ?: ""
            if (initials.isEmpty()) {
                til.error = "Required"
                valid = false
            } else if (seen.contains(initials)) {
                til.error = "Duplicate"
                valid = false
            } else {
                til.error = null
                seen.add(initials)
                playerInitials.add(initials)
            }
        }

        if (playerInitials.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least on player", Toast.LENGTH_SHORT).show()
            valid = false
        }

        if (valid) {
            onRoundStarted?.invoke(courseName, playerInitials, preloadedHoles)
            dismiss()
        }

        return valid
    }
}