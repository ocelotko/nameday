package sk.meniny

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Obľúbené mená uložené v SharedPreferences (každé meno zvlášť, napr. „Peter"). */
object Favorites {
    private const val PREFS = "favorites"
    private const val KEY_NAMES = "names"

    fun load(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_NAMES, emptySet())!!.toSet()

    fun save(context: Context, names: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_NAMES, names).apply()
    }
}

/** Najbližšie meniny obľúbeného mena. */
data class UpcomingFavorite(val name: String, val date: LocalDate, val daysUntil: Long)

/** Obľúbené mená zoradené podľa toho, kto má meniny najskôr (dnes = 0). */
fun upcomingFavorites(favorites: Set<String>, today: LocalDate): List<UpcomingFavorite> =
    favorites.mapNotNull { name ->
        val nameDay = NameDays.all.firstOrNull { name in it.nameList } ?: return@mapNotNull null
        // Najbližší rok, v ktorom ten deň existuje a ešte nebol (29. február len v priestupnom roku).
        val date = generateSequence(today.year) { it + 1 }
            .filter { nameDay.date.isValidYear(it) }
            .map { nameDay.date.atYear(it) }
            .first { !it.isBefore(today) }
        UpcomingFavorite(name, date, ChronoUnit.DAYS.between(today, date))
    }.sortedWith(compareBy({ it.daysUntil }, { it.name }))

/** Mená dňa, obľúbené označené hviezdičkou – pre widget. */
fun NameDay.labelWithStars(favorites: Set<String>): String =
    if (names.isEmpty()) label
    else nameList.joinToString(", ") { if (it in favorites) "★ $it" else it }
