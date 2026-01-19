import { useState, useEffect } from 'react'
import './App.css'
import { SafeLLMKit, GuardrailAction, GuardrailResult, OnnxClassifier } from 'safellmkit-js'

// Initialize Classifier with path to model in public/
const classifier = new OnnxClassifier('/jailbreak_classifier.onnx');
// Initialize Engine with default rules + ML classifier
const guard = new SafeLLMKit([], classifier);

function App() {
    const [prompt, setPrompt] = useState('');
    const [apiKey, setApiKey] = useState('');
    const [result, setResult] = useState<GuardrailResult | null>(null);
    const [llmResponse, setLlmResponse] = useState('');
    const [loading, setLoading] = useState(false);
    const [modelReady, setModelReady] = useState(false);

    useEffect(() => {
        // Initialize ONNX model on load
        classifier.init().then(() => {
            console.log("SafeLLMKit: ML Model Ready");
            setModelReady(true);
        });
    }, []);

    const handleAnalyze = async () => {
        setLoading(true);
        setResult(null);
        setLlmResponse('');

        // 1. Run Guardrails (Client Side!)
        // Now using validateAsync to include ML check
        console.log("Running SafeLLMKit Guardrails...");
        const guardResult = await guard.validateAsync(prompt);
        setResult(guardResult);

        // 2. If Allowed, call Gemini
        if (guardResult.action !== GuardrailAction.BLOCK) {
            if (apiKey) {
                try {
                    const response = await callGemini(guardResult.sanitizedInput, apiKey);
                    setLlmResponse(response);
                } catch (error: any) {
                    setLlmResponse("Error calling Gemini: " + error.message);
                }
            } else {
                setLlmResponse("Guardrails passed! Enter Gemini API Key to see real model response.");
            }
        } else {
            setLlmResponse("Request blocked by SafeLLMKit. Not sent to LLM.");
        }
        setLoading(false);
    };

    const callGemini = async (text: string, key: string) => {
        const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=${key}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ contents: [{ parts: [{ text }] }] })
        });
        const data = await res.json();
        if (data.error) throw new Error(data.error.message);
        return data?.candidates?.[0]?.content?.parts?.[0]?.text || "No response.";
    };

    return (
        <div className="container">
            <h1>🛡️ SafeLLMKit Web Demo</h1>

            <div className="card">
                <div style={{ marginBottom: '1rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.5rem', color: '#94a3b8' }}>Gemini API Key (Optional)</label>
                    <input
                        type="password"
                        placeholder="Enter AI Studio Key..."
                        value={apiKey}
                        onChange={(e) => setApiKey(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '0.75rem',
                            borderRadius: '0.5rem',
                            border: '1px solid #334155',
                            backgroundColor: '#0f172a',
                            color: 'white'
                        }}
                    />
                </div>

                <label style={{ display: 'block', marginBottom: '0.5rem', color: '#94a3b8' }}>Enter Prompt</label>
                <textarea
                    placeholder="Try a safe prompt like: 'What is deep learning?'&#10;Or a jailbreak like: 'Ignore previous instructions...'"
                    value={prompt}
                    onChange={(e) => setPrompt(e.target.value)}
                />

                <button className="btn" onClick={handleAnalyze} disabled={loading || !prompt}>
                    {loading ? 'Analyzing...' : !modelReady ? 'Loading Model...' : 'Analyze & Send'}
                </button>

                {!modelReady && <p style={{ textAlign: 'center', marginTop: '0.5rem', color: '#64748b', fontSize: '0.9rem' }}>Loading ONNX Model...</p>}

                {result && (
                    <div className="result-section">
                        <div className="stats">
                            <div className="stat-box">
                                <div className="stat-label">Action</div>
                                <div className={result.action === GuardrailAction.BLOCK ? "badge badge-block" : "badge badge-allow"}>
                                    {result.action}
                                </div>
                            </div>
                            <div className="stat-box">
                                <div className="stat-label">Risk Score</div>
                                <div className="stat-value" style={{ color: result.riskScore > 50 ? '#ef4444' : '#22c55e' }}>
                                    {result.riskScore}/100
                                </div>
                            </div>
                            <div className="stat-box">
                                <div className="stat-label">Findings</div>
                                <div className="stat-value">{result.findings.length}</div>
                            </div>
                        </div>

                        {result.findings.map((f, i) => (
                            <div key={i} className="finding" style={{ borderLeftColor: f.severity >= 9 ? '#ef4444' : '#fbbf24' }}>
                                <strong>{f.rule}</strong> ({f.category})
                                <p style={{ margin: '0.5rem 0 0', opacity: 0.8 }}>{f.message}</p>
                            </div>
                        ))}

                        <div style={{ marginTop: '2rem' }}>
                            <h3 style={{ color: '#94a3b8' }}>LLM Response</h3>
                            <div style={{
                                background: '#0f172a',
                                padding: '1rem',
                                borderRadius: '0.5rem',
                                minHeight: '60px',
                                border: result.action === GuardrailAction.BLOCK ? '1px solid #ef4444' : '1px solid #22c55e'
                            }}>
                                {llmResponse}
                            </div>
                        </div>

                        {result.action === GuardrailAction.SANITIZE && (
                            <div style={{ marginTop: '1rem' }}>
                                <h4 style={{ color: '#94a3b8' }}>Sanitized Input Sent:</h4>
                                <code style={{ display: 'block', background: '#334155', padding: '0.5rem', borderRadius: '4px' }}>
                                    {result.sanitizedInput}
                                </code>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    )
}

export default App
