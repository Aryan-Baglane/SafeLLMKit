package com.safellmkit.ml.onnx

import ai.onnxruntime.*
import com.safellmkit.ml.SlmClassifier
import com.safellmkit.ml.SlmResult
import com.safellmkit.ml.tokenizer.Tokenizer
import java.nio.LongBuffer
import kotlin.math.exp

/**
 * Generic ONNX classifier for JVM using:
 * - input_ids: [1, maxLen]
 * - attention_mask: [1, maxLen]
 *
 * Output expected:
 * - logits: [1, numLabels]  OR probability [1, 1]
 *
 * You must ensure your ONNX model input/output names match.
 */
class OnnxJvmClassifier(
    private val modelPath: String,
    private val tokenizer: Tokenizer,
    private val inputIdsName: String = "input_ids",
    private val attentionMaskName: String = "attention_mask",
    private val outputName: String = "logits",
    private val maxLen: Int = 64,
    private val jailbreakLabelIndex: Int = 1 // assume [SAFE, JAILBREAK]
) : SlmClassifier {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession = env.createSession(modelPath, OrtSession.SessionOptions())

    override suspend fun predict(prompt: String): SlmResult {
        val t = tokenizer.tokenize(prompt, maxLen)

        val shape = longArrayOf(1, maxLen.toLong())

        val inputIdsTensor = OnnxTensor.createTensor(
            env,
            LongBuffer.wrap(t.inputIds),
            shape
        )

        val attentionMaskTensor = OnnxTensor.createTensor(
            env,
            LongBuffer.wrap(t.attentionMask),
            shape
        )

        val inputs = mapOf(
            inputIdsName to inputIdsTensor,
            attentionMaskName to attentionMaskTensor
        )

        val result = session.run(inputs)

        val out = result[0].value

        // Case 1: output = float[][] logits
        val probability = when (out) {
            is Array<*> -> {
                @Suppress("UNCHECKED_CAST")
                val arr = out as Array<FloatArray>
                val logits = arr[0]
                softmax(logits)[jailbreakLabelIndex]
            }
            is FloatArray -> {
                // Case 2: direct probabilities
                if (out.size == 1) out[0] else softmax(out)[jailbreakLabelIndex]
            }
            else -> 0.0f
        }

        val label = if (probability >= 0.5f) "JAILBREAK" else "SAFE"

        inputIdsTensor.close()
        attentionMaskTensor.close()
        result.close()

        return SlmResult(
            label = label,
            probability = probability,
            confidence = probability
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
