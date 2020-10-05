from django import forms

class UploadFileForm(forms.Form):
    id = forms.CharField(max_length=50)
    file = forms.FileField()

