package com.peshkunti.filmitv

import android.content.ContentValues.TAG
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.InternalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.peshkunti.filmitv.FilterState.Efirni
import com.peshkunti.filmitv.FilterState.NoFilter
import com.peshkunti.filmitv.ViewState.Large
import com.peshkunti.filmitv.ViewState.Small
import com.peshkunti.filmitv.dto.Channel
import com.peshkunti.filmitv.dto.Events
import com.peshkunti.filmitv.dto.Movie
import com.peshkunti.filmitv.dto.filterRange
import com.peshkunti.filmitv.ui.theme.FilmiTVTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

val EVENTS = Events()
val MOUNTS = listOf(
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

/*
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val flow = getFirebaseInstance().collection("testdata")
        .asFlow()

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
*/

@ExperimentalAnimationApi
class MainActivity : ComponentActivity(), CoroutineScope by MainScope() {
//    private val flow = getFirebaseInstance().collection("testdata").asFlow()
    val db = Firebase.firestore

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
        readHtmlAsset(assetManager, it).onSuccess { s: String -> EVENTS.parse(s, tz) }
        Log.d(TAG, "asset: $it")
    }
}

@ExperimentalFoundationApi
@Suppress("UNCHECKED_CAST")
@ExperimentalTime
@ExperimentalAnimationApi
@Composable
fun MovieContent() {
    EVENTS.correctMovies()
    val movies = EVENTS
        .events
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
        .groupBy { (Clock.System.now() - it.dataTime).inWholeDays }
    var viewState by remember { mutableStateOf(Small) }
    var filterState by remember { mutableStateOf(NoFilter) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.primaryVariant),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    viewState = when (viewState) {
                        Large -> Small
                        Small -> Large
                    }
                },
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
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            movies.forEach { (passedDays: Long, groupedMoviesByDays: List<Movie>) ->
                stickyHeader {
                    Surface(
                        color = MaterialTheme.colors.surface,
                        elevation = 2.dp
                    ) {
                        Text(
                            text = passedDays.let {
                                when (it) {
                                    0L -> "днес"
                                    1L -> "вчера"
                                    else -> "преди $it дни"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.body2,
                        )
                    }
                }
                items(groupedMoviesByDays.size) { index ->
                    Poster(
                        movie = groupedMoviesByDays[index],
                        number = index,
                        filterState = filterState,
                        viewState = viewState
                    )
                }
            }
        }
    }
}

@ExperimentalTime
@ExperimentalAnimationApi
@Composable
private fun Poster(movie: Movie, number: Int, filterState: FilterState, viewState: ViewState) {
    var isExpanded by remember {
        mutableStateOf(
            when (viewState) {
                Large -> true
                Small -> false
            }
        )
    }
    val localDate by remember {
        mutableStateOf(movie.dataTime.toLocalDateTime(TimeZone.currentSystemDefault()))
    }
    val dateTime by remember {
        mutableStateOf(localDate.date.run { "$dayOfMonth ${MOUNTS[monthNumber]} $year" } + " от " +
                localDate.toString().substringAfter("T"))
    }
    val surfaceColor: Color by animateColorAsState(
        if (isExpanded) MaterialTheme.colors.onBackground.copy(alpha = 0.1f) else MaterialTheme.colors.surface,
    )

    AnimatedVisibility(
        visible = when (filterState) {
            NoFilter -> true
            Efirni -> !movie.hidden
        },
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        AnimatedContent(targetState = viewState) { state ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = surfaceColor,
                modifier = Modifier.animateContentSize()
            ) {
                Image(
                    modifier = when (state) {
                        Large -> Modifier.fillMaxWidth()
                        Small -> Modifier.height(83.dp)
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

                Text(
                    text = "${number + 1}",
                    style = MaterialTheme.typography.body2,
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .run {
                            when (viewState) {
                                Large -> padding(
                                    start = 8.dp,
                                    end = 8.dp,
                                    bottom = 8.dp,
                                    top = 540.dp
                                )
                                Small -> padding(
                                    start = 64.dp,
                                )
                            }
                        },
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.h6,
                        color = if (Clock.System.now() in
                            movie.dataTime..movie.dataTime + Duration.hours(2)
                        )
                            MaterialTheme.colors.secondary
                        else
                            MaterialTheme.colors.primary,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,

                        )
                    Text(
                        text = "" + movie.genres?.joinToString { it.names.toList()[1] },
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        style = MaterialTheme.typography.body2
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = movie.year?.let { "година: $it" } ?: "",
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = movie.channel.toString(),
                            style = MaterialTheme.typography.body2,
                        )
                        Text(
                            text = dateTime,
                            style = MaterialTheme.typography.body2,
                        )
                    }
                    AnimatedVisibility(visible = isExpanded) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Text(
                                "информация: " + movie.presentation,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
            }
        }
    }
}