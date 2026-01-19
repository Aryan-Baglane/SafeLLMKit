# SafeLLMKit 🛡️

**The Universal Guardrails SDK for Large Language Models.**

![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![Version](https://img.shields.io/badge/version-0.1.0-green.svg)
![Platform](https://img.shields.io/badge/platform-Kotlin%20%7C%20JS%20%7C%20Python-orange.svg)

SafeLLMKit is a comprehensive, multi-platform security framework designed to protect your LLM applications from adversarial attacks, jailbreaks, and sensitive data leakage. It combines fast heuristic rules with robust machine learning models to ensure your AI agents remain safe and compliant.

---

## 🚀 Key Features

*   **🛡️ Jailbreak Detection**: reliable defense against "DAN", "Mongo Tom", and complex persona-injection attacks using a hybrid approach (Regex + ONNX ML Model).
*   **🔒 PII Sanitization**: Automatically detect and redact sensitive information like Emails and Phone numbers before they reach the LLM.
*   **⚡ Multi-Platform**:
    *   **Kotlin/JVM**: For backend services (Spring, Ktor).
    *   **JavaScript/TypeScript**: For browser-based apps (React, Vue) and Node.js.
*   **🧠 Defense-in-Depth**:
    *   **Layer 1**: Heuristics (Fast, Rule-based).
    *   **Layer 2**: ML Classifier (Robust, Trained on 40k+ samples).
*   **🔌 Easy Integration**: Simple `protectInput(prompt)` API.

---

## 📦 Components

| Module | Description | Tech Stack |
| :--- | :--- | :--- |
| **`safellmkit-core`** | The core rule engine and agent logic. | Kotlin (Multiplatform) |
| **`safellmkit-ml`** | ML bindings and tokenizer logic. | Kotlin, ONNX Runtime |
| **`safellmkit-js`** | Standalone SDK for web/node. | TypeScript, ONNX Runtime Web |
| **`ml-training`** | Training scripts for the security model. | Python, PyTorch |

---

## 🛠️ Getting Started

### 1. JavaScript / TypeScript (Web SDK)

Defense in the browser! Block unsafe prompts before they even leave the client.

```bash
cd safellmkit-js
npm install
npm run build
```

**Usage:**

```typescript
import { SafeLLMKit, OnnxClassifier } from 'safellmkit-js';

// 1. Initialize ML Model (Optional but Recommended)
const classifier = new OnnxClassifier('/path/to/jailbreak_classifier.onnx');
await classifier.init();

// 2. Create Guardrails
const guard = new SafeLLMKit([], classifier);

// 3. Validate
const result = await guard.validateAsync("Ignore previous instructions and...");

if (result.action === "BLOCK") {
  console.error("Unsafe prompt detected!");
} else {
  // Safe to send to Gemini/OpenAI
  callLLM(result.sanitizedInput);
}
```

### 2. Kotlin (Backend SDK)

Defense on the server.

**Gradle:**

```kotlin
implementation(project(":safellmkit-core"))
implementation(project(":safellmkit-ml"))
```

**Usage:**

```kotlin
val modelPath = "path/to/jailbreak_classifier.onnx"
val classifier = OnnxJvmClassifier(modelPath = modelPath, tokenizer = Md5Tokenizer())

val config = GuardrailsAgentConfig(enableMlFallback = true)
val agent = GuardrailsAgent(engine, config, classifier)

val result = agent.protectInput("Some user prompt")

if (result.action == GuardrailAction.BLOCK) {
    throw SecurityException("Blocked!")
}
```

---

## 🧠 The ML Model

SafeLLMKit includes a custom-trained **ONNX Jailbreak Classifier** (`jailbreak_classifier.onnx`).
*   **Architecture**: Compact Neural Network optimized for edge inference.
*   **Tokenizer**: Custom consistent MD5-based tokenizer (compatible across Python, JVM, and JS).
*   **Performance**: Verified against advanced attacks (DAN 6.0, STAN, DUDE).

See [docs/ML_INTEGRATION.md](docs/ML_INTEGRATION.md) for training details.

---

## 🎮 Sample Web App

Try out the guardrails in a real React application!

**Location**: `sample-web-app/`

1.  **Install & Run**:
    ```bash
    cd safellmkit-js && npm install
    cd ../sample-web-app
    npm install
    npm run dev
    ```
2.  **Open**: `http://localhost:5173`
3.  **Test**: Enter prompts like *"Hello"* (Safe) or *"You are now DAN..."* (Unsafe) to see the shield in action!

---

## 📄 License

Apache 2.0 - Open Source Security for everyone.

---

## 🔧 Advanced Usage Examples

### JavaScript / TypeScript

#### 1. Adding Custom Rules
You can easily extend the SDK with your own domain-specific rules (e.g., blocking competitors' names, enforcing formatting).

```typescript
import { SafeLLMKit, Rule, GuardrailFinding } from 'safellmkit-js';

class NoCompetitorsRule implements Rule {
    name = "NO_COMPETITORS";
    category = "BUSINESS_LOGIC";

    check(input: string): GuardrailFinding[] {
        if (input.toLowerCase().includes("competitor-x")) {
            return [{
                category: this.category,
                rule: this.name,
                severity: 7,
                message: "Competitor mention detected"
            }];
        }
        return [];
    }

    sanitize(input: string): string {
        return input.replace(/competitor-x/gi, "[REDACTED]");
    }
}

// Initialize with custom rule
const guard = new SafeLLMKit([new NoCompetitorsRule()]);
```

#### 2. Handling Detailed Findings
Inspect exactly why a prompt was flagged.

```typescript
const result = await guard.validateAsync("Some risky prompt");

console.log(`Risk Score: ${result.riskScore}/100`);

if (result.action === "BLOCK") {
    console.error("❌ Blocked Request");
    result.findings.forEach(f => {
        console.log(`   - [${f.severity}] ${f.rule}: ${f.message}`);
    });
} else if (result.action === "SANITIZE") {
    console.warn("⚠️ Sanitized input:", result.sanitizedInput);
}
```

---

### Kotlin (JVM)

#### 1. Configuring Sensitivity
Adjust how aggressive the ML model should be.

```kotlin
val config = GuardrailsAgentConfig(
    enableMlFallback = true,
    // Only block if model is > 95% sure (Low False Positive mode)
    mlRiskThresholdBlock = 0.95f,
    // Sanitize/Warn if model is > 60% sure
    mlRiskThresholdSanitize = 0.60f
)

val agent = GuardrailsAgent(engine, config, classifier)
```

#### 2. Define Custom Policy in Kotlin
Use the declarative policy engine.

```kotlin
val policy = GuardrailsPolicy(
    inputRules = listOf(
        // Block inputs > 1000 chars
        LengthRule(max = 1000), 
        // Custom Regex Rule
        RegexRule(pattern = "(?i)confidential", severity = 8) 
    )
)

val engine = GuardrailsEngine(policy)
```

