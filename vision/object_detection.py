import cv2
from ultralytics import YOLO

model = YOLO("yolov9c.pt")

image = cv2.imread("/tmp/vision/input.jpg")

results = model.predict(image, conf=0.5, save=True, project="result")

result_file = results[0].save_dir + "/" + results[0].path

f = open("/tmp/vision/result.txt", "w")
f.write(result_file)
f.close()