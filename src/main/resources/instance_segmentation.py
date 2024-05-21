from ultralytics import FastSAM
from ultralytics.models.fastsam import FastSAMPrompt
import sys

source = "./app/vision/input.jpg"

model = FastSAM('FastSAM-x.pt') 

everything_results = model(source, device='cpu', retina_masks=True, imgsz=1024, conf=0.4, iou=0.9)

prompt_process = FastSAMPrompt(source, everything_results, device='cpu')

ann = prompt_process.text_prompt(text=sys.argv[1])

prompt_process.plot(annotations=ann, output='./result')

result_file = "result/input.jpg"

f = open("./app/vision/result.txt", "w")
f.write(result_file)
f.close()