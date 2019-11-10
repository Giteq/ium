from django.urls import path
from rest_framework.urlpatterns import format_suffix_patterns

from warehouse import views

urlpatterns = [
    path('users/', views.users_list),
    path('users/<int:pk>/', views.user_detail),
    path('products/', views.ProductsList.as_view()),
    path('products/<int:pk>/', views.user_detail),
]

urlpatterns = format_suffix_patterns(urlpatterns)
