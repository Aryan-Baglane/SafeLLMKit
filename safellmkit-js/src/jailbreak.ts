import { Rule, GuardrailFinding } from './types';

export class JailbreakRule implements Rule {
    name = "JAILBREAK_HEURISTICS";
    category = "PROMPT_INJECTION";

    // Known patterns of jailbreak/persona injection
    private signals = [
        { pattern: /do anything now/i, weight: 5 },
        { pattern: /DAN/i, weight: 2 }, // Context dependent, low weight
        { pattern: /bypass/i, weight: 3 },
        { pattern: /ignore previous instructions/i, weight: 10 },
        { pattern: /strive to avoid norms/i, weight: 10 }, // STAN
        { pattern: /mongo tom/i, weight: 10 },
        { pattern: /act as/i, weight: 1 },
        { pattern: /unfiltered/i, weight: 4 },
        { pattern: /developer mode/i, weight: 8 },
        { pattern: /jailbreak/i, weight: 5 }
    ];

    check(input: string): GuardrailFinding[] {
        const findings: GuardrailFinding[] = [];
        let score = 0;
        const detected: string[] = [];

        for (const signal of this.signals) {
            if (input.match(signal.pattern)) {
                score += signal.weight;
                detected.push(signal.pattern.source);
            }
        }

        if (score >= 10) {
            findings.push({
                category: this.category,
                rule: this.name,
                severity: 10, // Block
                message: `High confidence jailbreak signal detected (score=${score}): ${detected.join(", ")}`
            });
        } else if (score >= 5) {
            findings.push({
                category: this.category,
                rule: this.name,
                severity: 7, // High risk
                message: `Potential jailbreak signal detected (score=${score}): ${detected.join(", ")}`
            });
        }

        return findings;
    }

    sanitize(input: string): string {
        // We do not sanitize jailbreaks, we usually block them.
        // If specific patterns need removal, we could do it here.
        return input;
    }
}
