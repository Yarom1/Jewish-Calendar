package com.yarom.jewishcalendar.domain.zmanim

/** Every zman the settings screen (spec 4.a) lets the user toggle on/off per view. */
enum class ZmanType {
    ALOS_HASHACHAR,
    SUNRISE,
    SOF_ZMAN_SHEMA_GRA,
    SOF_ZMAN_SHEMA_MGA,
    SOF_ZMAN_TEFILA,
    CHATZOS,
    MINCHA_GEDOLA,
    MINCHA_KETANA,
    PLAG_HAMINCHA,
    SUNSET,
    TZEIS_HAKOCHAVIM,
    CANDLE_LIGHTING,
    ;

    companion object {
        /** Sensible default selection shown on first launch (weekly/daily headline zmanim). */
        val DEFAULT_VISIBLE: Set<ZmanType> = setOf(
            SUNRISE,
            SOF_ZMAN_SHEMA_GRA,
            MINCHA_GEDOLA,
            SUNSET,
            TZEIS_HAKOCHAVIM,
            CANDLE_LIGHTING,
        )
    }
}

/** Halachic calculation method for shema/tefila/day-boundary zmanim, per spec 4.c. */
enum class CalculationMethod(val displayName: String) {
    GRA("הגר\"א"),
    MAGEN_AVRAHAM("מגן אברהם"),
}
