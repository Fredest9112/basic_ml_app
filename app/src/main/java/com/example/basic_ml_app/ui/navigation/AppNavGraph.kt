package com.example.basic_ml_app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.basic_ml_app.ui.screens.main.MainScreen
import com.example.basic_ml_app.ui.screens.main.MainScreenAction
import com.example.basic_ml_app.ui.screens.main.MainViewModel

@Composable
fun AppNavGraph(
    navHostController: NavHostController,
    innerPaddingValues: PaddingValues
) {
    NavHost(
        navController = navHostController,
        startDestination = Screens.MainScreen,
        modifier = Modifier
            .padding(innerPaddingValues)
    ) {
        composable<Screens.MainScreen> {
            val mainScreenViewModel: MainViewModel = hiltViewModel()
            val state by mainScreenViewModel.uiState.collectAsStateWithLifecycle()
            MainScreen(
                state = state,
                onAction = { action ->
                    when(action) {
                        is MainScreenAction.OnTextChanged -> MainScreenAction.OnTextChanged(
                            input = action.input
                        )
                    }
                    mainScreenViewModel.onAction(action = action)
                }
            )
        }
    }
}