package com.example.hkueats.data

import com.example.hkueats.BuildConfig

object SupabaseConfig {
    val baseUrl: String
        get() = BuildConfig.SUPABASE_URL.trimEnd('/')

    val apiKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() &&
                apiKey.isNotBlank() &&
                !baseUrl.contains("your-project.supabase.co") &&
                apiKey != "your_supabase_anon_key_here"
}
