package com.biocap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.biocap.app.data.prefs.DeviceModePreferencesRepository
import com.biocap.app.presentation.navigation.AppNavigation
import com.biocap.app.presentation.navigation.Route
import com.biocap.app.ui.theme.VitalWorkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deviceModePreferences: DeviceModePreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // First launch (no mode picked yet) opens the Server/Client picker; afterwards go to Home.
        val startDestination =
            if (deviceModePreferences.getMode() == null) Route.ModeSelection.route
            else Route.Home.route
        setContent {
            VitalWorkTheme {
                AppNavigation(startDestination = startDestination)
            }
        }
    }
}
