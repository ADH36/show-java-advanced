

package com.argonaut.showjava.utils

import android.content.SharedPreferences
import com.google.ads.consent.ConsentStatus

/**
 * A thin-wrapper around [SharedPreferences] to expose preferences as getters.
 */
class UserPreferences(private val prefs: SharedPreferences) {

    companion object {
        const val NAME = "user_preferences"
    }

    interface DEFAULTS {
        companion object {
            const val CUSTOM_FONT = true
            const val DARK_MODE = false
            const val SHOW_MEMORY_USAGE = true
            const val SHOW_SYSTEM_APPS = false
            const val MEMORY_THRESHOLD = 80
            const val IGNORE_LIBRARIES = true
            const val KEEP_INTERMEDIATE_FILES = false
            const val CHUNK_SIZE = 500
            const val MAX_ATTEMPTS = 2
        }
    }

    val ignoreLibraries: Boolean
        get() = prefs.getBoolean("ignoreLibraries", DEFAULTS.IGNORE_LIBRARIES)

    val keepIntermediateFiles: Boolean
        get() = prefs.getBoolean("keepIntermediateFiles", DEFAULTS.KEEP_INTERMEDIATE_FILES)

    val customFont: Boolean
        get() = prefs.getBoolean("customFont", DEFAULTS.CUSTOM_FONT)

    val darkMode: Boolean
        get() = prefs.getBoolean("darkMode", DEFAULTS.DARK_MODE)

    val showMemoryUsage: Boolean
        get() = prefs.getBoolean("showMemoryUsage", DEFAULTS.SHOW_MEMORY_USAGE)

    val showSystemApps: Boolean
        get() = prefs.getBoolean("showSystemApps", DEFAULTS.SHOW_SYSTEM_APPS)

    val chunkSize: Int
        get() = try {
            prefs.getString("chunkSize", DEFAULTS.CHUNK_SIZE.toString())?.trim()?.toInt()
                    ?: DEFAULTS.CHUNK_SIZE
        } catch (ignored: Exception) {
            DEFAULTS.CHUNK_SIZE
        }

    val maxAttempts: Int
        get() = try {
            prefs.getString("maxAttempts", DEFAULTS.MAX_ATTEMPTS.toString())?.trim()?.toInt()
                    ?: DEFAULTS.MAX_ATTEMPTS
        } catch (ignored: Exception) {
            DEFAULTS.MAX_ATTEMPTS
        }

    val memoryThreshold: Int
        get() = try {
            prefs.getString(
                "memoryThreshold",
                DEFAULTS.MEMORY_THRESHOLD.toString()
            )?.trim()?.toInt()
                    ?: DEFAULTS.MEMORY_THRESHOLD
        } catch (ignored: Exception) {
            DEFAULTS.MEMORY_THRESHOLD
        }

    val consentStatus: Int
        get() = prefs.getInt("consentStatus", ConsentStatus.UNKNOWN.ordinal)
}