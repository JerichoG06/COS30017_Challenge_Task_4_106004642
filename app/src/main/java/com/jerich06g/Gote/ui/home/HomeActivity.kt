package com.jerich06g.Gote.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jerich06g.Gote.R
import org.json.JSONArray
import org.json.JSONObject


class HomeActivity : AppCompatActivity() {

    // UI references
    private lateinit var recyclerView: RecyclerView

    // Prevent Snackbar from overlapping sheet
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

        // Load Pokemon data from the JSON file
        val pokemonList = loadPokemonFromAssets()

        // Set up RecyclerView with vertical list layout
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Keep reference to sheet to all dismiss on finger release
        var currentSheet : PokemonBottomSheet? = null

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
                currentSheet = PokemonBottomSheet.newInstance(pokemon)
                // show() displays modal bottom sheet over current Activity
                currentSheet?.show(supportFragmentManager, "pokemon_sheet")
            },

            // Dismiss sheet on finger release
            onLongPressEnd = {
                currentSheet?.dismiss()
                currentSheet = null
            }
        )
    }

    // Inflate options menu XML into toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
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
}