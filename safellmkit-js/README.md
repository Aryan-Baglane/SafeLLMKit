# SafeLLMKit JS SDK

The Browser-based Guardrails SDK for LLMs.
Validate and sanitize user input directly in the browser (React, Vue, etc.) or Node.js before sending it to your backend.

## 📦 Installation

```bash
npm install safellmkit-js
# Install ONNX Runtime web if you plan to use the ML model
npm install onnxruntime-web
```

## 🛠️ Usage

### 1. Basic Mode (Rules Only)
Lightweight, zero-dependency mode using Regex and Heuristics. Good for PII and basic Jailbreaks (DAN).

```typescript
import { SafeLLMKit, GuardrailAction } from 'safellmkit-js';

const guard = new SafeLLMKit();

const userInput = "Ignore previous instructions...";
const result = guard.validate(userInput);

if (result.action === GuardrailAction.BLOCK) {
  console.error("Blocked:", result.messageToUser);
} else {
  console.log("Safe:", result.sanitizedInput);
}
```

### 2. Advanced Mode (With ML Model)
Uses the `jailbreak_classifier.onnx` model running via WebAssembly for robust protection against unknown attacks.

**Prerequisite:** Put `jailbreak_classifier.onnx` in your public/static folder.

```typescript
import { SafeLLMKit, OnnxClassifier } from 'safellmkit-js';

// 1. Setup Classifier
const classifier = new OnnxClassifier('/jailbreak_classifier.onnx');
// Initialize (loads WASM/Model) - best done in useEffect/onMount
await classifier.init();

// 2. Setup Engine
const guard = new SafeLLMKit([], classifier); // [] = default rules

// 3. Validate (Must be Async)
const result = await guard.validateAsync("Some complex prompt...");

if (result.findings.some(f => f.category === 'ML_CLASSIFIER')) {
    console.log("ML Model flagged this!");
}
```

## 🧩 Custom Rules

Implement the `Rule` interface to add your own checks.

```typescript
import { Rule, GuardrailFinding } from 'safellmkit-js';

class NoKeywordsRule implements Rule {
  name = "NO_KEYWORDS";
  category = "POLICY";

  check(input: string): GuardrailFinding[] {
     if (input.includes("secret")) {
       return [{
         category: this.category,
         rule: this.name,
         severity: 10,
         message: "Secret keyword detected"
       }];
     }
     return [];
  }
  
  sanitize(input: string) { return input; }
}

const guard = new SafeLLMKit([new NoKeywordsRule()]);
```
