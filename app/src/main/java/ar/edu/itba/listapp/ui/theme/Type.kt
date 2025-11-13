package ar.edu.itba.listapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ar.edu.itba.listapp.R

// Crete Round queda como fuente default en la app
val CreteRoundFontFamily = FontFamily(
    Font(R.font.crete_round_regular, FontWeight.Normal)
)

// Material design typography
// aca cambiamos el tamaño de la fuente segun el tipo de texto
val Typography = Typography(
    // Large display text (biggest)
    displayLarge = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,  // Change this to adjust size
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),

    // Headlines
    headlineLarge = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    // Titles
    titleLarge = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // Body text (most common text)
    bodyLarge = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,  // Medium body text
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,  // Small body text
        lineHeight = 16.sp
    ),

    // Labels (buttons, tabs, etc.)
    labelLarge = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CreteRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,  // Smallest text
        lineHeight = 16.sp
    )
)
