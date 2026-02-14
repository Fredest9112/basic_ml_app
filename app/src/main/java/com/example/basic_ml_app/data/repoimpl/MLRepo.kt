package com.example.basic_ml_app.data.repoimpl

import android.content.Context
import com.example.basic_ml_app.domain.repo.IMLRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject

class MLRepo @Inject constructor(
    private val context: Context
) : IMLRepo {

    private var interpreter: Interpreter? = null

    private suspend fun loadModelFile(): MappedByteBuffer {
        return withContext(Dispatchers.IO) {
            val assetFileDescriptor = context.assets.openFd("linear.tflite")
            FileInputStream(assetFileDescriptor.fileDescriptor).use { fileInputStream ->
                val fileChannel = fileInputStream.channel
                val startOffset = assetFileDescriptor.startOffset
                val length = assetFileDescriptor.length
                fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
            }
        }
    }

    override suspend fun getInterpreter(): Interpreter {
        return interpreter ?: Interpreter(loadModelFile()).also {
            interpreter = it
        }
    }

    override suspend fun runInference(input: String): Result<Float> {
        return withContext(Dispatchers.Default) {
            try {
                val inputBuffer = arrayOf(floatArrayOf(input.toFloat()))
                val outputBuffer = arrayOf(floatArrayOf(0f))
                interpreter?.run(inputBuffer, outputBuffer)
                Result.success(outputBuffer[0][0])
            } catch (e: Exception) {
                Result.failure(exception = e)
            }
        }
    }

    override suspend fun closeInterpreter() {
        interpreter?.close()
    }
}