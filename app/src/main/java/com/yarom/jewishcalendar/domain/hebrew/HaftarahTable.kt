package com.yarom.jewishcalendar.domain.hebrew

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar.Parsha

/**
 * Standard Ashkenazi haftarah for each regular weekly parasha (spec follow-up: "הפטרה" under the
 * parasha name on the weekly Shabbat bar).
 *
 * Scope/limitation: this covers the *default* haftarah for each parasha only. It does not
 * override the special-Shabbat readings (Shabbat Rosh Chodesh/Machar Chodesh, the Four
 * Parshiyot - Shekalim/Zachor/Parah/HaChodesh, Chanukah Shabbat, Shabbat Shuva) - those weeks
 * will show the underlying parasha's regular haftarah here instead of the special one. Please
 * double-check against a printed chumash/siddur before relying on this for davening.
 */
private val haftarahByParsha: Map<Parsha, String> = mapOf(
    Parsha.BERESHIS to "ישעיהו מ\"ב:ה'-מ\"ג:י'",
    Parsha.NOACH to "ישעיהו נ\"ד:א'-נ\"ה:ה'",
    Parsha.LECH_LECHA to "ישעיהו מ':כ\"ז-מ\"א:ט\"ז",
    Parsha.VAYERA to "מלכים ב' ד':א'-ל\"ז",
    Parsha.CHAYEI_SARA to "מלכים א' א':א'-ל\"א",
    Parsha.TOLDOS to "מלאכי א':א'-ב':ז'",
    Parsha.VAYETZEI to "הושע י\"ב:י\"ג-י\"ד:י'",
    Parsha.VAYISHLACH to "הושע י\"א:ז'-י\"ב:י\"ב",
    Parsha.VAYESHEV to "עמוס ב':ו'-ג':ח'",
    Parsha.MIKETZ to "מלכים א' ג':ט\"ו-ד':א'",
    Parsha.VAYIGASH to "יחזקאל ל\"ז:ט\"ו-כ\"ח",
    Parsha.VAYECHI to "מלכים א' ב':א'-י\"ב",
    Parsha.SHEMOS to "ישעיהו כ\"ז:ו'-כ\"ח:י\"ג; כ\"ט:כ\"ב-כ\"ג",
    Parsha.VAERA to "יחזקאל כ\"ח:כ\"ה-כ\"ט:כ\"א",
    Parsha.BO to "ירמיהו מ\"ו:י\"ג-כ\"ח",
    Parsha.BESHALACH to "שופטים ד':ד'-ה':ל\"א",
    Parsha.YISRO to "ישעיהו ו':א'-ז':ו'; ט':ה'-ו'",
    Parsha.MISHPATIM to "ירמיהו ל\"ד:ח'-כ\"ב; ל\"ג:כ\"ה-כ\"ו",
    Parsha.TERUMAH to "מלכים א' ה':כ\"ו-ו':י\"ג",
    Parsha.TETZAVEH to "יחזקאל מ\"ג:י'-כ\"ז",
    Parsha.KI_SISA to "מלכים א' י\"ח:א'-ל\"ט",
    Parsha.VAYAKHEL to "מלכים א' ז':י\"ג-כ\"ו",
    Parsha.PEKUDEI to "מלכים א' ז':נ\"א-ח':כ\"א",
    Parsha.VAYAKHEL_PEKUDEI to "מלכים א' ז':נ\"א-ח':כ\"א",
    Parsha.VAYIKRA to "ישעיהו מ\"ג:כ\"א-מ\"ד:כ\"ג",
    Parsha.TZAV to "ירמיהו ז':כ\"א-ח':ג'; ט':כ\"ב-כ\"ג",
    Parsha.SHMINI to "שמואל ב' ו':א'-י\"ט",
    Parsha.TAZRIA to "מלכים ב' ד':מ\"ב-ה':י\"ט",
    Parsha.METZORA to "מלכים ב' ז':ג'-כ'",
    Parsha.TAZRIA_METZORA to "מלכים ב' ז':ג'-כ'",
    Parsha.ACHREI_MOS to "יחזקאל כ\"ב:א'-י\"ט",
    Parsha.KEDOSHIM to "עמוס ט':ז'-ט\"ו",
    Parsha.ACHREI_MOS_KEDOSHIM to "עמוס ט':ז'-ט\"ו",
    Parsha.EMOR to "יחזקאל מ\"ד:ט\"ו-ל\"א",
    Parsha.BEHAR to "ירמיהו ל\"ב:ו'-כ\"ז",
    Parsha.BECHUKOSAI to "ירמיהו ט\"ז:י\"ט-י\"ז:י\"ד",
    Parsha.BEHAR_BECHUKOSAI to "ירמיהו ט\"ז:י\"ט-י\"ז:י\"ד",
    Parsha.BAMIDBAR to "הושע ב':א'-כ\"ב",
    Parsha.NASSO to "שופטים י\"ג:ב'-כ\"ה",
    Parsha.BEHAALOSCHA to "זכריה ב':י\"ד-ד':ז'",
    Parsha.SHLACH to "יהושע ב':א'-כ\"ד",
    Parsha.KORACH to "שמואל א' י\"א:י\"ד-י\"ב:כ\"ב",
    Parsha.CHUKAS to "שופטים י\"א:א'-ל\"ג",
    Parsha.BALAK to "מיכה ה':ו'-ו':ח'",
    Parsha.CHUKAS_BALAK to "מיכה ה':ו'-ו':ח'",
    Parsha.PINCHAS to "מלכים א' י\"ח:מ\"ו-י\"ט:כ\"א",
    Parsha.MATOS to "ירמיהו א':א'-ב':ג'",
    Parsha.MASEI to "ירמיהו ב':ד'-כ\"ח; ג':ד'",
    Parsha.MATOS_MASEI to "ירמיהו ב':ד'-כ\"ח; ג':ד'",
    Parsha.DEVARIM to "ישעיהו א':א'-כ\"ז",
    Parsha.VAESCHANAN to "ישעיהו מ':א'-כ\"ו",
    Parsha.EIKEV to "ישעיהו מ\"ט:י\"ד-נ\"א:ג'",
    Parsha.REEH to "ישעיהו נ\"ד:י\"א-נ\"ה:ה'",
    Parsha.SHOFTIM to "ישעיהו נ\"א:י\"ב-נ\"ב:י\"ב",
    Parsha.KI_SEITZEI to "ישעיהו נ\"ד:א'-י'",
    Parsha.KI_SAVO to "ישעיהו ס':א'-כ\"ב",
    Parsha.NITZAVIM to "ישעיהו ס\"א:י'-ס\"ג:ט'",
    Parsha.VAYEILECH to "הושע י\"ד:ב'-י'",
    Parsha.NITZAVIM_VAYEILECH to "ישעיהו ס\"א:י'-ס\"ג:ט'",
    Parsha.HAAZINU to "שמואל ב' כ\"ב:א'-נ\"א",
    Parsha.VZOS_HABERACHA to "יהושע א':א'-י\"ח",
)

fun haftarahFor(parsha: Parsha): String? = haftarahByParsha[parsha]
