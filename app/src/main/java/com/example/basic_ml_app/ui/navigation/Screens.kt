package com.example.basic_ml_app.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screens {
    @Serializable
    data object MainScreen: Screens()
}