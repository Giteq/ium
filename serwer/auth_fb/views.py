from django.contrib.auth import logout, login
from django.shortcuts import render

from .models import User


def index(request):
    context = {
        'posts': []
        if request.user.is_authenticated else []
    }

    print(request.user.email)

    return render(request, 'index.html', context)


def logout_view(request):
    logout(request)
    # Redirect to a success page.
    print(request)
    return render(request, 'index.html')
