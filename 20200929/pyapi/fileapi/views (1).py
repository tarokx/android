from django.shortcuts import render
from .forms import UploadFileForm
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
import sys
from PIL import Image
import cv2
import numpy as np
import os
from django.shortcuts import render, redirect
#from .models import IMG
from .forms import ImageForm
import pyocr
import pyocr.builders
#objsize
import imutils
from imutils import perspective
from imutils import contours
from scipy.spatial import distance as dist
def index(request):
    return HttpResponse("Hello Django world!")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
# ------------------------------------------------------------------
@csrf_exempt
def file_upload(request):
    form = UploadFileForm(request.POST, request.FILES)
    sys.stderr.write("*** file_uploaded *** start ***\n")
    if request.method == 'POST':
        imgSave(request)
        return success(request)
    else:
        form = UploadFileForm()
    return render(request, 'file_upload/upload.html', {'form': form})
    
def imgSave(request):
    form = UploadFileForm(request.POST, request.FILES)
    
    #sys.stderr.write(request.FILES['file'].name + "\n")
    #sys.stderr.write(request.POST['id'] + "\n")
    if form.is_valid():
        sys.stderr.write("*** file_upload *** aaa ***\n")
        handle_uploaded_file(request.FILES['file'])
        file_obj = request.FILES['file']
        sys.stderr.write(file_obj.name + "\n")
        sys.stderr.write(request.POST['id'] + "\n")
#
# ------------------------------------------------------------------
def handle_uploaded_file(file_obj):
    sys.stderr.write("*** handle_uploaded_file *** aaa ***\n")
    sys.stderr.write(file_obj.name + "\n")
    file_path = BASE_DIR + '/img/' + file_obj.name 
    sys.stderr.write(file_path + "\n")
    with open(file_path, 'wb+') as destination:
        for chunk in file_obj.chunks():
            sys.stderr.write("*** handle_uploaded_file *** ccc ***\n")
            destination.write(chunk)
            sys.stderr.write("*** handle_uploaded_file *** eee ***\n")
#
# ------------------------------------------------------------------
def success(request):
    path=BASE_DIR + '/img/'
    file_obj = request.FILES['file']
    res = imgOCR(path,file_obj.name)
    str_out = "{'message':'"+res+"'}"
    sys.stderr.write(str_out+'\n')
    return HttpResponse(str_out)

def tmp():
    #file_obj = request.FILES['file']
    response = HttpResponse(content_type="image/png")
    path=BASE_DIR + '/img/'

    #res = imgChange(path,file_obj.name)
    return HttpResponse("res")
    #res=imgChange(path,file_obj.name)
    #res.save(response,'png')
    #return response
# ------------------------------------------------------------------
def imgOCR(path,filename):
    # 1.インストール済みのTesseractのパスを通す
    path_tesseract = "C:\\Program Files\\Tesseract-OCR"
    if path_tesseract not in os.environ["PATH"].split(os.pathsep):
        os.environ["PATH"] += os.pathsep + path_tesseract
    
    # 2.OCRエンジンの取得
    tools = pyocr.get_available_tools()
    tool = tools[0]
    
    # 3.原稿画像の読み込み
    img_org = Image.open(path+filename)
    
    # 4.ＯＣＲ実行
    builder = pyocr.builders.TextBuilder()
    result = tool.image_to_string(img_org, lang="jpn", builder=builder)
    if result=="":
        result="nothing"
    sys.stderr.write(result+'\n')
    return result
    
def imgChange(path,imgPth):
    # 入力画像とテンプレート画像をで取得
    img = np.array(Image.open(path+imgPth))
    temp = np.array(Image.open(path+"temp.jpg"))
    
    # グレースケール変換
    gray = cv2.cvtColor(img,cv2.COLOR_RGB2GRAY)
    temp = cv2.cvtColor(temp,cv2.COLOR_RGB2GRAY)

    # テンプレート画像の高さ・幅
    h, w = temp.shape

    # テンプレートマッチング（OpenCVで実装）
    match = cv2.matchTemplate(gray, temp, cv2.TM_SQDIFF_NORMED)
    min_value, max_value, min_pt, max_pt = cv2.minMaxLoc(match)
    pt = min_pt

    # テンプレートマッチングの結果を出力
    cv2.rectangle(img, (pt[0], pt[1]), (pt[0] + w, pt[1] + h), (0, 0, 200), 3)
    cv2.imwrite(path+"res.jpg",img)
    return Image.fromarray(np.uint8(img))

def upload(request):
    if request.method == "POST":
        form = ImageForm(request.POST, request.FILES)
        if form.is_valid():
            form.save()
            return redirect('showall')
    else:
        form = ImageForm()

    context = {'form':form}
    return render(request, 'album/upload.html', context)


def showall(request):
    images = Image.objects.all()
    context = {'images':images}
    return render(request, 'album/showall.html', context)

