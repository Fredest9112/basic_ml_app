package com.example.basic_ml_app.data

import android.content.Context
import com.example.basic_ml_app.data.repoimpl.MLRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.tensorflow.lite.Interpreter

/**
 * Unit tests for [MLRepo].
 *
 * [loadModelFile] and [getInterpreter] rely on real TFLite assets and cannot
 * be meaningfully unit-tested without an actual model file, so those paths are
 * covered in the instrumented suite (MLRepoInstrumentedTest).
 *
 * Here we focus on [runInference] by accessing the interpreter field via
 * reflection so we can inject a mock [Interpreter] without touching the
 * Android asset system.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MLRepoTest {

    private lateinit var context: Context
    private lateinit var repo: MLRepo

    @Before
    fun setup() {
        context = mock()
        repo = MLRepo(context)
    }

    // ── Helper to inject a mock Interpreter via reflection ──────────────────

    private fun injectInterpreter(interpreter: Interpreter?) {
        val field = MLRepo::class.java.getDeclaredField("interpreter")
        field.isAccessible = true
        field.set(repo, interpreter)
    }

    // ── runInference – happy path ────────────────────────────────────────────

    @Test
    fun `runInference returns Success with correct float for a valid numeric string`() = runTest {
        val mockInterpreter: Interpreter = mock()

        // Capture the output buffer that the repo passes to interpreter.run()
        // and write the expected result into it.
        whenever(mockInterpreter.run(any<Any>(), any<Any>())).thenAnswer { invocation ->
            val outputBuffer = invocation.getArgument<Array<FloatArray>>(1)
            outputBuffer[0][0] = 9.0f // y = 2*5 - 1
            null
        }
        injectInterpreter(mockInterpreter)

        val result = repo.runInference("5")

        assertTrue("Expected Success", result.isSuccess)
        assertEquals(9.0f, result.getOrNull())
    }

    @Test
    fun `runInference returns Success with negative output for negative input`() = runTest {
        val mockInterpreter: Interpreter = mock()
        whenever(mockInterpreter.run(any<Any>(), any<Any>())).thenAnswer { invocation ->
            val outputBuffer = invocation.getArgument<Array<FloatArray>>(1)
            outputBuffer[0][0] = -3.0f // y = 2*(-1) - 1
            null
        }
        injectInterpreter(mockInterpreter)

        val result = repo.runInference("-1")

        assertTrue(result.isSuccess)
        assertEquals(-3.0f, result.getOrNull())
    }

    // ── runInference – non-numeric input ─────────────────────────────────────

    @Test
    fun `runInference returns Failure when input cannot be parsed as Float`() = runTest {
        injectInterpreter(mock()) // interpreter is present but input is bad

        val result = repo.runInference("not_a_number")

        assertTrue("Expected Failure for non-numeric input", result.isFailure)
        assertTrue(result.exceptionOrNull() is NumberFormatException)
    }

    @Test
    fun `runInference returns Failure for empty string input`() = runTest {
        injectInterpreter(mock())

        val result = repo.runInference("")

        assertTrue("Expected Failure for empty input", result.isFailure)
    }

    // ── runInference – null interpreter (not yet initialised) ────────────────

    @Test
    fun `runInference returns Success with default output when interpreter is null`() = runTest {
        // Interpreter is null → interpreter?.run(...) is a no-op, so the
        // outputBuffer stays at 0f and Result.success(0f) is returned.
        injectInterpreter(null)

        val result = repo.runInference("5")

        // The repo does not guard against a null interpreter; it uses ?.run()
        // which silently skips, leaving the output buffer at 0f.
        assertTrue("Expected Success even with null interpreter (silent skip)", result.isSuccess)
        assertEquals(0f, result.getOrNull())
    }

    // ── runInference – interpreter throws ────────────────────────────────────

    @Test
    fun `runInference wraps interpreter exception in Result failure`() = runTest {
        val mockInterpreter: Interpreter = mock()
        whenever(mockInterpreter.run(any<Any>(), any<Any>()))
            .thenThrow(RuntimeException("TFLite error"))
        injectInterpreter(mockInterpreter)

        val result = repo.runInference("5")

        assertTrue("Expected Failure when interpreter throws", result.isFailure)
        assertEquals("TFLite error", result.exceptionOrNull()?.message)
    }

    // ── closeInterpreter ─────────────────────────────────────────────────────

    @Test
    fun `closeInterpreter calls close on an active interpreter`() = runTest {
        val mockInterpreter: Interpreter = mock()
        injectInterpreter(mockInterpreter)

        repo.closeInterpreter()

        verify(mockInterpreter).close()
    }

    @Test
    fun `closeInterpreter does not throw when interpreter is null`() = runTest {
        injectInterpreter(null)

        // Should complete without exception
        repo.closeInterpreter()
    }
}