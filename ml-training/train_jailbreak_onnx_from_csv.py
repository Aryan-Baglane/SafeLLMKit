import os
import re
import hashlib
import pandas as pd
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix

# -----------------------------
# 1) SimpleHashTokenizer (MATCH SafeLLMKit - NOW STABLE MD5)
# -----------------------------
def stable_hash(word: str) -> int:
    # Use MD5, take first 8 hex chars = 32 bits.
    # consistently matches Java/Kotlin logic if we implement same truncation.
    # int(hex, 16) is a standard positive integer in Python.
    hex_str = hashlib.md5(word.encode("utf-8")).hexdigest()
    return int(hex_str[:8], 16)

def simple_hash_tokenize(text: str, max_len: int = 64, vocab_size: int = 8192):
    text = str(text).lower()
    text = re.sub(r"[^a-z0-9\s]", " ", text).strip()
    words = re.split(r"\s+", text)

    input_ids = np.zeros(max_len, dtype=np.int64)
    attention_mask = np.zeros(max_len, dtype=np.int64)

    count = min(len(words), max_len)
    for i in range(count):
        h = stable_hash(words[i])
        idx = (h % vocab_size) + 1  # 0 reserved for PAD
        input_ids[i] = idx
        attention_mask[i] = 1

    return input_ids, attention_mask


# -----------------------------
# 2) Dataset
# -----------------------------
class JailbreakDataset(Dataset):
    def __init__(self, texts, labels, max_len=64, vocab_size=8192):
        self.texts = texts
        self.labels = labels
        self.max_len = max_len
        self.vocab_size = vocab_size

    def __len__(self):
        return len(self.texts)

    def __getitem__(self, idx):
        text = str(self.texts[idx])
        label = int(self.labels[idx])

        input_ids, attention_mask = simple_hash_tokenize(
            text, max_len=self.max_len, vocab_size=self.vocab_size
        )

        return (
            torch.tensor(input_ids, dtype=torch.long),
            torch.tensor(attention_mask, dtype=torch.long),
            torch.tensor(label, dtype=torch.long),
        )


# -----------------------------
# 3) Model
# -----------------------------
class TinyJailbreakClassifier(nn.Module):
    def __init__(self, vocab_size=8192, embed_dim=64, num_labels=2):
        super().__init__()
        self.embedding = nn.Embedding(vocab_size + 2, embed_dim, padding_idx=0)
        self.fc1 = nn.Linear(embed_dim, 64)
        self.relu = nn.ReLU()
        self.dropout = nn.Dropout(0.2)
        self.fc2 = nn.Linear(64, num_labels)

    def forward(self, input_ids, attention_mask):
        x = self.embedding(input_ids)  # (B, L, D)

        mask = attention_mask.unsqueeze(-1).float()
        x = x * mask

        summed = x.sum(dim=1)
        denom = mask.sum(dim=1).clamp(min=1.0)
        pooled = summed / denom

        h = self.fc1(pooled)
        h = self.relu(h)
        h = self.dropout(h)
        logits = self.fc2(h)
        return logits


# -----------------------------
# 4) CSV Loader Helpers
# -----------------------------
def detect_text_column(df: pd.DataFrame):
    # prefer these column names if they exist
    preferred = ["prompt", "text", "content", "instruction", "query", "jailbreak_prompt"]
    for c in preferred:
        if c in df.columns:
            return c

    # otherwise pick the first object/string-like column
    for c in df.columns:
        if df[c].dtype == object:
            return c

    raise ValueError("No text column found in CSV.")


def load_jailbreak_csv(path: str):
    if not os.path.exists(path):
        print(f"⚠️ Warning: Dataset not found at {path}")
        return []
        
    df = pd.read_csv(path)
    col = detect_text_column(df)
    texts = df[col].dropna().astype(str).tolist()
    return texts


