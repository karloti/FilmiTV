package com.peshkunti.filmitv.classes

import kotlinx.datetime.Instant

sealed interface Event : Comparable<Event> {
    val presentation: String
    var fresh: Boolean
    val dataTime: Instant
    val channel: Channel
    var hidden: Boolean

    override fun compareTo(other: Event) = when (this) {
        is Movie -> when (other) {
            is Movie -> title.compareTo(other.title) // Movie with Movie comparison
            else -> title.compareTo(other.presentation) // Movie with any other Event except Movie
        }
        else -> presentation.compareTo(other.presentation) // Event with any other Event
    }
}

data class Movie(
    val title: String,
    override val dataTime: Instant,
    override val channel: Channel,
    override val presentation: String = "",
    var year: Int? = null,
    var genres: Set<Genre>? = null,
    var href: String? = null,
    override var fresh: Boolean = true,
    override var hidden: Boolean = false,
) : Comparable<Event>, Event

data class TVSeries(
    override val presentation: String,
    override val dataTime: Instant,
    override val channel: Channel,
    override var fresh: Boolean = true,
    override var hidden: Boolean = false,
) : Event

data class Other(
    override val presentation: String,
    override val dataTime: Instant,
    override val channel: Channel,
    override var fresh: Boolean = true,
    override var hidden: Boolean = false,
) : Event