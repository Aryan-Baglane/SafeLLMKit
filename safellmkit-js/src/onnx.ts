import * as ort from 'onnxruntime-web';
import { Md5Tokenizer } from './tokenizer';
import { GuardrailFinding } from './types';

export class OnnxClassifier {
    private session: ort.InferenceSession | null = null;
    private tokenizer: Md5Tokenizer;

    constructor(private modelPath: string = '/jailbreak_classifier.onnx') {
        this.tokenizer = new Md5Tokenizer(8192);
        // Default WASM path config might be needed depending on bundler
        // ort.env.wasm.wasmPaths = "https://cdn.jsdelivr.net/npm/onnxruntime-web/dist/";
    }

    async init() {
        if (this.session) return;
        try {
            this.session = await ort.InferenceSession.create(this.modelPath, {
                executionProviders: ['wasm']
            });
            console.log("SafeLLMKit: ONNX Model loaded successfully");
        } catch (e) {
            console.error("SafeLLMKit: Failed to load ONNX model from " + this.modelPath, e);
        }
    }

    async classify(text: string): Promise<GuardrailFinding[]> {
        if (!this.session) {
            // Try init if not ready? Or just skip
            return [];
        }

        const maxLen = 64;
        const tokens = this.tokenizer.tokenize(text, maxLen);

        try {
            const tensor = new ort.Tensor('int64', tokens, [1, maxLen]);
            const feeds = { input: tensor };
            const results = await this.session.run(feeds);
            const output = results.output; // Float32 tensor
            const riskScore = output.data[0] as number; // probability

            if (riskScore > 0.9) {
                return [{
                    category: "ML_CLASSIFIER",
                    rule: "ONNX_JAILBREAK_DETECTED",
                    severity: 10,
                    message: `ML Model detected jailbreak with confidence ${(riskScore * 100).toFixed(1)}%`
                }];
            }
        } catch (e) {
            console.error("SafeLLMKit: Inference failed", e);
        }
        return [];
    }
}
