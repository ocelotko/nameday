package sk.meniny

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SizeF
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.os.BundleCompat
import java.time.LocalDate
import java.time.ZoneId

class NameDayWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { manager.updateAppWidget(it, buildViews(context, manager, it)) }
        scheduleMidnightUpdate(context)
    }

    /** Volá sa pri zmene veľkosti widgetu – prepočíta výšku karty. */
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        manager.updateAppWidget(appWidgetId, buildViews(context, manager, appWidgetId))
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MIDNIGHT,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> updateAll(context)
            else -> super.onReceive(context, intent)
        }
    }

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(midnightIntent(context))
    }

    companion object {
        private const val ACTION_MIDNIGHT = "sk.meniny.action.MIDNIGHT"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, NameDayWidget::class.java))
            if (ids.isEmpty()) return
            NameDayWidget().onUpdate(context, manager, ids)
        }

        private fun buildViews(context: Context, manager: AppWidgetManager, appWidgetId: Int): RemoteViews {
            val today = LocalDate.now()
            val nameDay = NameDays.forDate(today)
            val tomorrow = NameDays.forDate(today.plusDays(1))
            val favorites = Favorites.load(context)

            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

            fun layout(size: SizeF, fitCard: Boolean): RemoteViews {
                val full = size.height >= FULL_LAYOUT_MIN_HEIGHT
                return RemoteViews(context.packageName, if (full) R.layout.widget_name_day else R.layout.widget_name_day_compact).apply {
                    setTextViewText(R.id.widget_title, context.getString(
                        if (nameDay.names.isEmpty()) R.string.widget_title_holiday else R.string.widget_title
                    ))
                    setTextViewText(R.id.widget_names, nameDay.labelWithStars(favorites))
                    if (full) {
                        setTextViewText(R.id.widget_tomorrow, context.getString(R.string.widget_tomorrow, tomorrow.labelWithStars(favorites)))
                    }
                    // Karta nebude vyššia ako široká – 2×2 vyzerá ako štvorec, rovnako ako widgety od Googlu.
                    if (fitCard && size.height > size.width) {
                        setViewLayoutHeight(android.R.id.background, size.width, TypedValue.COMPLEX_UNIT_DIP)
                    }
                    setOnClickPendingIntent(android.R.id.background, openApp)
                }
            }

            // Skutočné veľkosti widgetu na ploche (na výšku aj na šírku), ktoré hlási launcher.
            val sizes = BundleCompat.getParcelableArrayList(
                manager.getAppWidgetOptions(appWidgetId), AppWidgetManager.OPTION_APPWIDGET_SIZES, SizeF::class.java,
            ).orEmpty().distinct()

            if (sizes.isEmpty()) {
                // Launcher veľkosť nehlási – systém vyberie najväčšie rozloženie, ktoré sa zmestí.
                return RemoteViews(mapOf(
                    SizeF(110f, 40f) to layout(SizeF(110f, 40f), fitCard = false),
                    SizeF(110f, FULL_LAYOUT_MIN_HEIGHT) to layout(SizeF(110f, FULL_LAYOUT_MIN_HEIGHT), fitCard = false),
                ))
            }
            return RemoteViews(sizes.associateWith { layout(it, fitCard = true) })
        }

        /** Od tejto výšky (dp) sa zmestí aj riadok „Zajtra". */
        private const val FULL_LAYOUT_MIN_HEIGHT = 100f

        /** Neexaktný alarm tesne po polnoci – nepotrebuje povolenie na presné alarmy. */
        private fun scheduleMidnightUpdate(context: Context) {
            val nextMidnight = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli() + 5_000
            context.getSystemService(AlarmManager::class.java)
                .setAndAllowWhileIdle(AlarmManager.RTC, nextMidnight, midnightIntent(context))
        }

        private fun midnightIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, NameDayWidget::class.java).setAction(ACTION_MIDNIGHT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
