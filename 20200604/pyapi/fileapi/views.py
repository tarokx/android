from django.http import HttpResponseRedirect
from django.shortcuts import render
from .forms import UploadFileForm
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
import sys

def index(request):
    return HttpResponse("Hello Django world!")

# ------------------------------------------------------------------
@csrf_exempt
def file_upload(request):
    sys.stderr.write("*** file_uploaded *** start ***\n")
    if request.method == 'POST':
        form = UploadFileForm(request.POST, request.FILES)
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
    file_path = 'D:' + file_obj.name 
    sys.stderr.write(file_path + "\n")
    with open(file_path, 'wb+') as destination:
        for chunk in file_obj.chunks():
            sys.stderr.write("*** handle_uploaded_file *** ccc ***\n")
            destination.write(chunk)
            sys.stderr.write("*** handle_uploaded_file *** eee ***\n")
#
# ------------------------------------------------------------------
def success(request):
    #str_out = "Success!<p />"
    #str_out += "成功<p />"
    str_out = "{'message':'OK'}"
    return HttpResponse(str_out)
# ------------------------------------------------------------------