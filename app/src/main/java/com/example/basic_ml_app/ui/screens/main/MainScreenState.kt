package com.example.basic_ml_app.ui.screens.main

sealed class PredictionState {
    data class Ready(val output: String = "Ready"): PredictionState()
    data class Loading(val output: String = "Loading"): PredictionState()
    data class Error(val errorMessage: String): PredictionState()
    data class Success (val output: String): PredictionState()
}

data class MainScreenState(
    val inputText: String = "",
    val predictionState: PredictionState
)