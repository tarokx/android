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

def index(request):
    return HttpResponse("Hello Django world!")

# ------------------------------------------------------------------
@csrf_exempt
def file_upload(request):
    sys.stderr.write("*** file_uploaded *** start ***\n")
    if request.method == 'POST':
        form = UploadFileForm(request.POST, request.FILES)
        
        #sys.stderr.write(request.FILES['file'].name + "\n")
        #sys.stderr.write(request.POST['id'] + "\n")
        if form.is_valid():
            sys.stderr.write("*** file_upload *** aaa ***\n")
            handle_uploaded_file(request.FILES['file'])
            file_obj = request.FILES['file']
            sys.stderr.write(file_obj.name + "\n")
            sys.stderr.write(request.POST['id'] + "\n")
            #return HttpResponseRedirect('/success/url/')
            return success(request)
    else:
        form = UploadFileForm()
    return render(request, 'file_upload/upload.html', {'form': form})
#
#
# ------------------------------------------------------------------
def handle_uploaded_file(file_obj):
    sys.stderr.write("*** handle_uploaded_file *** aaa ***\n")
    sys.stderr.write(file_obj.name + "\n")
    file_path = 'D:/Google ドライブ/k/Django/pyapi/img/' + file_obj.name 
    sys.stderr.write(file_path + "\n")
    with open(file_path, 'wb+') as destination:
        for chunk in file_obj.chunks():
            sys.stderr.write("*** handle_uploaded_file *** ccc ***\n")
            destination.write(chunk)
            sys.stderr.write("*** handle_uploaded_file *** eee ***\n")
#
# ------------------------------------------------------------------
def success(request):
    path="D:/Google ドライブ/k/Django/pyapi/img/"
    file_obj = request.FILES['file']
    res=imgOCR(path,file_obj.name)
    str_out = "{'message':'"+res+"'}"
    return HttpResponse(str_out)
def tmp():
    #file_obj = request.FILES['file']
    response = HttpResponse(content_type="image/png")
    path="D:/Google ドライブ/k/Django/pyapi/img/"

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