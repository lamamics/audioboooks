package com.lamamics.audioboooks

import android.content.Context
import org.json.JSONObject

/** État persistant d'un livre (position, note, favori, terminé). */
data class BookState(
    val chapterIndex: Int = 0,
    val positionMs: Long = 0L,
    val finished: Boolean = false,
    val rating: Int = 0,
    val favorite: Boolean = false,
)

/** Persistance simple via SharedPreferences. */
object Store {

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("audioboooks", Context.MODE_PRIVATE)

    fun rootUri(ctx: Context): String? = prefs(ctx).getString("rootUri", null)

    fun setRootUri(ctx: Context, uri: String) {
        prefs(ctx).edit().putString("rootUri", uri).apply()
    }

    /** Secondes de retour en arrière à la reprise après pause (paramétrable). */
    fun rewindSeconds(ctx: Context): Int = prefs(ctx).getInt("rewindSeconds", 5)

    fun setRewindSeconds(ctx: Context, seconds: Int) {
        prefs(ctx).edit().putInt("rewindSeconds", seconds).apply()
    }

    fun speed(ctx: Context): Float = prefs(ctx).getFloat("speed", 1f)

    fun setSpeed(ctx: Context, speed: Float) {
        prefs(ctx).edit().putFloat("speed", speed).apply()
    }

    fun currentBookId(ctx: Context): String? = prefs(ctx).getString("currentBookId", null)

    fun setCurrentBookId(ctx: Context, id: String) {
        prefs(ctx).edit().putString("currentBookId", id).apply()
    }

    fun getBookState(ctx: Context, id: String): BookState {
        val raw = prefs(ctx).getString("book:$id", null) ?: return BookState()
        return try {
            val o = JSONObject(raw)
            BookState(
                chapterIndex = o.optInt("chapterIndex", 0),
                positionMs = o.optLong("positionMs", 0L),
                finished = o.optBoolean("finished", false),
                rating = o.optInt("rating", 0),
                favorite = o.optBoolean("favorite", false),
            )
        } catch (e: Exception) {
            BookState()
        }
    }

    fun saveBookState(ctx: Context, id: String, state: BookState) {
        val o = JSONObject()
            .put("chapterIndex", state.chapterIndex)
            .put("positionMs", state.positionMs)
            .put("finished", state.finished)
            .put("rating", state.rating)
            .put("favorite", state.favorite)
        prefs(ctx).edit().putString("book:$id", o.toString()).apply()
    }
}
