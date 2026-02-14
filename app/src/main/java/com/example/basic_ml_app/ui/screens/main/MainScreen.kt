package com.example.basic_ml_app.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.basic_ml_app.ui.screens.main.PredictionState.*

@Composable
fun MainScreen(
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = "y = 2x -1 Prediction"
        )
        OutlinedTextField(
            value = state.inputText,
            onValueChange = {
                onAction(MainScreenAction.OnTextChanged(input = it))
            },
            label = { Text("Insert a value") }
        )
        Text(
            text = "Result is : ${getResult(state = state.predictionState)}"
        )
    }
}

private fun getResult(state: PredictionState): String {
    return when(state) {
        is Loading -> Loading().output
        is Error -> "Error: ${state.errorMessage}"
        is Success -> state.output
        is Ready -> Ready().output
    }
}

@Preview(
    showBackground = true
)
@Composable
fun PreviewMainScreen() {
    MainScreen(
        state = MainScreenState(
            inputText = "",
            predictionState = Ready()
        ),
        onAction = {}
    )
}