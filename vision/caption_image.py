from transformers import AutoProcessor, AutoModelForCausalLM
from PIL import Image

processor = AutoProcessor.from_pretrained("microsoft/git-base-coco")
model = AutoModelForCausalLM.from_pretrained("microsoft/git-base-coco")

image = Image.open("/app/vision/input.jpg")

pixel_values = processor(images=image, return_tensors="pt").pixel_values

generated_ids = model.generate(pixel_values=pixel_values, max_length=50)
generated_caption = processor.batch_decode(generated_ids, skip_special_tokens=True)[0]
print(generated_caption)

f = open("/app/vision/result.txt", "w")
f.write(generated_caption)
f.close()