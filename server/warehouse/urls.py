from django.urls import path, include
from rest_framework.urlpatterns import format_suffix_patterns

from warehouse import views

urlpatterns = [
    path('products/', views.ProductsList.as_view()),
    path('rest-auth/', include('rest_auth.urls')),
]

urlpatterns = format_suffix_patterns(urlpatterns)
