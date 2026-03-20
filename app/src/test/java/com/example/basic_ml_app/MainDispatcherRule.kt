package com.example.basic_ml_app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that swaps [Dispatchers.Main] for a [TestDispatcher] for the
 * duration of each test, then resets it afterwards.
 *
 * Usage:
 *   @get:Rule val mainDispatcherRule = MainDispatcherRule()
 *
 * [UnconfinedTestDispatcher] is used by default so that coroutines launched
 * inside a ViewModel's init block run eagerly without extra `advanceUntilIdle`
 * calls for simple assertions.  Replace with [StandardTestDispatcher] if you
 * need to control execution order manually.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}