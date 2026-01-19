import { Rule, GuardrailResult, GuardrailAction, GuardrailFinding } from './types';
import { PiiRule } from './pii';
import { JailbreakRule } from './jailbreak';
import { OnnxClassifier } from './onnx';

export class SafeLLMKit {
    private rules: Rule[];
    private classifier?: OnnxClassifier;

    constructor(rules?: Rule[], classifier?: OnnxClassifier) {
        if (!rules || rules.length === 0) {
            this.rules = [
                new PiiRule(),
                new JailbreakRule()
            ];
        } else {
            this.rules = rules;
        }
        this.classifier = classifier;
    }

    public validate(input: string): GuardrailResult {
        return this.runValidation(input, []);
    }

    public async validateAsync(input: string): Promise<GuardrailResult> {
        let mlFindings: GuardrailFinding[] = [];
        if (this.classifier) {
            mlFindings = await this.classifier.classify(input);
        }
        return this.runValidation(input, mlFindings);
    }

    private runValidation(input: string, extraFindings: GuardrailFinding[]): GuardrailResult {
        let sanitized = input;
        const allFindings: GuardrailFinding[] = [...extraFindings];
        let maxSeverity = 0;

        // Check extra findings (ML) for severity
        extraFindings.forEach(f => {
            if (f.severity > maxSeverity) maxSeverity = f.severity;
        });

        // 1. Check & Sanitize loop
        for (const rule of this.rules) {
            const findings = rule.check(input);
            allFindings.push(...findings);

            sanitized = rule.sanitize(sanitized);

            findings.forEach(f => {
                if (f.severity > maxSeverity) {
                    maxSeverity = f.severity;
                }
            });
        }

        // 2. Determine Action
        let action = GuardrailAction.ALLOW;
        if (maxSeverity >= 9) {
            action = GuardrailAction.BLOCK;
        } else if (maxSeverity >= 5) {
            action = GuardrailAction.SANITIZE;
        }

        // 3. Calculate Risk Score
        const riskScore = maxSeverity * 10;

        return {
            action,
            riskScore,
            findings: allFindings,
            sanitizedInput: sanitized
        };
    }
}
