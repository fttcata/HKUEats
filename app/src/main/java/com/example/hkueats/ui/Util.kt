package com.example.hkueats.ui

import java.util.Locale

/** Format a rating value to one decimal place (e.g. 4.0 → "4.0"). */
fun Double.formatRating(): String = String.format(Locale.US, "%.1f", this)
