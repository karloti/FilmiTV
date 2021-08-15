package com.peshkunti.filmitv.dao

import com.peshkunti.filmitv.dto.Channel
import com.peshkunti.filmitv.dto.Event
import com.peshkunti.filmitv.dto.Movie
import kotlinx.datetime.Instant

interface IEvents {
    val events: MutableSet<Event>
}