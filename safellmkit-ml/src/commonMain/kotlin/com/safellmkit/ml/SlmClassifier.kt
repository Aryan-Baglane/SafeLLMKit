package com.safellmkit.ml

/**
 * Multiplatform SLM inference contract.
 * - Android implementation may use ONNX Runtime Mobile / TFLite
 * - iOS implementation may use CoreML / ONNX Runtime iOS
 * - JVM implementation may use ONNX Runtime Java
 */
interface SlmClassifier {
    suspend fun predict(prompt: String): SlmResult
}
