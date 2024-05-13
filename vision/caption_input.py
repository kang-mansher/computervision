from transformers import AutoProcessor, AutoModelForCausalLM
from PIL import Image
import torch
import sys

processor = AutoProcessor.from_pretrained("microsoft/git-base-textvqa")
model = AutoModelForCausalLM.from_pretrained("microsoft/git-base-textvqa")

image = Image.open("/tmp/vision/input.jpg").convert("RGB")

pixel_values = processor(images=image, return_tensors="pt").pixel_values

question = sys.argv[1]

input_ids = processor(text=question, add_special_tokens=False).input_ids
input_ids = [processor.tokenizer.cls_token_id] + input_ids
input_ids = torch.tensor(input_ids).unsqueeze(0)

generated_ids = model.generate(pixel_values=pixel_values, input_ids=input_ids, max_length=50)
generated_caption = processor.batch_decode(generated_ids, skip_special_tokens=True)
print(generated_caption)

f = open("/tmp/vision/result.txt", "w")
f.write(generated_caption[0])
f.close()