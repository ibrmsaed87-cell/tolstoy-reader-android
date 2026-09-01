package com.spinel.tolstoyreader.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reading_settings")

class ReadingPreferencesManager(private val context: Context) {
    companion object {
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val APP_THEME = stringPreferencesKey("app_theme")
        val THEME = stringPreferencesKey("theme")
        val AUTO_SCROLL_SPEED = floatPreferencesKey("auto_scroll_speed")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }


    val appLanguageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_LANGUAGE] ?: "en"
    }

    val fontSizeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 18f
    }

    val lineSpacingFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LINE_SPACING] ?: 1.5f
    }

        val appThemeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[APP_THEME] ?: "system"
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME] ?: "system"
    }
    
    val autoScrollSpeedFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[AUTO_SCROLL_SPEED] ?: 1.0f
    }


    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE] = lang
        }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setLineSpacing(spacing: Float) {
        context.dataStore.edit { preferences ->
            preferences[LINE_SPACING] = spacing
        }
    }

        suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }
    
    suspend fun setAutoScrollSpeed(speed: Float) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_SCROLL_SPEED] = speed
        }
    }
}
