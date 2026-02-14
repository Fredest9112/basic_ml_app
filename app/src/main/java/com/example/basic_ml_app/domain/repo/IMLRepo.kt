package com.example.basic_ml_app.domain.repo

import org.tensorflow.lite.Interpreter

interface IMLRepo {
    suspend fun runInference(input: String): Result<Float>
    suspend fun closeInterpreter()
    suspend fun getInterpreter(): Interpreter
}