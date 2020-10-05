from django import forms
from .models import Image

class UploadFileForm(forms.Form):
    id = forms.CharField(max_length=50)
    file = forms.FileField()

class ImageForm(forms.ModelForm):
    class Meta:
        model = Image
        fields = ['picture', 'title']