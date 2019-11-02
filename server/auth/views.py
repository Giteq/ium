from django.contrib.auth.models import User, Group
from rest_framework import viewsets
from auth.serializers import UserSerializer, GroupSerializer
from rest_framework.views import APIView
from rest_framework.renderers import JSONRenderer
from rest_framework.response import Response


class UserViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows users to be viewed or edited.
    """
    queryset = User.objects.all().order_by('-date_joined')
    serializer_class = UserSerializer


class GroupViewSet(viewsets.ModelViewSet):
    """
    API endpoint that allows groups to be viewed or edited.
    """
    queryset = Group.objects.all()
    serializer_class = GroupSerializer


class UserCountView(APIView):
    """
    A view that returns the count of active users in JSON.
    """
    renderer_classes = [JSONRenderer]

    def get(self, request, format=None):
        content = {
            'html': "https://accounts.google.com/o/oauth2/v2/auth?" +
                     "client_id=276416597205-i796qijcjhb63jkimi4icnq4j2sho8np.apps.googleusercontent.com&" +
                     "response_type=code&" +
                     "scope=openid%20email&" +
                     "redirect_uri=http://www.myapp.com/answer&" +
                     "state=security_token%3D138r5719ru3e1%26url%3Dhttps://oauth2-login-demo.example.com/myHome&" +
                     "login_hint=jsmith@example.com&" +
                     "openid.realm=example.com&" +
                     "nonce=0394852-3190485-2490358&" +
                     "hd=example.com"
        }
        return Response(content)
