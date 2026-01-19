package com.safellmkit.ml.onnx

import ai.onnxruntime.*
import com.safellmkit.ml.SlmClassifier
import com.safellmkit.ml.SlmResult
import com.safellmkit.ml.tokenizer.Tokenizer
import java.nio.LongBuffer
import kotlin.math.exp

class OnnxAndroidClassifier(
    private val modelPath: String,
    private val tokenizer: Tokenizer,
    private val inputIdsName: String = "input_ids",
    private val attentionMaskName: String = "attention_mask",
    private val outputName: String = "logits",
    private val maxLen: Int = 64,
    private val jailbreakLabelIndex: Int = 1
) : SlmClassifier {

    private val env = OrtEnvironment.getEnvironment()
    private val session = env.createSession(modelPath, OrtSession.SessionOptions())

    override suspend fun predict(prompt: String): SlmResult {
        val t = tokenizer.tokenize(prompt, maxLen)
        val shape = longArrayOf(1, maxLen.toLong())

        val idsTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(t.inputIds), shape)
        val maskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(t.attentionMask), shape)

        val result = session.run(mapOf(inputIdsName to idsTensor, attentionMaskName to maskTensor))
        val out = result[0].value

        val prob = when (out) {
            is Array<*> -> {
                @Suppress("UNCHECKED_CAST")
                val arr = out as Array<FloatArray>
                val logits = arr[0]
                softmax(logits)[jailbreakLabelIndex]
            }
            else -> 0.0f
        }

        idsTensor.close()
        maskTensor.close()
        result.close()

        return SlmResult(
            label = if (prob >= 0.5f) "JAILBREAK" else "SAFE",
            probability = prob,
            confidence = prob
        )
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        val exps = FloatArray(logits.size)
        var sum = 0.0
        for (i in logits.indices) {
            val e = exp((logits[i] - max).toDouble())
            exps[i] = e.toFloat()
            sum += e
        }
        for (i in exps.indices) exps[i] = (exps[i] / sum).toFloat()
        return exps
    }
}
