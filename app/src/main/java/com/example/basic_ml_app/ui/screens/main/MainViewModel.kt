package com.example.basic_ml_app.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basic_ml_app.domain.repo.IMLRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(private val mlRepo: IMLRepo) : ViewModel() {
    private val _uiState =
        MutableStateFlow(value = MainScreenState(predictionState = PredictionState.Ready()))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mlRepo.getInterpreter()
        }
        // Setup the "Inference Pipeline" ONCE
        // This acts like a permanent listener for as long as the ViewModel exists.
        uiState
            .map { it.inputText } // Only care about text changes
            .distinctUntilChanged()
            .debounce(timeoutMillis = 500L) // Wait for the user to stop typing
            .onEach { input ->
                when {
                    input.isEmpty() -> {
                        _uiState.update {
                            it.copy(
                                inputText = input,
                                predictionState = PredictionState.Ready()
                            )
                        }
                    }
                    else -> {
                        updateInputAndInference(input = input)
                    }
                }
            }
            .launchIn(scope = viewModelScope) // This is your "Collect" started once
        // Writing .onEach { ... }.launchIn(scope) is essentially shorthand for:
//        viewModelScope.launch {
//            uiState.collect { input ->
//                // ... your logic here
//            }
//        }
    }

    @OptIn(FlowPreview::class)
    fun onAction(action: MainScreenAction) {
        when (action) {
            is MainScreenAction.OnTextChanged -> {
                // We ONLY update the state here.
                // The pipeline in init{} will see this and react automatically.
                // The emit happens inside the _uiState.update { ... }
                _uiState.update {
                    it.copy(
                        inputText = action.input,
                        predictionState = PredictionState.Loading()
                    )
                }
            }
        }
    }

    private fun updateInputAndInference(input: String) {
        viewModelScope.launch {
            val result = mlRepo.runInference(input)
            _uiState.update {
                result.fold(
                    onSuccess = { value ->
                        it.copy(
                            inputText = input,
                            predictionState = PredictionState.Success(output = value.toString())
                        )
                    },
                    onFailure = { value ->
                        it.copy(
                            inputText = input,
                            predictionState = PredictionState.Error(errorMessage = value.message.toString())
                        )
                    }
                )
            }
        }
    }
}
