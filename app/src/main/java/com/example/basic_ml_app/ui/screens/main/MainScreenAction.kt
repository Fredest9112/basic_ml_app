package com.example.basic_ml_app.ui.screens.main

sealed interface MainScreenAction {
    data class OnTextChanged(val input: String): MainScreenAction
}