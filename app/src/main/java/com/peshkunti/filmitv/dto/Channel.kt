package com.peshkunti.filmitv.dto

enum class Channel(val channelName: String) {
    AXN("AXN"),
    AXN_BLACK("AXN BLACK"),
    BLOOMBERG_TV("Bloomberg TV"),
    BNT("BNT"),
    BNT_2("BNT 2"),
    BNT_3("BNT 3"),
    BNT_4("BNT 4"),
    BTV("BTV"),
    BTV_ACTION("BTV Action"),
    BTV_CINEMA("BTV Cinema"),
    BTV_COMEDY("BTV Comedy"),
    BTV_LADY("BTV Lady"),
    BULGARIA_ON_AIR("Bulgaria on Air"),
    CARTOON_NETWORK("Cartoon Network"),
    CINEMAX("Cinemax"),
    DIEMA("Diema"),
    DIEMA_FAMILY("Diema Family"),
    DIEMA_SPORT_2("Diema Sport 2"),
    DISNEY_CHANNEL("Disney Channel"),
    EUROCOM("Eurocom"),
    FILM_BOX("Film Box"),
    FOX_CHANNEL("Fox Channel"),
    FOX_CRIME("FOX Crime"),
    FOX_LIFE("Fox Life"),
    HBO("HBO"),
    HBO_2("HBO 2"),
    HISTORY_CHANNEL("History Channel"),
    HOBBY_TV("Hobby TV"),
    KINONOVA("KinoNova"),
    KITCHEN24("24 Kitchen"),
    MAX_SPORT_2("Max Sport 2"),
    MAX_SPORT_3("Max Sport 3"),
    MOVIESTAR("Moviestar"),
    NOVA_NEWS("Nova News"),
    NOVA_TV("Nova TV"),
    ORT("ORT"),
    SKAT("Skat"),
    TRAVEL_CHANNEL("Travel Channel"),
    TV1("TV1"),
    TV1000("TV1000"),
    TV_PLUS("TV+"),
    VIVACOM_ARENA("Vivacom Arena"),
    ;

    companion object {
        fun String.toChannel(): Channel? =
            values().firstOrNull { it.channelName.equals(this, true) }
    }
}