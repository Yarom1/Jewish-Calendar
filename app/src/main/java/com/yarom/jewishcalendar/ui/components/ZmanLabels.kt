package com.yarom.jewishcalendar.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yarom.jewishcalendar.R
import com.yarom.jewishcalendar.domain.zmanim.ZmanType

@StringRes
fun ZmanType.labelRes(): Int = when (this) {
    ZmanType.ALOS_HASHACHAR -> R.string.zman_alos
    ZmanType.SUNRISE -> R.string.zman_sunrise
    ZmanType.SOF_ZMAN_SHEMA_GRA -> R.string.zman_sof_zman_shema_gra
    ZmanType.SOF_ZMAN_SHEMA_MGA -> R.string.zman_sof_zman_shema_mga
    ZmanType.SOF_ZMAN_TEFILA -> R.string.zman_sof_zman_tefila
    ZmanType.CHATZOS -> R.string.zman_chatzos
    ZmanType.MINCHA_GEDOLA -> R.string.zman_mincha_gedola
    ZmanType.MINCHA_KETANA -> R.string.zman_mincha_ketana
    ZmanType.PLAG_HAMINCHA -> R.string.zman_plag_hamincha
    ZmanType.SUNSET -> R.string.zman_sunset
    ZmanType.TZEIS_HAKOCHAVIM -> R.string.zman_tzeis
    ZmanType.CANDLE_LIGHTING -> R.string.zman_candle_lighting
}

/**
 * Shorter label for the weekly view's narrow fixed-width label column (spec follow-up): the
 * full "סוף זמן ק"ש (גר"א)" label was silently clipping its own method suffix there, making the
 * GRA/MGA sof-zman-shema rows look identical. Only these two need shortening; every other label
 * already fits.
 */
@Composable
fun ZmanType.compactLabel(): String = when (this) {
    ZmanType.SOF_ZMAN_SHEMA_GRA -> "ק\"ש (גר\"א)"
    ZmanType.SOF_ZMAN_SHEMA_MGA -> "ק\"ש (מג\"א)"
    else -> stringResource(labelRes())
}
