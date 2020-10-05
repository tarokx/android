from django.urls import path

from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('upload', views.file_upload, name='file_upload'),
    path('do_upload', views.file_upload, name='file_upload'),
    path('showall/', views.showall, name='showall'),
    path('uploadD/', views.upload, name='upload'),
    path('login/',views.login,name='login'),
    path('objsize/',views.objsize,name='objsize'),
    path('objdis/',views.objdis,name='objsize'),
    path('showimg/',views.showimg,name='showimg'),
    path('speachToText/',views.speachToText,name='speachToText'),
    
]