@csrf_exempt
def login(request):
    if request.method == "POST":
        try:
            id=request.POST['id']
            ps=request.POST['pass']
            sys.stderr.write("id:" + id + "\n")
            sys.stderr.write("pass:" + ps + "\n")
            if(id=="1" and ps=="password"):
                str_out = "{'message':'welcome "+id+"'}"
            else:
                str_out = "{'message':'NO'}"
        except:
            str_out = "{'message':'ERR'}"
        return HttpResponse(str_out)
    else:
        return render(request, 'login/login.html')
    
def midpoint(ptA, ptB):
	return ((ptA[0] + ptB[0]) * 0.5, (ptA[1] + ptB[1]) * 0.5)

@csrf_exempt
def showimg(request):
    imgpath=BASE_DIR + '/img/res.jpg'
    img = Image.open(imgpath)
    response = HttpResponse(content_type='image/jpg')
    img.save(response, "JPEG")
    return response
    
@csrf_exempt
def objsize(request):
    if request.method == "POST":
        try:
            objsize_do(request)
            str_out="{'message':'OK'}"  
            return HttpResponse(str_out)
            
        except:
            str_out = "{'message':'ERR'}"
        return HttpResponse(str_out)
    else:
        return render(request, 'login/login.html')

def objsize_do(request):
    path=BASE_DIR + '/img/'
    imgfile = path + request.FILES['file'].name
    imgSave(request)
    # load the image, convert it to grayscale, and blur it slightly
    image = np.array(Image.open(imgfile))
    sys.stderr.write("*** file_objsize_do *** start ***\n")
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    gray = cv2.GaussianBlur(gray, (7, 7), 0)

    # perform edge detection, then perform a dilation + erosion to
    # close gaps in between object edges
    edged = cv2.Canny(gray, 50, 100)
    edged = cv2.dilate(edged, None, iterations=1)
    edged = cv2.erode(edged, None, iterations=1)

    # find contours in the edge map
    cnts = cv2.findContours(edged.copy(), cv2.RETR_EXTERNAL,
        cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)

    # sort the contours from left-to-right and initialize the
    # 'pixels per metric' calibration variable
    (cnts, _) = contours.sort_contours(cnts)
    pixelsPerMetric = None
        
    orig = image.copy()
    
    # loop over the contours individually
    for c in cnts:
        # if the contour is not sufficiently large, ignore it
        if cv2.contourArea(c) < 100:
            continue

        # compute the rotated bounding box of the contour
        box = cv2.minAreaRect(c)
        box = cv2.cv.BoxPoints(box) if imutils.is_cv2() else cv2.boxPoints(box)
        box = np.array(box, dtype="int")

        # order the points in the contour such that they appear
        # in top-left, top-right, bottom-right, and bottom-left
        # order, then draw the outline of the rotated bounding
        # box
        box = perspective.order_points(box)
        cv2.drawContours(orig, [box.astype("int")], -1, (0, 255, 0), 2)

        # loop over the original points and draw them
        for (x, y) in box:
            cv2.circle(orig, (int(x), int(y)), 5, (0, 0, 255), -1)

        # unpack the ordered bounding box, then compute the midpoint
        # between the top-left and top-right coordinates, followed by
        # the midpoint between bottom-left and bottom-right coordinates
        (tl, tr, br, bl) = box
        (tltrX, tltrY) = midpoint(tl, tr)
        (blbrX, blbrY) = midpoint(bl, br)

        # compute the midpoint between the top-left and top-right points,
        # followed by the midpoint between the top-righ and bottom-right
        (tlblX, tlblY) = midpoint(tl, bl)
        (trbrX, trbrY) = midpoint(tr, br)

        # draw the midpoints on the image
        cv2.circle(orig, (int(tltrX), int(tltrY)), 5, (255, 0, 0), -1)
        cv2.circle(orig, (int(blbrX), int(blbrY)), 5, (255, 0, 0), -1)
        cv2.circle(orig, (int(tlblX), int(tlblY)), 5, (255, 0, 0), -1)
        cv2.circle(orig, (int(trbrX), int(trbrY)), 5, (255, 0, 0), -1)

        # draw lines between the midpoints
        cv2.line(orig, (int(tltrX), int(tltrY)), (int(blbrX), int(blbrY)),
            (255, 0, 255), 2)
        cv2.line(orig, (int(tlblX), int(tlblY)), (int(trbrX), int(trbrY)),
            (255, 0, 255), 2)

        # compute the Euclidean distance between the midpoints
        dA = dist.euclidean((tltrX, tltrY), (blbrX, blbrY))
        dB = dist.euclidean((tlblX, tlblY), (trbrX, trbrY))

        # if the pixels per metric has not been initialized, then
        # compute it as the ratio of pixels to supplied metric
        # (in this case, inches)
        if pixelsPerMetric is None:
            pixelsPerMetric = dB

        # compute the size of the object
        dimA = dA / pixelsPerMetric
        dimB = dB / pixelsPerMetric

        # draw the object sizes on the image
        cv2.putText(orig, "{:.1f}in".format(dimA),
            (int(tltrX - 15), int(tltrY - 10)), cv2.FONT_HERSHEY_SIMPLEX,
            0.65, (255, 255, 255), 2)
        cv2.putText(orig, "{:.1f}in".format(dimB),
            (int(trbrX + 10), int(trbrY)), cv2.FONT_HERSHEY_SIMPLEX,
            0.65, (255, 255, 255), 2)
    sys.stderr.write("*** file_obj *** end ***\n")
    res=Image.fromarray(np.uint8(orig))
    res.save(path + "res.jpg")
    
