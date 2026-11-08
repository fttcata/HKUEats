package com.example.hkueats.data

import com.example.hkueats.BuildConfig

object SupabaseConfig {
    val baseUrl: String
        get() = BuildConfig.SUPABASE_URL.trimEnd('/')

    val apiKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank()
}
