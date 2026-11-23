package com.example.hkueats.data

import com.example.hkueats.BuildConfig

object GoogleMapsConfig {
    val apiKey: String
        get() = BuildConfig.GOOGLE_MAPS_API_KEY.trim()

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()
}
