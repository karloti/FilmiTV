package com.peshkunti.filmitv.classes

import junit.framework.TestCase
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class MovieTest : TestCase() {
    val event: Movie = Movie(
        title = "Peshkunti e pich",
        presentation = "Ala bala",
        year = 2020,
        genres = setOf(Genre.ACTION, Genre.ADVENTURE),
        href = "",
        dataTime = Instant.parse("2021-07-19T05:00:00Z"),
        channel = Channel.DISNEY_CHANNEL,
        fresh = false,
    )

    val event1 = Movie(title = "Karlo", dataTime = Clock.System.now(), Channel.FOX_CHANNEL)

    init {
        println(event)
    }
}