package com.suspended.musicplayer.ui.screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class SettingsState(
    val darkMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = false,
    val gaplessPlayback: Boolean = true
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val DARK_MODE_KEY = stringPreferencesKey("dark_mode")
    private val DYNAMIC_COLORS_KEY = booleanPreferencesKey("dynamic_colors")
    private val GAPLESS_KEY = booleanPreferencesKey("gapless_playback")

    val settings: StateFlow<SettingsState> = context.dataStore.data
        .map { prefs ->
            SettingsState(
                darkMode = try {
                    ThemeMode.valueOf(prefs[DARK_MODE_KEY] ?: "SYSTEM")
                } catch (_: Exception) { ThemeMode.SYSTEM },
                dynamicColors = prefs[DYNAMIC_COLORS_KEY] ?: false,
                gaplessPlayback = prefs[GAPLESS_KEY] ?: true
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            context.dataStore.edit { it[DARK_MODE_KEY] = mode.name }
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[DYNAMIC_COLORS_KEY] = enabled }
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { it[GAPLESS_KEY] = enabled }
        }
    }
}
