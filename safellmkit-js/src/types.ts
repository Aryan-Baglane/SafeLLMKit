export enum GuardrailAction {
    ALLOW = "ALLOW",
    SANITIZE = "SANITIZE",
    BLOCK = "BLOCK"
}

export interface GuardrailFinding {
    category: string;
    rule: string;
    severity: number; // 1..10
    message: string;
    details?: any;
}

export interface GuardrailResult {
    action: GuardrailAction;
    riskScore: number; // 0..100
    findings: GuardrailFinding[];
    sanitizedInput: string;
}

export interface Rule {
    name: string;
    category: string;
    check(input: string): GuardrailFinding[];
    sanitize(input: string): string;
}
