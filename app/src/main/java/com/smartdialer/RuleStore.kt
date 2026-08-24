package com.smartdialer

import android.content.Context

/** Keeps rules local and explicit; no call data is uploaded. */
class RuleStore(context: Context) {
    private val preferences = context.getSharedPreferences("call_rules", Context.MODE_PRIVATE)

    var silentNumber: String
        get() = preferences.getString("silent_number", "").orEmpty()
        set(value) = preferences.edit().putString("silent_number", value.filter { it.isDigit() || it == '+' }).apply()

    var silentEnabled: Boolean
        get() = preferences.getBoolean("silent_enabled", false)
        set(value) = preferences.edit().putBoolean("silent_enabled", value).apply()

    fun matches(number: String): Boolean {
        val saved = silentNumber.filter(Char::isDigit)
        val incoming = number.filter(Char::isDigit)
        return silentEnabled && saved.isNotEmpty() && incoming.takeLast(10) == saved.takeLast(10)
    }
}