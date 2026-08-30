package com.vpet.android.utils

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREF_NAME = "vpet_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"

    fun getApiKey(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    }

    fun setApiKey(context: Context, apiKey: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
    }
}
