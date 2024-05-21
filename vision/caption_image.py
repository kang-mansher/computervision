import base64
import openai

openai.api_key = "dummy"

def encode_image(image_path):
    with open(image_path, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode('utf-8')

image_path = "/app/vision/input.jpg"

base64_image = encode_image(image_path)

conversation_history = [
    {
        "role": "user",
        "content": [
            {
                "type": "text",
                "text": "Generate a caption for the image in 6 words or less"
            },
            {
                "type": "image_url",
                "image_url": {
                    "url": f"data:image/jpeg;base64,{base64_image}"
                }
            }
        ]
    }
]

response = openai.ChatCompletion.create(
    model="gpt-4o",
    messages=conversation_history
)

generated_caption = response['choices'][0]['message']['content']

f = open("/app/vision/result.txt", "w")
f.write(generated_caption)
f.close()