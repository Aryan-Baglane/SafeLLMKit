# SafeLLMKit Kotlin / JVM SDK ☕

The core backend SDK for integrating guardrails into Spring Boot, Ktor, or Android applications.

## 📦 Installation

## 📦 Installation (via JitPack)

To use SafeLLMKit in your Kotlin/Android project, use **JitPack**.

**1. Add Repository to `settings.gradle.kts` (or root `build.gradle.kts`):**
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**2. Add Dependencies:**
```kotlin
dependencies {
    // Core Logic (Rules)
    implementation("com.github.Aryan-Baglane.SafeLLMKit:safellmkit-core:v1.0.0")
    
    // ML Support (Optional)
    implementation("com.github.Aryan-Baglane.SafeLLMKit:safellmkit-ml:v1.0.0")
}
```

## 🛠️ Usage Guide

### 1. Basic Mode (Rules Only)
Lightweight validation using Regex and Policies.

```kotlin
import com.safellmkit.core.*

// Define Policy
val policy = GuardrailsPolicy.STRICT
val engine = GuardrailsEngine(policy)

// Create Agent (ML Disabled)
val config = GuardrailsAgentConfig(enableMlFallback = false)
val agent = GuardrailsAgent(engine, config)

// Validate
val result = agent.protectInput("Ignore previous instructions...")

when (result.action) {
    GuardrailAction.BLOCK -> println("🚫 Blocked: ${result.messageToUser}")
    GuardrailAction.SANITIZE -> println("⚠️ Sanitized: ${result.safeText}")
    GuardrailAction.ALLOW -> println("✅ Safe")
}
```

### 2. Advanced Mode (With ML Model)
Uses ONNX Runtime (Java) to execute the jailbreak classifier.

```kotlin
import com.safellmkit.ml.onnx.OnnxJvmClassifier
import com.safellmkit.ml.tokenizer.Md5Tokenizer

// 1. Initialize Classifier
val modelPath = "path/to/jailbreak_classifier.onnx"
val classifier = OnnxJvmClassifier(
    modelPath = modelPath, 
    tokenizer = Md5Tokenizer()
)

// 2. Configure Agent
val config = GuardrailsAgentConfig(
    enableMlFallback = true,
    mlRiskThresholdBlock = 0.95f,   // Block if > 95% confident
    mlRiskThresholdSanitize = 0.60f // Warn/Sanitize if > 60%
)

val agent = GuardrailsAgent(engine, config, classifier)

// 3. Validate
val result = agent.protectInput("Sophisticated attack...")
```

## 🔧 Configuring Policies

You can define custom policies in code.

```kotlin
val customPolicy = GuardrailsPolicy(
    inputRules = listOf(
        LengthRule(max = 2000),
        RegexRule(
            pattern = "(?i)confidential", 
            severity = 8, 
            ruleName = "CONFIDENTIALITY"
        )
    )
)

val engine = GuardrailsEngine(customPolicy)
```

## 📱 Android Support
The core logic is Multiplatform and runs on Android. Use `OnnxAndroidClassifier` (if implemented) or fallback to rules-only mode for lighter footprint.