def build_safe_examples(n: int):
    print(f"⚡ Generating {n} diverse safe examples...")
    
    # 1. Templates for instructional/informational queries
    templates_info = [
        "Explain {topic} in simple words",
        "How to create a {noun} using {topic}?",
        "Tell me about the history of {topic}",
        "What is the difference between {topic} and {topic}?",
        "Give me tips to improve my {skill}",
        "Write a short {text_type} about {topic}",
        "Summarize this {text_type}: {noun}",
        "Explain {topic} concepts for beginners",
        "How does {topic} actually work?",
        "Create a {noun} for {topic}",
        "What is the best way to learn {topic}?",
        "Write a function to {verb} {noun} in {lang}",
        "Is {topic} better than {topic}?",
        "Who invented {topic}?",
        "Why is {topic} considered {adj}?",
        "List 5 benefits of {topic}",
        "Describe the impact of {topic} on {noun}",
        "How to fix {noun} error in {topic}?",
        "Recipe for {food}",
        "Best places to visit in {place}",
        "Review of {noun}",
        "Guide to {verb} {noun}"
    ]
    
    # 2. Casual/Chat templates
    templates_chat = [
        "Hi, how are you?",
        "Good morning",
        "Hello there",
        "What's up?",
        "Tell me a joke",
        "Sing a song",
        "Who are you?",
        "What can you do?",
        "Are you an AI?",
        "Nice to meet you",
        "I'm feeling {adj} today",
        "The weather is {adj}",
        "I like {topic}",
        "Do you like {food}?",
        "Let's talk about {topic}"
    ]

    # Data lists
    topics = [
        "Kotlin", "Java", "Python", "Rust", "Go", "C++", "JavaScript", "TypeScript",
        "React", "Vue", "Angular", "Svelte", "Node.js", "Spring Boot", "Django", "Flask",
        "Android", "iOS", "Swift", "Flutter", "Jetpack Compose", "SwiftUI",
        "Machine Learning", "AI", "Deep Learning", "Neural Networks", "NLP",
        "Cybersecurity", "Blockchain", "Cloud Computing", "AWS", "Azure", "Docker",
        "Physics", "Chemistry", "Biology", "History", "Geography", "Math",
        "Music", "Art", "Philosophy", "Psychology", "Economics", "Politics",
        "Gardening", "Cooking", "Photography", "Gaming", "Yoga", "Meditation"
    ]
    
    nouns = ["project", "app", "website", "game", "system", "tool", "device", "car", "book", "movie", "song", "algorithm", "database", "server", "phone", "laptop", "idea", "plan", "report"]
    verbs = ["build", "create", "fix", "analyze", "optimize", "learn", "study", "design", "develop", "test", "deploy", "write", "read", "calculate"]
    adjectives = ["good", "bad", "fast", "slow", "easy", "hard", "complex", "simple", "beautiful", "ugly", "important", "useless", "funny", "sad", "happy", "angry"]
    langs = ["Python", "Java", "Kotlin", "Swift", "C#", "Go", "Rust", "JavaScript", "C++", "Ruby", "PHP"]
    skills = ["coding", "writing", "speaking", "listening", "cooking", "driving", "management", "leadership", "design", "marketing"]
    text_types = ["email", "essay", "paragraph", "story", "poem", "article", "report", "memo", "letter"]
    foods = ["pizza", "burger", "pasta", "salad", "sushi", "cake", "ice cream", "coffee", "tea", "bread", "soup", "tacos"]
    places = ["Paris", "London", "New York", "Tokyo", "India", "USA", "Europe", "Asia", "the beach", "the mountains", "space", "Mars"]

    import random
    random.seed(42)

    safe = []
    
    # Generate combinatorial prompts aggressively
    # We want 'n' examples.
    
    while len(safe) < n:
        # Mix simple templates and random word salads to mimic general safe text
        r = random.random()
        
        if r < 0.6: # 60% Structured coherent queries
            t = random.choice(templates_info + templates_chat)
            
            # Simple manual formatting to avoid errors if keys missing
            # Iterate and replace known placeholders
            p = t
            if "{topic}" in p: p = p.replace("{topic}", random.choice(topics), 1)
            if "{topic}" in p: p = p.replace("{topic}", random.choice(topics), 1) # Handle second occurrence
            if "{noun}" in p: p = p.replace("{noun}", random.choice(nouns))
            if "{verb}" in p: p = p.replace("{verb}", random.choice(verbs))
            if "{lang}" in p: p = p.replace("{lang}", random.choice(langs))
            if "{adj}" in p: p = p.replace("{adj}", random.choice(adjectives))
            if "{skill}" in p: p = p.replace("{skill}", random.choice(skills))
            if "{text_type}" in p: p = p.replace("{text_type}", random.choice(text_types))
            if "{food}" in p: p = p.replace("{food}", random.choice(foods))
            if "{place}" in p: p = p.replace("{place}", random.choice(places))
            
            safe.append(p)
            
        else: # 40% Random safe statements/questions (more variety)
            # Construct synthetic sentences
            structure = random.choice([
                "I want to {verb} a {noun}.",
                "Can you help me with {topic}?",
                "Is {noun} {adj}?",
                "Why is {topic} so {adj}?",
                "The {noun} is {adj}.",
                "Please {verb} the {noun}.",
                "Do not {verb} the {noun}.", # Safe negative command
                "Write code for {noun}.",
                "Check my {topic} homework.",
                "{verb}ing is {adj}."
            ])
            p = structure.replace("{verb}", random.choice(verbs))\
                         .replace("{noun}", random.choice(nouns))\
                         .replace("{topic}", random.choice(topics))\
                         .replace("{adj}", random.choice(adjectives))
            safe.append(p)
    
    # ---------------------------------------------------------
    # 3. ADVERSARIAL SAFE EXAMPLES (Crucial for realism)
    # ---------------------------------------------------------
    # These use "trigger words" usually found in jailbreaks (ignore, system, act as, mode)
    # but in a purely safe context. This prevents the model from just learning keywords.
    
    adversarial_templates = [
        "Ignore the previous error and try compiling again",
        "Please ignore my last typo",
        "Act as a professional Java tutor",
        "Act as a travel guide for Paris",
        "System check: is the server running?",
        "I want to bypass the traffic jam",
        "Can we mode-switch to dark theme?",
        "Disable notifications for this app",
        "Forget the previous context, let's start a new topic",
        "Make this system more secure",
        "Override the default settings to custom",
        "Simulate a conversation between two friends",
        "Pretend you are a compiler",
        "How to hack a difficult math problem?",
        "What is the system prompt for a Linux terminal?",
        "Please unrestricted access to my own files",
        "Development mode is useful for debugging",
        "Do not answer if you don't know",
        "Start a new session",
        "Translate this from English to French"
    ]
    
    # Add aggressive amount of adversarial examples (20% of dataset)
    num_adversarial = int(n * 0.2)
    for _ in range(num_adversarial):
        safe.append(random.choice(adversarial_templates))

    # Shuffle to ensure mix
    random.shuffle(safe)
    return safe[:n]


