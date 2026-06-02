package com.jerich06g.Gote.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jerich06g.Gote.R
import com.jerich06g.Gote.ui.round.NewRoundDialog

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnStartRound).setOnClickListener {
            val dialog = NewRoundDialog()
            dialog.onRoundStarted = { courseName, players, preloadedHoles ->
                val args = Bundle().apply {
                    putString("courseName", courseName)
                    putStringArrayList("playerInitials", ArrayList(players))
                    putIntegerArrayList("preloadedPars", ArrayList(preloadedHoles.map { it.par }))
                    putIntegerArrayList("preloadedDistances", ArrayList(preloadedHoles.map { it.distance }))
                }
                findNavController().navigate(R.id.action_home_to_activeRound, args)
            }
            dialog.show(parentFragmentManager, "NewRoundDialog")
        }

        view.findViewById<Button>(R.id.btnHistory).setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        view.findViewById<Button>(R.id.btnFavourites).setOnClickListener {
            findNavController().navigate(R.id.favouritesFragment)
        }
    }
}