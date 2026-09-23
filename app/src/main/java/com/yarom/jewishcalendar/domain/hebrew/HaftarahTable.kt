package com.yarom.jewishcalendar.domain.hebrew

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar.Parsha
import java.util.Calendar

/**
 * The haftarah reading for a Shabbat, formatted the way a printed luach announces it: "הפטרת
 * השבת: <book> פרק <chapter> - <opening words>" (spec follow-up - previously this only showed a
 * verse-range citation like "ישעיהו מ"ב:ה'-מ"ג:י'", with no indication of which words the
 * reading actually opens with).
 *
 * Scope/limitation: opening words are transcribed from memory of the standard Ashkenazi text and
 * spot-checked, not verified word-for-word against a printed Tanach for every entry. Please
 * double-check the exact wording against a printed chumash/siddur before relying on this for
 * davening.
 */
private data class Haftarah(val book: String, val chapter: String, val opening: String) {
    fun format(): String = "$book פרק $chapter - $opening"
}

/** Standard Ashkenazi haftarah for each regular weekly parasha, when no special Shabbat overrides it. */
private val haftarahByParsha: Map<Parsha, Haftarah> = mapOf(
    Parsha.BERESHIS to Haftarah("ישעיהו", "מ\"ב", "כה אמר האל ה' בורא השמים ונוטיהם"),
    Parsha.NOACH to Haftarah("ישעיהו", "נ\"ד", "רני עקרה לא ילדה"),
    Parsha.LECH_LECHA to Haftarah("ישעיהו", "מ'", "למה תאמר יעקב ותדבר ישראל נסתרה דרכי מה'"),
    Parsha.VAYERA to Haftarah("מלכים ב'", "ד'", "ואשה אחת מנשי בני הנביאים צעקה אל אלישע"),
    Parsha.CHAYEI_SARA to Haftarah("מלכים א'", "א'", "והמלך דוד זקן בא בימים"),
    Parsha.TOLDOS to Haftarah("מלאכי", "א'", "משא דבר ה' אל ישראל ביד מלאכי"),
    Parsha.VAYETZEI to Haftarah("הושע", "י\"ב", "ויברח יעקב שדה ארם"),
    Parsha.VAYISHLACH to Haftarah("הושע", "י\"א", "ועמי תלואים למשובתי"),
    Parsha.VAYESHEV to Haftarah("עמוס", "ב'", "כה אמר ה' על שלשה פשעי ישראל"),
    Parsha.MIKETZ to Haftarah("מלכים א'", "ג'", "ויקץ שלמה והנה חלום"),
    Parsha.VAYIGASH to Haftarah("יחזקאל", "ל\"ז", "ויהי דבר ה' אלי לאמר ואתה בן אדם קח לך עץ אחד"),
    Parsha.VAYECHI to Haftarah("מלכים א'", "ב'", "ויקרבו ימי דוד למות"),
    Parsha.SHEMOS to Haftarah("ישעיהו", "כ\"ז", "הבאים ישרש יעקב"),
    Parsha.VAERA to Haftarah("יחזקאל", "כ\"ח", "כה אמר אדני אלוקים בקבצי את בית ישראל"),
    Parsha.BO to Haftarah("ירמיהו", "מ\"ו", "הדבר אשר דבר ה' אל ירמיהו הנביא לבוא נבוכדראצר"),
    Parsha.BESHALACH to Haftarah("שופטים", "ד'", "ותשר דבורה וברק בן אבינעם"),
    Parsha.YISRO to Haftarah("ישעיהו", "ו'", "בשנת מות המלך עזיהו"),
    Parsha.MISHPATIM to Haftarah("ירמיהו", "ל\"ד", "הדבר אשר היה אל ירמיהו מאת ה' אחרי כרת המלך צדקיהו"),
    Parsha.TERUMAH to Haftarah("מלכים א'", "ה'", "וה' נתן חכמה לשלמה"),
    Parsha.TETZAVEH to Haftarah("יחזקאל", "מ\"ג", "אתה בן אדם הגד את בית ישראל את הבית"),
    Parsha.KI_SISA to Haftarah("מלכים א'", "י\"ח", "ויהי ימים רבים ודבר ה' היה אל אליהו"),
    Parsha.VAYAKHEL to Haftarah("מלכים א'", "ז'", "וישלח המלך שלמה ויקח את חירם מצר"),
    Parsha.PEKUDEI to Haftarah("מלכים א'", "ז'", "ותשלם כל המלאכה אשר עשה המלך שלמה"),
    Parsha.VAYAKHEL_PEKUDEI to Haftarah("מלכים א'", "ז'", "וישלח המלך שלמה ויקח את חירם מצר"),
    Parsha.VAYIKRA to Haftarah("ישעיהו", "מ\"ג", "עם זו יצרתי לי תהלתי יספרו"),
    Parsha.TZAV to Haftarah("ירמיהו", "ז'", "כה אמר ה' צבאות אלוקי ישראל עלותיכם ספו על זבחיכם"),
    Parsha.SHMINI to Haftarah("שמואל ב'", "ו'", "ויאסף עוד דוד את כל בחור בישראל"),
    Parsha.TAZRIA to Haftarah("מלכים ב'", "ד'", "ואיש בא מבעל שלישה"),
    Parsha.METZORA to Haftarah("מלכים ב'", "ז'", "וארבעה אנשים היו מצרעים"),
    Parsha.TAZRIA_METZORA to Haftarah("מלכים ב'", "ז'", "וארבעה אנשים היו מצרעים"),
    Parsha.ACHREI_MOS to Haftarah("יחזקאל", "כ\"ב", "ואתה בן אדם התשפט התשפט את עיר הדמים"),
    Parsha.KEDOSHIM to Haftarah("עמוס", "ט'", "הלוא כבני כשיים אתם לי בני ישראל"),
    Parsha.ACHREI_MOS_KEDOSHIM to Haftarah("עמוס", "ט'", "הלוא כבני כשיים אתם לי בני ישראל"),
    Parsha.EMOR to Haftarah("יחזקאל", "מ\"ד", "והכהנים הלוים בני צדוק"),
    Parsha.BEHAR to Haftarah("ירמיהו", "ל\"ב", "ויאמר ירמיהו היה דבר ה' אלי לאמר"),
    Parsha.BECHUKOSAI to Haftarah("ירמיהו", "ט\"ז", "ה' עזי ומעזי ומנוסי ביום צרה"),
    Parsha.BEHAR_BECHUKOSAI to Haftarah("ירמיהו", "ט\"ז", "ה' עזי ומעזי ומנוסי ביום צרה"),
    Parsha.BAMIDBAR to Haftarah("הושע", "ב'", "והיה מספר בני ישראל כחול הים"),
    Parsha.NASSO to Haftarah("שופטים", "י\"ג", "ויהי איש אחד מצרעה"),
    Parsha.BEHAALOSCHA to Haftarah("זכריה", "ב'", "רני ושמחי בת ציון"),
    Parsha.SHLACH to Haftarah("יהושע", "ב'", "וישלח יהושע בן נון מן השטים שנים אנשים מרגלים"),
    Parsha.KORACH to Haftarah("שמואל א'", "י\"א", "ויאמר שמואל אל העם לכו ונלכה הגלגל"),
    Parsha.CHUKAS to Haftarah("שופטים", "י\"א", "ויפתח הגלעדי היה גבור חיל"),
    Parsha.BALAK to Haftarah("מיכה", "ה'", "והיה שארית יעקב בקרב עמים רבים"),
    Parsha.CHUKAS_BALAK to Haftarah("מיכה", "ה'", "והיה שארית יעקב בקרב עמים רבים"),
    Parsha.PINCHAS to Haftarah("מלכים א'", "י\"ח", "ויד ה' היתה אל אליהו"),
    Parsha.MATOS to Haftarah("ירמיהו", "א'", "דברי ירמיהו בן חלקיהו"),
    Parsha.MASEI to Haftarah("ירמיהו", "ב'", "שמעו דבר ה' בית יעקב"),
    Parsha.MATOS_MASEI to Haftarah("ירמיהו", "ב'", "שמעו דבר ה' בית יעקב"),
    Parsha.DEVARIM to Haftarah("ישעיהו", "א'", "חזון ישעיהו בן אמוץ"),
    Parsha.VAESCHANAN to Haftarah("ישעיהו", "מ'", "נחמו נחמו עמי יאמר אלוקיכם"),
    Parsha.EIKEV to Haftarah("ישעיהו", "מ\"ט", "ותאמר ציון עזבני ה' וה' שכחני"),
    Parsha.REEH to Haftarah("ישעיהו", "נ\"ד", "עניה סערה לא נחמה"),
    Parsha.SHOFTIM to Haftarah("ישעיהו", "נ\"א", "אנכי אנכי הוא מנחמכם"),
    Parsha.KI_SEITZEI to Haftarah("ישעיהו", "נ\"ד", "רני עקרה לא ילדה"),
    Parsha.KI_SAVO to Haftarah("ישעיהו", "ס'", "קומי אורי כי בא אורך"),
    Parsha.NITZAVIM to Haftarah("ישעיהו", "ס\"א", "שוש אשיש בה'"),
    Parsha.VAYEILECH to Haftarah("הושע", "י\"ד", "שובה ישראל עד ה' אלוקיך"),
    Parsha.NITZAVIM_VAYEILECH to Haftarah("ישעיהו", "ס\"א", "שוש אשיש בה'"),
    Parsha.HAAZINU to Haftarah("שמואל ב'", "כ\"ב", "וידבר דוד לה' את דברי השירה הזאת"),
    Parsha.VZOS_HABERACHA to Haftarah("יהושע", "א'", "ויהי אחרי מות משה עבד ה'"),
)

