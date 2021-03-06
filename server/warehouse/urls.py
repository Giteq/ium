from django.urls import path, include
from rest_framework.urlpatterns import format_suffix_patterns

from warehouse import views
from django.contrib.auth.views import LogoutView



urlpatterns = [
    path('products/', views.ProductsList.as_view()),
    path('products/<int:pk>/', views.ProductsDetail.as_view()),
    path('o/', include('oauth2_provider.urls', namespace='oauth2_provider')),
]

urlpatterns = format_suffix_patterns(urlpatterns)
