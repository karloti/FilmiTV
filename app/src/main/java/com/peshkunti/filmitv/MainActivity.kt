package com.peshkunti.filmitv

import android.content.ContentValues.TAG
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.InternalAnimationApi
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peshkunti.filmitv.FilterState.*
import com.peshkunti.filmitv.ViewState.*
import com.peshkunti.filmitv.classes.Channel
import com.peshkunti.filmitv.classes.Events
import com.peshkunti.filmitv.classes.Movie
import com.peshkunti.filmitv.classes.filterRange
import com.peshkunti.filmitv.ui.theme.FilmiTVTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

val EVENTS = Events()
val MOUNTS = listOf<String>(
    "НЯМА",
    "Януари",
    "Февруари",
    "Март",
    "Април",
    "Май",
    "Юни",
    "Юли",
    "Август",
    "Септември",
    "Октомври",
    "Ноември",
    "Декември",
)

private enum class ViewState {
    Large,
    Small,
}

private enum class FilterState {
    NoFilter,
    Efirni,
}


@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    @InternalAnimationApi
    @ExperimentalFoundationApi
    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmiTVTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val assetManager: AssetManager = assets
                    start(assetManager)
                    MovieContent()
                }
            }
        }
    }
}

fun readHtmlAsset(assetManager: AssetManager, fileName: String): Result<String> =
    runCatching { assetManager.open(fileName).bufferedReader().use { it.readText() } }

@ExperimentalAnimationApi
@ExperimentalTime
fun start(assetManager: AssetManager) {
    val tz: TimeZone = TimeZone.currentSystemDefault() // "Europe/Helsinki"
    assetManager.list("")?.filter { it.contains(".html") }?.forEach {
        readHtmlAsset(assetManager, it).onSuccess { EVENTS.parse(it, tz) }
        Log.d(TAG, "asset: $it")
    }
}

@Suppress("UNCHECKED_CAST")
@ExperimentalTime
@ExperimentalAnimationApi
@Composable
fun MovieContent() {
    EVENTS.correctMovies()
    val movies = EVENTS
        .getEvents()
        .filterIsInstance<Movie>()
        .filterRange(7)
        .sortedByDescending { it.dataTime }
        .distinctBy { it.title }
        .onEach { movie ->
            movie.hidden =
                movie.channel !in setOf(
                    Channel.BNT,
                    Channel.BNT_2,
                    Channel.BNT_3,
                    Channel.BNT_4,
                    Channel.BTV,
                    Channel.NOVA_TV
                )
        }
    var viewState by remember { mutableStateOf(Small) }
    var filterState by remember { mutableStateOf(NoFilter) }

//    val transitionViewState = updateTransition(targetState = viewState, label = "")
//    val transitionFilterState = updateTransition(targetState = filterState, label = "")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    viewState = when (viewState) {
                        Large -> Small
                        Small -> Large
                    }
                },
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = "РАЗШИЕРНИ",
                    modifier = Modifier.padding(4.dp),
                    color = when (viewState) {
                        Large -> MaterialTheme.colors.onBackground
                        Small -> MaterialTheme.colors.background
                    }
                )
            }
            Button(
                onClick = {
                    filterState = when (filterState) {
                        NoFilter -> Efirni
                        Efirni -> NoFilter
                    }
                },
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = "ЕФИРНИ",
                    modifier = Modifier.padding(4.dp),
                    color = when (filterState) {
                        NoFilter -> MaterialTheme.colors.background
                        Efirni -> MaterialTheme.colors.onBackground
                    }
                )
            }
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            reverseLayout = false,

            ) {
            items(movies.size) { index ->
                Poster(
                    movie = movies[index],
                    number = index,
                    filterState = filterState,
                    viewState = viewState
                )
            }
        }
    }
}

@ExperimentalTime
@ExperimentalAnimationApi
@Composable
private fun Poster(movie: Movie, number: Int, filterState: FilterState, viewState: ViewState) {
//    val transition = updateTransition(targetState = viewState, label = "")
    var isExpanded by remember {
        mutableStateOf(false)
    }
    isExpanded = when (viewState) {
        Large -> true
        Small -> false
    }

    var text by remember { mutableStateOf("") }

    AnimatedVisibility(
        modifier = Modifier.padding(4.dp),
        visible = when (filterState) {
            NoFilter -> true
            Efirni -> !movie.hidden
        },
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .animateContentSize(
                    animationSpec = snap(0),
                    finishedListener = { a, b -> Log.d(TAG, "Poster: ${a.height} ${b.height} ") }
                ),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
            elevation = 1.dp,
            border = BorderStroke(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                width = 1.dp
            ),

            ) {
            AnimatedContent(
                targetState = viewState,
                contentAlignment = Alignment.TopStart,
            ) { state ->
                Image(
                    modifier = when (state) {
                        Large -> Modifier.fillMaxWidth()
                        Small -> Modifier.width(62.dp)
                    },
                    painter = painterResource(id = R.drawable.header),
                    contentDescription = null,
                    contentScale = when (state) {
                        Large -> ContentScale.FillWidth
                        Small -> ContentScale.Inside
                    },
                    alignment = when (viewState) {
                        Large -> Alignment.TopCenter
                        Small -> Alignment.BottomCenter
                    },
                )
            }

            Text(
                text = "${number + 1}",
                style = MaterialTheme.typography.body2,
            )
            Box(
                modifier = Modifier
                    .run {
                        when (viewState) {
                            Large -> padding(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp,
                                top = 540.dp
                            )
                            Small -> padding(
                                start = 72.dp,
                                end = 8.dp,
                                bottom = 8.dp,
                            )
                        }
                    },
                contentAlignment = Alignment.BottomEnd,
            )
            {
                text = (Clock.System.now() - movie.dataTime).inWholeDays
                    .let {
                        when (it) {
                            0L -> "днес"
                            1L -> "вчера"
                            else -> "преди $it дни"
                        }
                    }
                val localDate by remember {
                    mutableStateOf(movie.dataTime.toLocalDateTime(TimeZone.currentSystemDefault()))
                }
                val dateTime by remember {
                    mutableStateOf(localDate.date.run { "$dayOfMonth ${MOUNTS[monthNumber]} $year" } + " от " +
                            localDate.toString().substringAfter("T"))
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = movie.title,
                        style = typography.h6,
                        color = if (Clock.System.now() in
                            movie.dataTime..movie.dataTime + Duration.hours(3)
                        ) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()

                    )
                    Text(
                        "жанрове: " + movie.genres?.joinToString { it.names.toList()[1] },
                        style = typography.body2
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("година: " + movie.year, style = typography.body2)
                        Text(
                            text = text,
                            style = typography.body2,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = movie.channel.toString(),
                            style = typography.body2,
                        )
                        Text(
                            text = dateTime,
                            style = typography.body2,
                        )
                    }
                    AnimatedVisibility(visible = isExpanded) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Text("информация: " + movie.presentation, style = typography.body2)
                        }
                    }
                }
            }
        }
    }
}

/*
@ExperimentalFoundationApi
@OptIn(ExperimentalAnimationApi::class)
@ExperimentalTime
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewPoster() {
    FilmiTVTheme {
        Column() {
            MovieContent()
            Poster(
                movie = Movie("Karlo", Clock.System.now(), Channel.FOX_CHANNEL),
                number = 1,
                filterEfirni = false,
                largeView = false
            )
            Poster(
                movie = Movie(
                    "Karlo2",
                    Clock.System.now() + Duration.Companion.days(2),
                    Channel.FOX_CHANNEL
                ),
                number = 50,
                filterEfirni = false,
                largeView = false
            )
        }
    }
}*/
