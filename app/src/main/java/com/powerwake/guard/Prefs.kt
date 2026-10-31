package com.powerwake.guard

import android.content.Context

/** Small wrapper around SharedPreferences for the app's three settings. */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("power_wake_guard", Context.MODE_PRIVATE)

    /** Master switch. When off, the phone behaves normally. */
    var enabled: Boolean
        get() = sp.getBoolean(KEY_ENABLED, true)
        set(value) {
            sp.edit().putBoolean(KEY_ENABLED, value).apply()
        }

    /** How many power presses in a row are needed to really wake the phone. */
    var presses: Int
        get() = sp.getInt(KEY_PRESSES, 3).coerceIn(MIN_PRESSES, MAX_PRESSES)
        set(value) {
            sp.edit().putInt(KEY_PRESSES, value.coerceIn(MIN_PRESSES, MAX_PRESSES)).apply()
        }

    /** Longest allowed pause (seconds) between two presses of the sequence. */
    var gapSeconds: Int
        get() = sp.getInt(KEY_GAP, 2).coerceIn(MIN_GAP, MAX_GAP)
        set(value) {
            sp.edit().putInt(KEY_GAP, value.coerceIn(MIN_GAP, MAX_GAP)).apply()
        }

    companion object {
        private const val KEY_ENABLED = "enabled"
        private const val KEY_PRESSES = "presses"
        private const val KEY_GAP = "gap_seconds"

        const val MIN_PRESSES = 2
        const val MAX_PRESSES = 6
        const val MIN_GAP = 1
        const val MAX_GAP = 5
    }
}
