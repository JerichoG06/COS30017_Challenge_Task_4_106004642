package com.jerich06g.pokemon_gen6

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // UI references
    private lateinit var recyclerView: RecyclerView
    private lateinit var scrimView: View
    private lateinit var bottomPanel: View
    private lateinit var panelName: TextView
    private lateinit var panelType: TextView
    private lateinit var panelEvo: TextView
    private lateinit var panelLocation: TextView

    // Track whether the bottom panel is showing
    private var isPanelShowing = false

    // Keep reference to current Snackbar to allow dismiss of it early if needed
    private var currentSnackbar: Snackbar? = null

    // SharedPreferences key constants
    companion object {
        const val PREFS_NAME = "PokemonPrefs"
        const val KEY_LAST_CLICKED = "last_clicked_pokemon"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Wire toolbar as action bar to host as the options menu
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Gen 6 Pokédex"

        // Get refrences to all UI views
        recyclerView = findViewById(R.id.recyclerView)
        scrimView = findViewById(R.id.scrimView)
        bottomPanel = findViewById(R.id.bottomPanel)
        panelName = findViewById(R.id.panelName)
        panelType = findViewById(R.id.panelType)
        panelEvo = findViewById(R.id.panelEvo)
        panelLocation = findViewById(R.id.panelLocation)

        // Load Pokemon data from the JSON file
        val pokemonList = loadPokemonFromAssets()

        // Set up RecyclerView with vertical list layout
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PokemonAdapter(
            context = this,
            pokemonList = pokemonList,

            // Short click: show Snackbar + save to SharedPreferences
            onShortClick = { pokemon ->
                saveLastClicked(pokemon.name)
                currentSnackbar = Snackbar.make(
                    findViewById(android.R.id.content),
                    "Type: ${pokemon.type}",
                    Snackbar.LENGTH_SHORT
                )
                currentSnackbar?.show()
            },

            // Long press start: save + show bottom panel with scrim
            onLongPressStart = { pokemon ->
                currentSnackbar?.dismiss() // kill snackbar before panel appears
                saveLastClicked(pokemon.name)
                showBottomPanel(pokemon)
            },

            // Long press end = hide panel
            onLongPressEnd = {
                if (isPanelShowing) hideBottomPanel()
            }
        )
    }

    // Inflate options menu XML into toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handles toolbar menu item taps
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_saved -> {
                // Launch SavedActivity when save icon tapped
                startActivity(Intent(this, SavedActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Read and Parse pokemon_gen6.json
    private fun loadPokemonFromAssets(): List<Pokemon> {
        val list = mutableListOf<Pokemon>()
        try {
            // assets.open() reads the file as a stream, then it's converted to a String
            val jsonString = assets.open("pokemon_gen6.json")
                .bufferedReader()
                .use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            // Loop through each JSON object and map it to a Pokemon data class
            for (i in 0 until jsonArray.length()) {
                val obj: JSONObject = jsonArray.getJSONObject(i)
                list.add(
                    Pokemon(
                        name = obj.getString("name"),
                        pokedex = obj.getInt("pokedex"),
                        type = obj.getString("type"),
                        evoLevel = obj.getString("evoLevel"),
                        location = obj.getString("location"),
                        image = obj.getString("image")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // Saves clicked Pokemon name to SharedPreferences so it survives app restarts
    private fun saveLastClicked(name: String) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_CLICKED, name)
            .apply() // apply() saves in background preventing interruption
    }

    // Populate and animate bottom panel into view
    private fun showBottomPanel(pokemon: Pokemon) {
        isPanelShowing = true

        // Fill panel text fields
        panelName.text = pokemon.name
        panelType.text = "Type: ${pokemon.type}"
        panelEvo.text = "EVO: ${pokemon.evoLevel}"
        panelLocation.text = "Where To Find: ${pokemon.location}"

        // Show views and animate panel sliding up from below screen
        scrimView.visibility = View.VISIBLE
        bottomPanel.visibility = View.VISIBLE
        bottomPanel.translationY = bottomPanel.height.toFloat()
        bottomPanel.animate().translationY(0f).setDuration(250).start()
    }

    // Slides the bottom panel back down and hides the scrim
    private fun hideBottomPanel() {
        isPanelShowing = false
        bottomPanel.animate()
            .translationY(bottomPanel.height.toFloat())
            .setDuration(200)
            .withEndAction {
                // Only hide after animation completes to avoid a visual glitch
                bottomPanel.visibility = View.GONE
                scrimView.visibility = View.GONE
            }
            .start()
    }
}