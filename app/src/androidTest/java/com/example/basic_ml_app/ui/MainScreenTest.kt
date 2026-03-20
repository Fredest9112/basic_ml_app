package com.example.basic_ml_app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.basic_ml_app.ui.screens.main.MainScreen
import com.example.basic_ml_app.ui.screens.main.MainScreenAction
import com.example.basic_ml_app.ui.screens.main.MainScreenState
import com.example.basic_ml_app.ui.screens.main.MainScreenTags
import com.example.basic_ml_app.ui.screens.main.PredictionState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [MainScreen].
 *
 * These tests render the composable in isolation — there is no ViewModel,
 * no Hilt, and no TFLite model involved.  We drive the composable directly
 * with pre-built [MainScreenState] values and a captured [onAction] lambda.
 *
 * NOTE: The OutlinedTextField's label ("Insert a value") is used as a
 * semantic node locator.  If you add testTags to your composables the tests
 * become more robust; swap `onNodeWithText` for `onNodeWithTag` where noted.
 */
@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Static content ────────────────────────────────────────────────────────

    @Test
    fun titleTextIsDisplayed() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(predictionState = PredictionState.Ready()),
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithText("y = 2x -1 Prediction")
            .assertIsDisplayed()
    }

    @Test
    fun textFieldWithCorrectLabelIsDisplayed() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(predictionState = PredictionState.Ready()),
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithTag(MainScreenTags.INPUT_FIELD)
            .assertIsDisplayed()
    }

    @Test
    fun resultPrefixTextIsAlwaysDisplayed() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(predictionState = PredictionState.Ready()),
                onAction = {}
            )
        }

        // The "Result is :" prefix is always rendered regardless of state.
        composeTestRule
            .onNodeWithText("Result is : ", substring = true)
            .assertIsDisplayed()
    }

    // ── PredictionState rendering ─────────────────────────────────────────────

    @Test
    fun readyStateShowsEmptyResult() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(predictionState = PredictionState.Ready()),
                onAction = {}
            )
        }

        // Ready().output is "" so the result label ends with ": "
        composeTestRule
            .onNodeWithText("Result is : Ready")
            .assertIsDisplayed()
    }

    @Test
    fun successStateDisplaysOutputValue() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(
                    inputText = "5",
                    predictionState = PredictionState.Success(output = "9.0")
                ),
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithText("Result is : 9.0")
            .assertIsDisplayed()
    }

    @Test
    fun errorStateDisplaysErrorMessage() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(
                    inputText = "abc",
                    predictionState = PredictionState.Error(errorMessage = "For input string: \"abc\"")
                ),
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithText(
                text = "Result is : Error: For input string: \"abc\""
            )
            .assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsLoadingText() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(
                    inputText = "5",
                    predictionState = PredictionState.Loading()
                ),
                onAction = {}
            )
        }

        // Loading().output value is shown – adjust to match your actual Loading.output string
        composeTestRule
            .onNodeWithText("Result is : Loading")
            .assertIsDisplayed()
    }

    // ── Input text reflected in TextField ─────────────────────────────────────

    @Test
    fun textFieldDisplaysCurrentInputFromState() {
        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(
                    inputText = "42",
                    predictionState = PredictionState.Ready()
                ),
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithText("42")
            .assertIsDisplayed()
    }

    // ── onAction callback ────────────────────────────────────────────────────

    @Test
    fun typingInTextFieldDispatchesOnTextChangedAction() {
        var capturedAction: MainScreenAction? = null

        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(predictionState = PredictionState.Ready()),
                onAction = { capturedAction = it }
            )
        }

        // Type into the OutlinedTextField (identified by its current value node)
        composeTestRule
            .onNodeWithTag(MainScreenTags.INPUT_FIELD) // the label node also works as an ancestor
            .performTextInput("7")

        assert(capturedAction is MainScreenAction.OnTextChanged) {
            "Expected OnTextChanged action but got $capturedAction"
        }
        assert((capturedAction as MainScreenAction.OnTextChanged).input.contains("7")) {
            "Expected input to contain '7' but got ${(capturedAction as MainScreenAction.OnTextChanged).input}"
        }
    }

    @Test
    fun clearingTextFieldDispatchesOnTextChangedWithEmptyInput() {
        var capturedAction: MainScreenAction? = null

        composeTestRule.setContent {
            MainScreen(
                state = MainScreenState(
                    inputText = "5",
                    predictionState = PredictionState.Ready()
                ),
                onAction = { capturedAction = it }
            )
        }

        composeTestRule
            .onNodeWithText("5")
            .performTextClearance()

        assert(capturedAction is MainScreenAction.OnTextChanged)
        assert((capturedAction as MainScreenAction.OnTextChanged).input.isEmpty())

        composeTestRule
            .onNodeWithText("Result is : Ready")
            .assertIsDisplayed()
    }
}