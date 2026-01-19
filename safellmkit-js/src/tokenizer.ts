import { md5 } from './md5';

export class Md5Tokenizer {
    constructor(private vocabSize: number = 8192) { }

    tokenize(text: string, maxLen: number): BigInt64Array {
        const lower = text.toLowerCase();
        // Remove special chars (keep alphanumeric and whitespace)
        const cleaned = lower.replace(/[^a-z0-9\s]/g, '');
        const words = cleaned.split(/\s+/).filter(w => w.length > 0);

        const tokens = new BigInt64Array(maxLen); // Initialized to 0n
        const count = Math.min(words.length, maxLen);

        for (let i = 0; i < count; i++) {
            const hash = md5(words[i]);
            // Take first 8 chars (32 bits)
            const truncated = hash.substring(0, 8);
            // Parse as unsigned integer
            const intVal = parseInt(truncated, 16);
            // Modulo vocab size + 1 (1-based index)
            const token = (intVal % this.vocabSize) + 1;
            tokens[i] = BigInt(token);
        }
        return tokens;
    }
}
