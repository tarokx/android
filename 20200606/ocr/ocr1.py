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
last_txt = ""

frame = cv2.imread("ocr-test.png")
orgHeight, orgWidth = frame.shape[:2]
size = (int(orgWidth), int(orgHeight))
glay = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
image = cv2.resize(glay, size)
txt = tool.image_to_string(
    Image.fromarray(image),
    lang="jpn",
    builder=pyocr.builders.TextBuilder(tesseract_layout=6)
)
if len(txt) != 0 and txt != last_txt:
    last_txt = txt
    print( txt )
