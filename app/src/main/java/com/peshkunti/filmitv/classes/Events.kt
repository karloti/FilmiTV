package com.peshkunti.filmitv.classes

import com.peshkunti.filmitv.classes.Channel.Companion.toChannel
import com.peshkunti.filmitv.classes.Genre.Companion.toGenre
import it.skrape.core.htmlDocument
import it.skrape.selects.text
import kotlinx.datetime.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class Events {
    private val events: MutableSet<Event> = mutableSetOf<Event>()

    val groupByTitle: Map<String, List<Movie>>
        get() = events.filterIsInstance<Movie>().groupBy { it.title }

    val titleWithEpgs: Map<String, List<Pair<Instant, Channel>>> = groupByTitle
        .keys
        .associateWith { title: String -> groupByTitle[title]!!.map { it.dataTime to it.channel } }


    fun addEvent(movie: Event): Boolean = events.add(movie)

    fun filterMoviesByTitles(title: String): List<Movie> =
        events.filterIsInstance<Movie>().filter { it.title == title }

    fun getEvents(): Set<Event> = events

    @ExperimentalTime
    fun correctMovies() {
        val moviesByTitle: Map<String, List<Movie>> =
            events.filterIsInstance<Movie>().groupBy { it.title }

        val titleWithEpgs: Map<String, List<Pair<Instant, Channel>>> = moviesByTitle
            .keys
            .associateWith { title: String -> moviesByTitle[title]!!.map { it.dataTime to it.channel } }

        titleWithEpgs.keys
            .filter { title ->
                val result: Map<Channel, List<Instant>> = titleWithEpgs[title]!!
                    .groupingBy { it.second }
                    .fold(emptyList<Instant>()) { accumulator, element ->
                        accumulator + element.first
                    }

                result.keys.firstOrNull { ch -> // излъчванията на филм само по един канал
                    val size = result[ch]!!.size
                    if (size < 3)
                        false
                    else {
                        val times: List<Instant> = result[ch]!!.sorted()
                        when {
                            times
                                .zipWithNext { a, b -> b - a }
                                .minOrNull()!! < Duration.hours(5) -> true //най-краткото време между два едни и същи филма по един канал
                            size > 4 && (times.maxOrNull()!! - times.minOrNull()!!)
                                .div(size - 1) < Duration.hours(25) -> true //средната честота на излъчване за целия период по даден канал

                            else -> false
                        }
                    }
                } != null
            }
            .joinToString("\n") { moviesByTitle[it]!!.joinToString("\n") }
            .let(::println)
    }

    @ExperimentalTime
    fun parse(htmlSample: String, tz: TimeZone) {
        htmlDocument(html = htmlSample) {
            val date = findFirst(cssSelector = "#contentHolderLeft > p:nth-child(7)")
                .ownText
                .split(" ")
                .takeLast(3)
                .let {
                    val (day, mount, year) = it
                    LocalDate(
                        year.toInt(),
                        Mount.getEnum(mount)?.ordinal?.inc()
                            ?: throw Exception("Not recognized mount -> $mount"),
                        day.toInt(),
                    )
                }

            findFirst(cssSelector = "#contentHolderLeft > table > tbody") {
                findAll("tr") {
                    map {
                        val (hour, minute) = it
                            .findFirst("td.timeHolder > div")
                            .text
                            .split(":")
                            .map(String::toInt)

                        val dateTime: Instant = date
                            .atTime(hour, minute)
                            .toInstant(tz)

                        val channelTV: Channel = it
                            .findFirst("td.tvLogoHolder > div > a > img")
                            .attributeValues[1]
                            .drop(10).apply {
                                toChannel()
                                    ?: throw Exception("'$this' channel not exist in Enum Channels. Need to add manually!")
                            }
                            .toChannel()!!

                        val presentation: String = it.findFirst("td:nth-child(3)").text

                        val year: Int? = Regex("(?:19|20)\\d{2}").find(presentation)?.value?.toInt()

                        val series: String? =
                            Regex("част.\\d", RegexOption.IGNORE_CASE).find(presentation)?.value

                        val title = presentation
                            .substring(
                                correctRange(
                                    presentation.indexOfFirst { it == '"' },
                                    presentation.indexOfLast { it == '"' },
                                    presentation.length,
                                )
                            )
                            .also {
                                if (it.length < 3) {
                                    println("presentation!! = ${presentation}")
                                    println("it = ${it}")
                                }
                            }
                            .takeWhile { it.isLetterOrDigit() || it in """,:.-" """ }
                            .trim()
                            .ifEmpty {
                                findFirst("td:nth-child(3) > a")
                                    .children
                                    .text
                                    .takeWhile { it.isLetterOrDigit() || it in ": " }
                                    .trim()
                                    .split(" ")
                                    .let {
                                        it.take(1).first() +
                                                " " +
                                                it
                                                    .drop(1)
                                                    .takeWhile { it.toGenre() == null }
                                                    .joinToString(" ")
                                    } + (series?.let { " ${it.takeLast(1)}" } ?: "")
                            }

                        val genres: Set<Genre> = Regex("[а-яА-Я]+")
                            .findAll(presentation)
                            .map { it.value.toGenre() }
                            .filterNotNull()
                            .toSet()

                        val tvSeries: Boolean = when {
                            presentation.contains(other = "Риболовен филм") -> true
                            presentation.contains(other = "Документален филм") && channelTV == Channel.TV1 -> true
                            presentation.contains(other = "сериeен") -> true
                            presentation.contains(other = "сериал") -> true
                            presentation.contains(
                                regex = Regex(
                                    pattern = "/\\s*п\\s*/",
                                    option = RegexOption.IGNORE_CASE,
                                )
                            ) -> true // "/п/"
                            presentation.contains(
                                regex = Regex(
                                    pattern = "\\(\\s*Ep\\.\\s*\\d+\\s*\\)",
                                    option = RegexOption.IGNORE_CASE,
                                )
                            ) -> true  // "(Ep. 3)"
                            else -> false
                        }

                        addEvent(
                            when {
                                tvSeries -> TVSeries(
                                    presentation = presentation,
                                    dataTime = dateTime,
                                    channel = channelTV,
                                )
                                title.isNotEmpty() -> Movie(
                                    title = title,
                                    dataTime = dateTime,
                                    channel = channelTV,
                                    presentation = presentation,
                                    year = year,
                                    genres = genres,
                                )
                                else -> Other(
                                    presentation = presentation,
                                    dataTime = dateTime,
                                    channel = channelTV,
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    private fun correctRange(a: Int, b: Int, length: Int): IntRange = when {
        a == -1 -> 0..length - 1
        b == -1 -> 0..a - 1
        b - a < 2 -> 0..length - 1
        else -> a + 1..b - 1
    }
}

@ExperimentalTime
fun List<Movie>.filterRange(daysPast: Int): List<Movie> = filter {
    it.dataTime in (Clock.System.now() - Duration.days(daysPast)).rangeTo(Clock.System.now())
}


/*
@ExperimentalTime
fun List<Movie>.filterRange(instantFrom: Instant, instantTo: Instant): List<Movie> =
    filter {
                it.dataTime in instantFrom.rangeTo(instantTo)
            }
            .firstOrNull() != null
    }

*/
