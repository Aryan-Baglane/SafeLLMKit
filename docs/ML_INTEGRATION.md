# SafeLLMKit ML Integration

## Overview
SafeLLMKit now includes an embedded ONNX model for detecting jailbreak attempts with high accuracy. This model runs locally on-device (JVM/Android) without external API calls.

## How it works
1. **Tokenizer**: Uses a stable MD5-based hashing tokenizer (`Md5Tokenizer`) to map words to integer IDs. Consistent across Python training and Kotlin inference.
2. **Model**: A lightweight ONNX classifier (`jailbreak_classifier.onnx`) loaded via Microsoft ONNX Runtime.
3. **Agent Integration**: The `GuardrailsAgent` can be configured to check prompts against this model.

## Configuration
To enable the ML check:
```kotlin
val config = GuardrailsAgentConfig(
    enableMlCheck = true,
    mlThreshold = 0.9f, // Block if model is >90% confident
    blockOnMlJailbreak = true
)

val engine = GuardrailsEngine(policies)
val classifier = OnnxJvmClassifier(
    modelPath = "/path/to/jailbreak_classifier.onnx", // Or load from assets
    tokenizer = Md5Tokenizer()
)

val agent = GuardrailsAgent(engine, classifier, config)
```

## Training
The model training script is located in `ml-training/train_jailbreak_onnx_from_csv.py`.
To retrain:
```bash
cd ml-training
source .venv/bin/activate
python train_jailbreak_onnx_from_csv.py
```
This will generate a new `jailbreak_classifier.onnx`.

## Supported Platforms
- **JVM**: Fully supported.
- **Android**: Supported (requires copying `.onnx` to assets).
- **iOS**: Pending native integration (stub implementation exists).
