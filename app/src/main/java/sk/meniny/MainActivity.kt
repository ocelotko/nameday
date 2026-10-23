package sk.meniny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MeninyTheme { NameDaysScreen() } }
    }

    override fun onResume() {
        super.onResume()
        // Pre istotu obnoví widget, keby ho systém nestihol aktualizovať o polnoci.
        NameDayWidget.updateAll(this)
    }
}

@Composable
private fun MeninyTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    MaterialTheme(colorScheme = colors, content = content)
}

/** Položky zoznamu: hlavička mesiaca alebo jeden deň. */
private sealed interface Entry {
    data class Header(val title: String) : Entry
    data class Day(val nameDay: NameDay) : Entry
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NameDaysScreen() {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val todayMonthDay = MonthDay.from(today)
    val rows = remember {
        NameDays.all.groupBy { it.date.month }.flatMap { (month, days) ->
            listOf(Entry.Header(formatMonth(today.withMonth(month.value).withDayOfMonth(1)))) + days.map { Entry.Day(it) }
        }
    }
    // Pár riadkov nad dneškom, aby bol zvýraznený deň viditeľný s kontextom.
    val todayIndex = rows.indexOfFirst { it is Entry.Day && it.nameDay.date == todayMonthDay }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (todayIndex - 2).coerceAtLeast(0))
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val nameDay = NameDays.forDate(today)

    var favorites by remember { mutableStateOf(Favorites.load(context)) }
    var showFavorites by remember { mutableStateOf(false) }
    // Deň s viacerými menami – v dialógu si vyberieš, ktoré meno označiť.
    var picking by remember { mutableStateOf<NameDay?>(null) }

    fun toggle(name: String) {
        val added = name !in favorites
        favorites = if (added) favorites + name else favorites - name
        Favorites.save(context, favorites)
        NameDayWidget.updateAll(context)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(if (added) "★ $name pridané medzi obľúbené" else "$name odobraté z obľúbených")
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(nameDay.label, fontWeight = FontWeight.Bold)
                        Text(
                            "Dnes · ${formatLongDate(today)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFavorites = !showFavorites }) {
                        Text(
                            if (showFavorites) "★" else "☆",
                            fontSize = 24.sp,
                            color = if (showFavorites) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (!showFavorites && !listState.isTodayVisible(todayIndex)) {
                ExtendedFloatingActionButton(onClick = {
                    scope.launch { listState.animateScrollToItem((todayIndex - 2).coerceAtLeast(0)) }
                }) { Text("Dnes") }
            }
        },
    ) { padding ->
        val contentPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + 88.dp,
        )
        if (showFavorites) {
            FavoritesList(upcomingFavorites(favorites, today), contentPadding, onRemove = ::toggle)
        } else {
            LazyColumn(state = listState, contentPadding = contentPadding, modifier = Modifier.fillMaxSize()) {
                items(rows.size, key = { i -> rows[i].key() }) { i ->
                    when (val row = rows[i]) {
                        is Entry.Header -> MonthHeader(row.title)
                        is Entry.Day -> DayRow(
                            row.nameDay, today.year,
                            isToday = row.nameDay.date == todayMonthDay,
                            favorites = favorites,
                            onLongPress = {
                                val names = row.nameDay.nameList
                                when (names.size) {
                                    0 -> Unit
                                    1 -> toggle(names.single())
                                    else -> picking = row.nameDay
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    picking?.let { day ->
        PickNamesDialog(day, favorites, onToggle = ::toggle, onDismiss = { picking = null })
    }
}

private fun Entry.key(): String = when (this) {
    is Entry.Header -> "h-$title"
    is Entry.Day -> "d-${nameDay.date}"
}

private fun LazyListState.isTodayVisible(index: Int): Boolean =
    layoutInfo.visibleItemsInfo.any { it.index == index }

@Composable
private fun MonthHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayRow(
    nameDay: NameDay,
    year: Int,
    isToday: Boolean,
    favorites: Set<String>,
    onLongPress: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val date = if (nameDay.date.isValidYear(year)) nameDay.date.atYear(year) else null
    val hasFavorite = nameDay.nameList.any { it in favorites }
    val favoriteColor = if (isToday) Color.Unspecified else colors.tertiary
    Surface(
        color = when {
            isToday -> colors.primaryContainer
            hasFavorite -> colors.tertiaryContainer.copy(alpha = 0.35f)
            else -> colors.surface
        },
        contentColor = if (isToday) colors.onPrimaryContainer else colors.onSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = if (isToday || hasFavorite) 2.dp else 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                )
                .padding(horizontal = 12.dp, vertical = if (isToday) 16.dp else 10.dp),
        ) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(40.dp)) {
                Text(
                    "${nameDay.date.dayOfMonth}.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                    date?.let { formatShortWeekday(it) } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) colors.onPrimaryContainer else colors.onSurfaceVariant,
                )
            }
            Column {
                Text(
                    if (nameDay.names.isEmpty()) buildAnnotatedString { append(nameDay.label) }
                    else buildAnnotatedString {
                        nameDay.nameList.forEachIndexed { i, name ->
                            if (i > 0) append(", ")
                            if (name in favorites) {
                                withStyle(SpanStyle(color = favoriteColor, fontWeight = FontWeight.Bold)) { append("★ $name") }
                            } else {
                                append(name)
                            }
                        }
                    },
                    style = if (isToday) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (nameDay.names.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                )
                if (isToday) {
                    Text("Dnes má meniny", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PickNamesDialog(
    nameDay: NameDay,
    favorites: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Hotovo") } },
        title = { Text("Obľúbené mená") },
        text = {
            Column {
                nameDay.nameList.forEach { name ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(name) },
                    ) {
                        Checkbox(checked = name in favorites, onCheckedChange = { onToggle(name) })
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}

private val favoriteDate = DateTimeFormatter.ofPattern("EEEE d. MMMM", SLOVAK)

@Composable
private fun FavoritesList(
    upcoming: List<UpcomingFavorite>,
    contentPadding: PaddingValues,
    onRemove: (String) -> Unit,
) {
    if (upcoming.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(contentPadding).padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "Zatiaľ nemáš žiadne obľúbené mená.\n\nV kalendári podrž prst na mene a pridá sa sem.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    val colors = MaterialTheme.colorScheme
    LazyColumn(contentPadding = contentPadding, modifier = Modifier.fillMaxSize()) {
        items(upcoming, key = { it.name }) { fav ->
            val isToday = fav.daysUntil == 0L
            Surface(
                color = if (isToday) colors.primaryContainer else colors.surface,
                contentColor = if (isToday) colors.onPrimaryContainer else colors.onSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 20.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(fav.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            favoriteDate.format(fav.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isToday) colors.onPrimaryContainer else colors.onSurfaceVariant,
                        )
                    }
                    Text(
                        inDays(fav.daysUntil),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isToday) colors.onPrimaryContainer else colors.tertiary,
                    )
                    IconButton(onClick = { onRemove(fav.name) }) {
                        Text("★", fontSize = 22.sp, color = if (isToday) colors.onPrimaryContainer else colors.tertiary)
                    }
                }
            }
        }
    }
}

/** „dnes", „zajtra", „o 3 dni", „o 12 dní" */
private fun inDays(days: Long): String = when (days) {
    0L -> "dnes"
    1L -> "zajtra"
    in 2L..4L -> "o $days dni"
    else -> "o $days dní"
}
