package com.safellmkit.ml

/**
 * Default classifier if user doesn't configure ML.
 * Always returns SAFE.
 */
class NoOpSlmClassifier : SlmClassifier {
    override suspend fun predict(prompt: String): SlmResult {
        return SlmResult(
            label = "SAFE",
            probability = 0.0f,
            confidence = 0.0f
        )
    }
}
