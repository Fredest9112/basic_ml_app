package com.example.basic_ml_app

import app.cash.turbine.test
import com.example.basic_ml_app.domain.repo.IMLRepo
import com.example.basic_ml_app.ui.screens.main.MainScreenAction
import com.example.basic_ml_app.ui.screens.main.MainViewModel
import com.example.basic_ml_app.ui.screens.main.PredictionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    // Replaces Dispatchers.Main with a test dispatcher for every test.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mlRepo: IMLRepo
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        mlRepo = mock()
    }

    // ─── Initial state ────────────────────────────────────────────────────────
    @Test
    fun `initial state has empty input and Ready prediction`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())

        viewModel = MainViewModel(mlRepo)

        val state = viewModel.uiState.value
        assertEquals("", state.inputText)
        assertTrue(
            "Expected Ready state, got ${state.predictionState}",
            state.predictionState is PredictionState.Ready
        )
    }

    @Test
    fun `init block calls getInterpreter on the repo`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())

        viewModel = MainViewModel(mlRepo)
        advanceUntilIdle()

        verify(mlRepo).getInterpreter()
    }

    // ─── onAction – immediate state updates ──────────────────────────────────

    @Test
    fun `OnTextChanged action updates inputText and sets Loading state immediately`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())
        whenever(mlRepo.runInference("5")).thenReturn(Result.success(9f))
        viewModel = MainViewModel(mlRepo)

        viewModel.onAction(MainScreenAction.OnTextChanged(input = "5"))

        val state = viewModel.uiState.value
        assertEquals("5", state.inputText)
        assertTrue(
            "Expected Loading state right after action",
            state.predictionState is PredictionState.Loading
        )
    }

    // ─── Debounce – empty input ───────────────────────────────────────────────

    @Test
    fun `empty input resets prediction to Ready after debounce`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())
        viewModel = MainViewModel(mlRepo)

        viewModel.onAction(MainScreenAction.OnTextChanged(input = ""))
        advanceTimeBy(600L) // past the 500 ms debounce

        val state = viewModel.uiState.value
        assertTrue(
            "Expected Ready after empty input debounce",
            state.predictionState is PredictionState.Ready
        )
        verify(mlRepo, never()).runInference(any())
    }

    // ─── Debounce – inference not triggered before timeout ───────────────────

    @Test
    fun `runInference is NOT called before the debounce period elapses`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())
        whenever(mlRepo.runInference(any())).thenReturn(Result.success(9f))
        viewModel = MainViewModel(mlRepo)

        viewModel.onAction(MainScreenAction.OnTextChanged(input = "5"))
        advanceTimeBy(400L) // still inside the 500 ms window

        verify(mlRepo, never()).runInference(any())
    }

    // ─── Debounce – success path ──────────────────────────────────────────────

    @Test
    fun `valid input after debounce triggers inference and sets Success state`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())
        whenever(mlRepo.runInference("5")).thenReturn(Result.success(9f))
        viewModel = MainViewModel(mlRepo)

        viewModel.onAction(MainScreenAction.OnTextChanged(input = "5"))
        advanceTimeBy(600L)

        val state = viewModel.uiState.value
        assertTrue(
            "Expected Success state after inference",
            state.predictionState is PredictionState.Success
        )
        assertEquals(
            "9.0",
            (state.predictionState as PredictionState.Success).output
        )
    }

    // ─── Debounce – failure path ──────────────────────────────────────────────

    @Test
    fun `inference failure sets Error state with the exception message`() = runTest {
        val errorMessage = "Model crashed"
        whenever(mlRepo.getInterpreter()).thenReturn(mock())
        whenever(mlRepo.runInference(any()))
            .thenReturn(Result.failure(Exception(errorMessage)))
        viewModel = MainViewModel(mlRepo)

        viewModel.onAction(MainScreenAction.OnTextChanged(input = "bad_input"))
        advanceTimeBy(600L)

        val state = viewModel.uiState.value
        assertTrue(
            "Expected Error state after inference failure",
            state.predictionState is PredictionState.Error
        )
        assertEquals(
            errorMessage,
            (state.predictionState as PredictionState.Error).errorMessage
        )
    }

    // ─── Rapid typing – only last emission is processed ─────────────────────

    @Test
    fun `rapid successive actions only trigger one inference after debounce`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())
        whenever(mlRepo.runInference("3")).thenReturn(Result.success(5.0f))
        viewModel = MainViewModel(mlRepo)

        // Simulate fast typing: each keystroke resets the debounce timer
        listOf("1", "12", "123", "1", "3").forEach { text ->
            viewModel.onAction(MainScreenAction.OnTextChanged(input = text))
            advanceTimeBy(100L) // less than debounce each time
        }
        advanceTimeBy(600L) // let the last one fire

        // Only the final value "3" should have been inferred
        verify(mlRepo).runInference("3")
    }

    // ─── Flow emissions via Turbine ──────────────────────────────────────────

    @Test
    fun `uiState emits Loading then Success in order`() = runTest {
        whenever(mlRepo.getInterpreter()).thenReturn(mock())
        whenever(mlRepo.runInference("5")).thenReturn(Result.success(9.0f))
        viewModel = MainViewModel(mlRepo)

        viewModel.uiState.test {
            // Consume the initial Ready emission.
            val initial = awaitItem()
            assertTrue(initial.predictionState is PredictionState.Ready)

            // Trigger the action.
            viewModel.onAction(MainScreenAction.OnTextChanged(input = "5"))

            // Should immediately emit Loading.
            val loadingState = awaitItem()
            assertTrue(loadingState.predictionState is PredictionState.Loading)

            // After debounce + inference, should emit Success.
            advanceTimeBy(600L)
            val successState = awaitItem()
            assertTrue(successState.predictionState is PredictionState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }
}