/** Special Shabbatot whose haftarah replaces the regular parasha's, keyed by a short Hebrew label. */
private data class SpecialShabbos(val label: String, val haftarah: Haftarah)

private val hagadolHaftarah = Haftarah("מלאכי", "ג'", "וערבה לה' מנחת יהודה וירושלם כימי עולם וכשנים קדמוניות")
private val chazonHaftarah = Haftarah("ישעיהו", "א'", "חזון ישעיהו בן אמוץ")
private val nachamuHaftarah = Haftarah("ישעיהו", "מ'", "נחמו נחמו עמי יאמר אלוקיכם")
private val shuvaHaftarah = Haftarah("הושע", "י\"ד", "שובה ישראל עד ה' אלוקיך")

/**
 * Returns the special-Shabbat override for [saturday] (a [JewishCalendar] already advanced to
 * the relevant Shabbos), or null on a regular Shabbat with no override. Ordered by halachic
 * precedence: the four parshiyot / Hagadol / Chazon / Nachamu / Shuva (all mutually exclusive by
 * month, computed by KosherJava's own getSpecialShabbos()) outrank Chanukah, which outranks a
 * plain Shabbat Rosh Chodesh, which outranks Shabbat Mevarchim/Machar Chodesh.
 */
private fun specialShabbosFor(saturday: JewishCalendar): SpecialShabbos? {
    if (saturday.dayOfWeek != Calendar.SATURDAY) return null

    when (saturday.specialShabbos) {
        Parsha.SHKALIM -> return SpecialShabbos("שבת שקלים", Haftarah("מלכים ב'", "י\"ב", "בן שבע שנים יהואש במלכו"))
        Parsha.ZACHOR -> return SpecialShabbos("שבת זכור", Haftarah("שמואל א'", "ט\"ו", "כה אמר ה' צבאות פקדתי את אשר עשה עמלק לישראל"))
        Parsha.PARA -> return SpecialShabbos("שבת פרה", Haftarah("יחזקאל", "ל\"ו", "בן אדם בית ישראל ישבים על אדמתם ויטמאו אותה"))
        Parsha.HACHODESH -> return SpecialShabbos("שבת החודש", Haftarah("יחזקאל", "מ\"ה", "כה אמר אדני אלוקים בראשון באחד לחדש"))
        Parsha.HAGADOL -> return SpecialShabbos("שבת הגדול", hagadolHaftarah)
        Parsha.CHAZON -> return SpecialShabbos("שבת חזון", chazonHaftarah)
        Parsha.NACHAMU -> return SpecialShabbos("שבת נחמו", nachamuHaftarah)
        Parsha.SHUVA -> return SpecialShabbos("שבת שובה", shuvaHaftarah)
        else -> {}
    }

    if (saturday.isChanukah) {
        return if (saturday.dayOfChanukah <= 7) {
            SpecialShabbos("שבת חנוכה", Haftarah("זכריה", "ב'", "רני ושמחי בת ציון"))
        } else {
            SpecialShabbos("שבת חנוכה (שנייה)", Haftarah("מלכים א'", "ז'", "ויעש חירם את הכיורות ואת היעים ואת המזרקות"))
        }
    }
    if (saturday.isRoshChodesh) {
        return SpecialShabbos("שבת ראש חודש", Haftarah("ישעיהו", "ס\"ו", "כה אמר ה' השמים כסאי והארץ הדום רגלי"))
    }
    if (saturday.isErevRoshChodesh) {
        return SpecialShabbos("שבת מברכים (מחר חודש)", Haftarah("שמואל א'", "כ'", "ויאמר לו יהונתן מחר חודש"))
    }
    return null
}

/** The heading to show: "הפטרת השבת" for a regular week, or "הפטרת <special Shabbat's own name>". */
fun specialShabbosLabel(saturday: JewishCalendar): String {
    val special = specialShabbosFor(saturday) ?: return "הפטרת השבת"
    return "הפטרת ${special.label}"
}

/** Returns the haftarah text to display for [saturday]'s parasha [parsha], applying any special-Shabbat override. */
fun haftarahFor(saturday: JewishCalendar, parsha: Parsha): String? {
    specialShabbosFor(saturday)?.let { return it.haftarah.format() }
    return haftarahByParsha[parsha]?.format()
}