@csrf_exempt
def objdis(request):
    if request.method == "POST":
        try:
            objdis_do(request)
            str_out="{'message':'OK'}"  
            return HttpResponse(str_out)
            
        except:
            str_out = "{'message':'ERR'}"
        return HttpResponse(str_out)
    else:
        return render(request, 'login/login.html')

def objdis_do(request):
    path=BASE_DIR + '/img/'
    imgfile = path + request.FILES['file'].name
    imgSave(request)
    # load the image, convert it to grayscale, and blur it slightly
    image = np.array(Image.open(imgfile))
    sys.stderr.write("*** file_objdis_do *** start ***\n")
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    gray = cv2.GaussianBlur(gray, (7, 7), 0)

    # perform edge detection, then perform a dilation + erosion to
    # close gaps in between object edges
    edged = cv2.Canny(gray, 50, 100)
    edged = cv2.dilate(edged, None, iterations=1)
    edged = cv2.erode(edged, None, iterations=1)

    # find contours in the edge map
    cnts = cv2.findContours(edged.copy(), cv2.RETR_EXTERNAL,
        cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)

    # sort the contours from left-to-right and, then initialize the
    # distance colors and reference object
    (cnts, _) = contours.sort_contours(cnts)
    colors = ((0, 0, 255), (240, 0, 159), (0, 165, 255), (255, 255, 0),
        (255, 0, 255))
    refObj = None
    
    orig = image.copy()

    # loop over the contours individually
    for c in cnts:
        # if the contour is not sufficiently large, ignore it
        if cv2.contourArea(c) < 100:
            continue

        # compute the rotated bounding box of the contour
        box = cv2.minAreaRect(c)
        box = cv2.cv.BoxPoints(box) if imutils.is_cv2() else cv2.boxPoints(box)
        box = np.array(box, dtype="int")

        # order the points in the contour such that they appear
        # in top-left, top-right, bottom-right, and bottom-left
        # order, then draw the outline of the rotated bounding
        # box
        box = perspective.order_points(box)

        # compute the center of the bounding box
        cX = np.average(box[:, 0])
        cY = np.average(box[:, 1])

        # if this is the first contour we are examining (i.e.,
        # the left-most contour), we presume this is the
        # reference object
        if refObj is None:
            # unpack the ordered bounding box, then compute the
            # midpoint between the top-left and top-right points,
            # followed by the midpoint between the top-right and
            # bottom-right
            (tl, tr, br, bl) = box
            (tlblX, tlblY) = midpoint(tl, bl)
            (trbrX, trbrY) = midpoint(tr, br)

            # compute the Euclidean distance between the midpoints,
            # then construct the reference object
            D = dist.euclidean((tlblX, tlblY), (trbrX, trbrY))
            refObj = (box, (cX, cY), D)
            continue

        # draw the contours on the image
        cv2.drawContours(orig, [box.astype("int")], -1, (0, 255, 0), 2)
        cv2.drawContours(orig, [refObj[0].astype("int")], -1, (0, 255, 0), 2)

        # stack the reference coordinates and the object coordinates
        # to include the object center
        refCoords = np.vstack([refObj[0], refObj[1]])
        objCoords = np.vstack([box, (cX, cY)])

        # loop over the original points
        for ((xA, yA), (xB, yB), color) in zip(refCoords, objCoords, colors):
            # draw circles corresponding to the current points and
            # connect them with a line
            cv2.circle(orig, (int(xA), int(yA)), 5, color, -1)
            cv2.circle(orig, (int(xB), int(yB)), 5, color, -1)
            cv2.line(orig, (int(xA), int(yA)), (int(xB), int(yB)),
                color, 2)

            # compute the Euclidean distance between the coordinates,
            # and then convert the distance in pixels to distance in
            # units
            D = dist.euclidean((xA, yA), (xB, yB)) / refObj[2]
            (mX, mY) = midpoint((xA, yA), (xB, yB))
            cv2.putText(orig, "{:.1f}in".format(D), (int(mX), int(mY - 10)),
                cv2.FONT_HERSHEY_SIMPLEX, 0.55, color, 2)
            
    sys.stderr.write("*** file_obj *** end ***\n")
    res=Image.fromarray(np.uint8(orig))
    res.save(path + "res.jpg")