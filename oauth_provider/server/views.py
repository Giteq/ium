import django
from django.http import HttpResponse
from django.template import loader
from django_registration.forms import User
from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view

from .serializers import UserSerializer


def vote(request):
    template = loader.get_template('profile.html')
    print(request.user)
    context = {
        "redirect": f"com.google.codelabs.appauth:/oauth2callback&tok"
    }
    return HttpResponse(template.render(context, request))

@api_view(['POST'])
def create_auth(request):
    serialized = UserSerializer(data=request.data)
    if serialized.is_valid():
        print(serialized)
        try:
            user = User.objects.create_user(
                username=serialized['username'].value,
                email=serialized['email'].value
            )
            print(serialized['password'].value)
            user.set_password(serialized['password'].value)
            user.save()
        except django.db.utils.IntegrityError:
            print("elo")
        return Response(serialized.data, status=status.HTTP_201_CREATED)
    else:
        print("elo2")
        return Response(serialized._errors, status=status.HTTP_400_BAD_REQUEST)

