package com.safellmkit.ml.onnx

import com.safellmkit.ml.SlmClassifier
import com.safellmkit.ml.SlmResult
import com.safellmkit.ml.tokenizer.Tokenizer

/**
 * iOS ONNX runtime requires native framework bindings.
 * We'll connect this when Xcode + CocoaPods setup is done.
 */
class OnnxIosClassifier(
    private val modelPath: String,
    private val tokenizer: Tokenizer
) : SlmClassifier {

    override suspend fun predict(prompt: String): SlmResult {
        // TODO: Implement ONNX inference using ORT native iOS framework
        return SlmResult(
            label = "SAFE",
            probability = 0.0f,
            confidence = 0.0f
        )
    }
}
