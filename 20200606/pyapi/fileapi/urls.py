from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('upload', views.file_upload, name='file_upload'),
    path('do_upload', views.file_upload, name='file_upload'),
    path('showall/', views.showall, name='showall'),
    path('uploadD/', views.upload, name='upload'),
]