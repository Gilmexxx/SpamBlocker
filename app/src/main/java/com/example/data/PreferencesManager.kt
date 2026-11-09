package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("spamblocker_prefs", Context.MODE_PRIVATE)

    private val _isProtectionActiveFlow = MutableStateFlow(isProtectionActive)
    val isProtectionActiveFlow: StateFlow<Boolean> = _isProtectionActiveFlow.asStateFlow()

    init {
        // Ensure initial filtered calls count starts at 0 per user requirement
        if (!prefs.contains(KEY_INITIALIZED_ZERO)) {
            prefs.edit()
                .putInt(KEY_TOTAL_CALLS_SCREENED, 0)
                .putBoolean(KEY_INITIALIZED_ZERO, true)
                .apply()
        }
    }

    var isProtectionActive: Boolean
        get() = prefs.getBoolean(KEY_PROTECTION_ACTIVE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_PROTECTION_ACTIVE, value).apply()
            _isProtectionActiveFlow.value = value
        }

    var totalCallsScreened: Int
        get() = prefs.getInt(KEY_TOTAL_CALLS_SCREENED, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_CALLS_SCREENED, value).apply()

    fun incrementCallsScreened(): Int {
        val current = totalCallsScreened + 1
        totalCallsScreened = current
        return current
    }

    fun resetCallsScreened() {
        totalCallsScreened = 0
    }

    companion object {
        private const val KEY_PROTECTION_ACTIVE = "protection_active"
        private const val KEY_TOTAL_CALLS_SCREENED = "total_calls_screened"
        private const val KEY_INITIALIZED_ZERO = "initialized_zero_v2"

        @Volatile
        private var instance: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context).also { instance = it }
            }
        }
    }
}
