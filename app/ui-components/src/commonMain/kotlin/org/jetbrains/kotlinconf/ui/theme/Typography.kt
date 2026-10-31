package org.jetbrains.kotlinconf.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class Typography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val text1: TextStyle,
    val text2: TextStyle
)

internal val AppTypography: Typography
    @Composable
    get() = Typography(
        h1 = TextStyle(
            fontFamily = AppFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp,
            lineHeight = 32.sp,
        ),
        h2 = TextStyle(
            fontFamily = AppFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        h3 = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontFamily = AppFont,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        h4 = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontFamily = AppFont,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        ),
        text1 = TextStyle(
            fontFamily = AppFont,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        ),
        text2 = TextStyle(
            fontFamily = AppFont,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        ),
    )

/** The platform's own typeface, so no font is bundled with the app. */
internal val AppFont: FontFamily
    @Composable
    get() = FontFamily.Default
