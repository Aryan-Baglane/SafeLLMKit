import { Rule, GuardrailFinding } from './types';

export class PiiRule implements Rule {
    name = "PII_SANITIZER";
    category = "PII";

    private emailRegex = /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/g;
    private phoneRegex = /\b(\+?\d{1,3}[- ]?)?\d{10}\b/g;

    check(input: string): GuardrailFinding[] {
        const findings: GuardrailFinding[] = [];

        if (input.match(this.emailRegex)) {
            findings.push({
                category: this.category,
                rule: this.name,
                severity: 6,
                message: "Email address detected."
            });
        }

        if (input.match(this.phoneRegex)) {
            findings.push({
                category: this.category,
                rule: this.name,
                severity: 6,
                message: "Phone number detected."
            });
        }

        return findings;
    }

    sanitize(input: string): string {
        let safe = input;
        safe = safe.replace(this.emailRegex, "[REDACTED_EMAIL]");
        safe = safe.replace(this.phoneRegex, "[REDACTED_PHONE]");
        return safe;
    }
}
