package io.github.designtools.widget

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate

data class DesignTool(
    val name: String,
    val url: String,
    val category: String,
    val description: String,
)

/** Loads the bundled tool list (generated from the repo README) once and caches it. */
object ToolRepository {
    @Volatile
    private var cache: List<DesignTool>? = null

    fun tools(context: Context): List<DesignTool> = cache ?: synchronized(this) {
        cache ?: load(context).also { cache = it }
    }

    private fun load(context: Context): List<DesignTool> {
        val json = context.assets.open("tools.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            DesignTool(
                name = o.getString("name"),
                url = o.getString("url"),
                category = o.getString("category"),
                description = o.optString("description"),
            )
        }
    }

    /** Same tool for everyone on a given day, shuffled deterministically across days. */
    fun indexForDay(day: LocalDate, size: Int): Int {
        val seed = day.toEpochDay() * 2654435761L
        return Math.floorMod(seed xor (seed ushr 16), size.toLong()).toInt()
    }
}
