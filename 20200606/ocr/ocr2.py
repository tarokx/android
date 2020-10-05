from PIL import Image
import cv2
import sys
import pyocr
import pyocr.builders
import time

tools = pyocr.get_available_tools()
if len(tools) == 0:
    print("No OCR tool found")
    sys.exit(1)
tool = tools[0]

langs = tool.get_available_languages()
lang = langs[0]
capture = cv2.VideoCapture(0)
last_txt = ""
while True:
    ret, frame = capture.read()
    orgHeight, orgWidth = frame.shape[:2]
    size = (int(orgWidth/4), int(orgHeight/4))
    glay = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    image = cv2.resize(glay, size)
    t = time.time()
    txt = tool.image_to_string(
        Image.fromarray(image),
        lang="eng",
        builder=pyocr.builders.TextBuilder(tesseract_layout=6)
    )
    #print(time.time() - t)
    if len(txt) != 0 and txt != last_txt:
        last_txt = txt
        print( txt )

    cv2.imshow("Capture", image)
       
    if cv2.waitKey(33) >= 0:
        break

cv2.destroyAllWindows()
