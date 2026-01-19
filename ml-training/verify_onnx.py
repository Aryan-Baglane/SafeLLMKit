import onnxruntime as ort
import numpy as np
import sys

try:
    sess = ort.InferenceSession("jailbreak_classifier.onnx")
    print("✅ Model loaded successfully")
    
    input_ids = np.zeros((1,64), dtype=np.int64)
    attention_mask = np.zeros((1,64), dtype=np.int64)
    
    out = sess.run(["logits"], {"input_ids": input_ids, "attention_mask": attention_mask})
    print("✅ Inference successful, output shape:", out[0].shape)
except Exception as e:
    print("❌ Failed to load/run model:", e)
    sys.exit(1)
