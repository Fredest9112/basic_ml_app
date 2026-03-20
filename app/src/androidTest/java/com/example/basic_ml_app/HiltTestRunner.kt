package com.example.basic_ml_app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner that replaces the real Application with
 * [HiltTestApplication] so Hilt's component tree is available
 * during instrumented tests.
 *
 * Wire it up in build.gradle.kts:
 *   testInstrumentationRunner = "com.example.basic_ml_app.HiltTestRunner"
 */

class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}