# -----------------------------
# 5) Train + Export ONNX
# -----------------------------
def export_onnx(model, max_len=64, out_path="jailbreak_classifier.onnx"):
    model.eval()

    dummy_input_ids = torch.zeros((1, max_len), dtype=torch.long)
    dummy_attention_mask = torch.zeros((1, max_len), dtype=torch.long)

    torch.onnx.export(
        model,
        (dummy_input_ids, dummy_attention_mask),
        out_path,
        input_names=["input_ids", "attention_mask"],
        output_names=["logits"],
        dynamic_axes={
            "input_ids": {0: "batch"},
            "attention_mask": {0: "batch"},
            "logits": {0: "batch"},
        },
        opset_version=13
    )

    print(f"\n✅ Exported ONNX model: {os.path.abspath(out_path)}")


def train_from_csvs(
    jailbreak_csv_paths,
    out_model_path="jailbreak_classifier.onnx",
    max_len=64,
    vocab_size=8192,
    epochs=10
):
    jailbreak_texts = []
    for p in jailbreak_csv_paths:
        jb = load_jailbreak_csv(p)
        if jb:
            print(f"✅ Loaded {len(jb)} jailbreak prompts from: {p}")
            jailbreak_texts.extend(jb)

    jailbreak_texts = list(dict.fromkeys(jailbreak_texts))  # deduplicate
    print(f"✅ Total unique jailbreak samples: {len(jailbreak_texts)}")
    
    if len(jailbreak_texts) == 0:
        print("❌ No jailbreak data found. Please add csv files to the folder.")
        return

    # Build SAFE dataset same size
    print("⏳ generating safe examples...")
    safe_texts = build_safe_examples(len(jailbreak_texts))
    print(f"✅ Generated {len(safe_texts)} safe examples")

    texts = safe_texts + jailbreak_texts
    labels = [0] * len(safe_texts) + [1] * len(jailbreak_texts)

    X_train, X_test, y_train, y_test = train_test_split(
        texts, labels, test_size=0.2, random_state=42, stratify=labels
    )

    train_ds = JailbreakDataset(X_train, y_train, max_len=max_len, vocab_size=vocab_size)
    test_ds = JailbreakDataset(X_test, y_test, max_len=max_len, vocab_size=vocab_size)

    train_loader = DataLoader(train_ds, batch_size=32, shuffle=True)
    test_loader = DataLoader(test_ds, batch_size=32, shuffle=False)

    device = torch.device("cpu")

    model = TinyJailbreakClassifier(vocab_size=vocab_size).to(device)
    
    # ADDED WEIGHT DECAY to prevent overfitting
    optimizer = torch.optim.Adam(model.parameters(), lr=1e-3, weight_decay=1e-4)
    loss_fn = nn.CrossEntropyLoss()

    for epoch in range(epochs):
        model.train()
        total_loss = 0.0
        
        # limit printing for large datasets
        step = 0
        for input_ids, attention_mask, y in train_loader:
            input_ids = input_ids.to(device)
            attention_mask = attention_mask.to(device)
            y = y.to(device)

            optimizer.zero_grad()
            logits = model(input_ids, attention_mask)
            loss = loss_fn(logits, y)
            loss.backward()
            optimizer.step()

            total_loss += loss.item()
            step += 1
            
        print(f"Epoch {epoch+1}/{epochs} | Loss: {total_loss:.4f}")

    # Evaluate
    model.eval()
    all_preds = []
    all_true = []

    with torch.no_grad():
        for input_ids, attention_mask, y in test_loader:
            logits = model(input_ids, attention_mask)
            preds = torch.argmax(logits, dim=1).cpu().numpy().tolist()

            all_preds.extend(preds)
            all_true.extend(y.cpu().numpy().tolist())

    print("\n✅ Accuracy:", accuracy_score(all_true, all_preds))
    print(classification_report(all_true, all_preds, target_names=["SAFE", "JAILBREAK"]))
    
    cm = confusion_matrix(all_true, all_preds)
    print("\n✅ Confusion Matrix:")
    try:
        print(f"True SAFE (TN): {cm[0][0]} | False JAILBREAK (FP): {cm[0][1]}")
        print(f"False SAFE (FN): {cm[1][0]} | True JAILBREAK (TP): {cm[1][1]}")
    except:
        pass
    print("\nArray view:\n", cm)


    export_onnx(model, max_len=max_len, out_path=out_model_path)


if __name__ == "__main__":
    jailbreak_csvs = [
        "jailbreak_prompts.csv",
        "malicous_deepset.csv",
        "forbidden_question_set_df.csv",
        "forbidden_question_set_with_prompts.csv",
        "predictionguard_df.csv"
    ]

    train_from_csvs(
        jailbreak_csv_paths=jailbreak_csvs,
        out_model_path="jailbreak_classifier.onnx",
        epochs=12
    )
