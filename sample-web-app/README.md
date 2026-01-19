# SafeLLMKit Web Demo App

A React + Vite application demonstrating the **SafeLLMKit JS SDK** with ONNX integration.

## 🚀 Run Locally

1.  **Install SDK Dependencies:**
    ```bash
    cd ../safellmkit-js
    npm install
    ```

2.  **Install App Dependencies:**
    ```bash
    cd ../sample-web-app
    npm install
    ```

3.  **Start Dev Server:**
    ```bash
    npm run dev
    ```

4.  Open `http://localhost:5173`.

## 🧪 Features
- **Prompt Input**: Enter safe or unsafe prompts.
- **Real-time Analysis**: See Risk Score, Action (Block/Sanitize), and Findings.
- **ML Integration**: Uses `jailbreak_classifier.onnx` (loaded from `public/`).
- **Gemini Integration**: Optionally sends safe prompts to Google Gemini (requires API Key).
