package com.peshkunti.filmitv.classes

enum class Channel(val channelName: String) {
    AXN("axn"),
    AXN_BLACK("axn black"),
    BLOOMBERG_TV("bloomberg tv"),
    BNT("bnt"),
    BNT_2("bnt 2"),
    BNT_3("bnt 3"),
    BNT_4("bnt 4"),
    BTV("btv"),
    BTV_ACTION("btv action"),
    BTV_CINEMA("btv cinema"),
    BTV_COMEDY("btv comedy"),
    BTV_LADY("btv lady"),
    BULGARIA_ON_AIR("bulgaria on air"),
    CARTOON_NETWORK("cartoon network"),
    CINEMAX("cinemax"),
    DIEMA("diema"),
    DIEMA_FAMILY("diema family"),
    DIEMA_SPORT_2("diema sport 2"),
    DISNEY_CHANNEL("disney channel"),
    EUROCOM("eurocom"),
    FILM_BOX("film box"),
    FOX_CHANNEL("fox channel"),
    FOX_LIFE("fox life"),
    HBO("hbo"),
    HBO_2("hbo 2"),
    HISTORY_CHANNEL("history channel"),
    HOBBY_TV("hobby tv"),
    KINONOVA("kinonova"),
    KITCHEN24("24 kitchen"),
    MAX_SPORT_3("max sport 3"),
    MOVIESTAR("moviestar"),
    NOVA_TV("nova tv"),
    ORT("ort"),
    SKAT("skat"),
    TRAVEL_CHANNEL("travel channel"),
    TV1("tv1"),
    TV1000("tv1000"),
    TV_PLUS("tv+"),
    NOVA_NEWS("nova news"),
    VIVACOM_ARENA("vivacom arena"),
    ;

    companion object {
        fun String.toChannel(): Channel? =
            values().firstOrNull { it.channelName.equals(lowercase(), true) }
    }
}