package com.eugene.aichat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val system = FontFamily.Default

val AIChatTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = system, fontWeight = FontWeight.Medium,
        fontSize = 40.sp, lineHeight = 48.sp
    ),
    displayMedium = TextStyle(
        fontFamily = system, fontWeight = FontWeight.Medium,
        fontSize = 32.sp, lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = system, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = system, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = system, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = system, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = system, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = system, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = system, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = system, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp
    )
)
