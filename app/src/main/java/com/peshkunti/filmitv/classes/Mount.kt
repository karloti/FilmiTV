package com.peshkunti.filmitv.classes

enum class Mount(val nameBG: String) {
    JANUARY("януари"),
    FEBRUARY("февруари"),
    MARCH("март"),
    APRIL("април"),
    MAY("май"),
    JUNE("юни"),
    JULY("юли"),
    AUGUST("август"),
    SEPTEMBER("септември"),
    OCTOBER("октомври"),
    NOVEMBER("ноември"),
    DECEMBER("декември"),
    ;

    companion object {
        fun getEnum(nameBg: String): Mount? = values().firstOrNull { nameBg.lowercase() == it.nameBG }
    }
}