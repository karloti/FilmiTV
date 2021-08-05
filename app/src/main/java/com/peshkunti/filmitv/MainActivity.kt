package com.peshkunti.filmitv

import android.content.ContentValues.TAG
import android.content.res.AssetManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.peshkunti.filmitv.classes.Channel
import com.peshkunti.filmitv.classes.Events
import com.peshkunti.filmitv.classes.Movie
import com.peshkunti.filmitv.classes.filterRange
import com.peshkunti.filmitv.ui.theme.FilmiTVTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
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

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
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

@ExperimentalFoundationApi
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
    var buttonEfirni by remember { mutableStateOf(false) }
    var buttonExtend by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { buttonExtend = !buttonExtend },
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = "РАЗШИЕРНИ",
                    modifier = Modifier.padding(4.dp),
                    color = if (buttonExtend)
                        MaterialTheme.colors.background
                    else
                        MaterialTheme.colors.onBackground,
                )
            }
            Button(
                onClick = { buttonEfirni = !buttonEfirni },
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = "ЕФИРНИ",
                    modifier = Modifier.padding(4.dp),
                    color = if (buttonEfirni)
                        MaterialTheme.colors.background
                    else
                        MaterialTheme.colors.onBackground,
                )
            }
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(movies.size) { index ->
                Poster(
                    movie = movies[index],
                    number = index,
                    filterEfirni = buttonEfirni,
                    largeView = buttonExtend
                )
            }
        }
    }
}

@ExperimentalTime
@ExperimentalAnimationApi
@Composable
fun Poster(movie: Movie, number: Int, filterEfirni: Boolean, largeView: Boolean) {
    val typography: Typography = MaterialTheme.typography
    Card {
        AnimatedVisibility(
            visible = !(filterEfirni && movie.hidden),
        ) {
            if (largeView)
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    CardImage(largeView, number)
                    Spacer(modifier = Modifier.height(16.dp))
                    CardText(movie, typography)
                }
            else
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    CardImage(largeView, number)
                    Spacer(modifier = Modifier.width(16.dp))
                    CardText(movie, typography)
                }
        }
    }

}

@Composable
private fun CardImage(
    expanded: Boolean,
    number: Int
) {
    if (expanded)
        Column(
            Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.header),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            )
        }
    else
        Column {
            Image(
                painter = painterResource(id = R.drawable.header),
                contentDescription = null,
                contentScale = ContentScale.Inside,
                alignment = Alignment.TopStart,
                modifier = Modifier
                    .height(82.dp)
                    .animateContentSize()
            )
            Text(
                text = "${number + 1}",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
}

@ExperimentalAnimationApi
@ExperimentalTime
@Composable
private fun CardText(
    movie: Movie,
    typography: Typography
) {
    var isExpanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    text = (Clock.System.now() - movie.dataTime).inWholeDays
        .let {
            when (it) {
                0L -> "днес"
                1L -> "преди $it ден"
                else -> "преди $it дни"
            }
        }
    val localDate by remember {
        mutableStateOf(movie.dataTime.toLocalDateTime(TimeZone.currentSystemDefault())) }

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
            style = MaterialTheme.typography.h6,
            color = if (Clock.System.now() in
                movie.dataTime..movie.dataTime + Duration.hours(3)
            ) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
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
}