package com.peshkunti.filmitv.dto

enum class Genre(val names: Set<String>) {
    ACTION(setOf("action", "екшън")),
    ADULT(setOf("adult", "порнографски", "еротичен")),
    ADVENTURE(setOf("adventure", "приключенски")),
    ANIMATION(setOf("animation", "анимация", "анимационен")),
    BIOGRAPHY(setOf("biography", "биография", "биографичен", "биографична")),
    COMEDY(setOf("comedy", "комедия")),
    CRIME(setOf("crime", "престъпление", "криминален")),
    DOCUMENTARY(setOf("documentary", "документален")),
    DRAMA(setOf("drama", "драма")),
    FAMILY(setOf("family", "семеен")),
    FANTASY(setOf("fantasy", "фентъзи")),
    FILM_NOIR(setOf("film-noir")),
    GAME_SHOW(setOf("game-show", "гейм-шоу")),
    HISTORY(setOf("history", "исторически")),
    HORROR(setOf("horror", "ужаси", "хорър")),
    MUSICAL(setOf("musical", "музикален")),
    MUSIC(setOf("music", "музика")),
    MYSTERY(setOf("mystery", "мистерия", "мистери")),
    NEWS(setOf("news", "новини")),
    REALITY_TV(setOf("reality-tv", "риалити")),
    ROMANCE(setOf("romance", "романтичен", "романтична")),
    SCI_FI(setOf("sci-fi", "фантастика")),
    SHORT(setOf("short", "Късометражен")),
    SPORT(setOf("sport", "спортен")),
    TALK_SHOW(setOf("talk-show", "толк-шоу")),
    THRILLER(setOf("thriller", "трилър")),
    WAR(setOf("war", "война", "военен")),
    WESTERN(setOf("western", "уестърн")),
    ;

    companion object {
        fun String.toGenre(): Genre? = values().firstOrNull { lowercase() in it.names }
    }
}