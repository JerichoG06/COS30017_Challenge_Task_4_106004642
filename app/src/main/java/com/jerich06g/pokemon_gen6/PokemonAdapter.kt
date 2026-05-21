package com.jerich06g.pokemon_gen6

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class PokemonAdapter(
    private val context: Context,
    private val pokemonList: List<Pokemon>,
    // Lambda callbacks so MainActivity can handle the click logic, not the adapter
    private val onShortClick: (Pokemon) -> Unit,
    private val onLongPressStart: (Pokemon) -> Unit,
    private val onLongPressEnd: () -> Unit
) : RecyclerView.Adapter<PokemonAdapter.ViewHolder>() {

    // ViewHodler caches the views for each row so findViewById isn't repeatedly called
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.pokemonImage)
        val name: TextView = view.findViewById(R.id.pokemonName)
        val dex: TextView = view.findViewById(R.id.pokemonDex)
        val cardRoot: View = view.findViewById(R.id.cardRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the item_pokemon layout for each row
        val view = LayoutInflater.from(context).inflate(R.layout.item_pokemon, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        // Fill in text fields
        holder.name.text = pokemon.name
        holder.dex.text = "#${String.format("%03d", pokemon.pokedex)}" //The Pokedex No. e.g. #650

        // Load PNG from assets using Glide
        Glide.with(context)
            .load("file:///android_asset/${pokemon.image}")
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.image)

        // Apply colour based on primary type (First type if dual)
        val primaryType = pokemon.type.split("/")[0].trim()
        holder.cardRoot.setBackgroundColor(getTypeColor(primaryType))

        // Track if long press was started to block short click on release
        var longPressTriggered = false

        // Short press -> show Snackbar
        holder.itemView.setOnClickListener {
            // Only fires if wasn't long click
            if (!longPressTriggered) {
                onShortClick(pokemon)
            }
            longPressTriggered = false // reset after every click
        }

        // Long press -> show bottom panel while held down
        holder.itemView.setOnLongClickListener {
            longPressTriggered = true
            onLongPressStart(pokemon)
            true // consumes the event so short click doesn't also fire
        }

        // Detect finger lift after long press
        holder.itemView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP ||
                event.action == MotionEvent.ACTION_CANCEL) {
                onLongPressEnd()
                // Reset after a short delay to let click listener check it first
                holder.itemView.postDelayed({ longPressTriggered = false }, 300)
            }
            false // false so normal clicks still work
        }
    }

    override fun getItemCount() = pokemonList.size

    // Maps type string to corresponding colour from colors.xml
    private fun getTypeColor(type: String): Int {
        val colorRes = when (type.lowercase()) {
            "normal"   -> R.color.type_normal
            "fire"     -> R.color.type_fire
            "water"    -> R.color.type_water
            "grass"    -> R.color.type_grass
            "electric" -> R.color.type_electric
            "ice"      -> R.color.type_ice
            "fighting" -> R.color.type_fighting
            "poison"   -> R.color.type_poison
            "ground"   -> R.color.type_ground
            "flying"   -> R.color.type_flying
            "psychic"  -> R.color.type_psychic
            "bug"      -> R.color.type_bug
            "rock"     -> R.color.type_rock
            "ghost"    -> R.color.type_ghost
            "dragon"   -> R.color.type_dragon
            "dark"     -> R.color.type_dark
            "steel"    -> R.color.type_steel
            "fairy"    -> R.color.type_fairy
            else       -> R.color.type_unknown
        }
        return context.getColor(colorRes)
    }
}