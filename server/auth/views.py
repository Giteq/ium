import httplib2
import oauth2client.client
import requests
from django.contrib.auth.models import User, Group
import json
import requests as req

from rest_framework import viewsets
from auth.serializers import UserSerializer, GroupSerializer
from rest_framework.views import APIView
from rest_framework.renderers import JSONRenderer
from rest_framework.response import Response
from google.oauth2 import id_token
from google.auth.transport import requests
import googleapiclient.discovery

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


CLIENT_ID = '276416597205-fdi3s21e6dshg9384c8rgsj1ef28h6rr.apps.googleusercontent.com'

class UserCountView(APIView):
    """
    A view that returns the count of active users in JSON.
    """
    renderer_classes = [JSONRenderer]


    def post(self, request, format=None):
        content = {
            'answer': str(self._validate_id_token(request.data['id_token']))
        }

        user_agent = "Google Sheets API for Python"
        revoke_uri = "https://apis.google.com/js/platform.js"
        credentials = oauth2client.client.AccessTokenCredentials(
            access_token=request.data['access_token'],
            user_agent=user_agent,
            revoke_uri=revoke_uri)

        http = httplib2.Http()
        http = credentials.authorize(http)
        print(http)
        hed = {
            "Host": "www.googleapis.com",
            "Authorization": f"Bearer {request.data['access_token']}"
        }
        ans = req.get(f"https://www.googleapis.com/oauth2/v3/userinfo", headers=hed)
        print(ans.text)

        return Response(content)

    def _validate_id_token(self, token_id):
        try:
            # Specify the CLIENT_ID of the app that accesses the backend:
            idinfo = id_token.verify_oauth2_token(token_id, requests.Request(), CLIENT_ID)

            # Or, if multiple clients access the backend server:
            # idinfo = id_token.verify_oauth2_token(token, requests.Request())
            # if idinfo['aud'] not in [CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3]:
            #     raise ValueError('Could not verify audience.')

            if idinfo['iss'] not in ['accounts.google.com', 'https://accounts.google.com']:
                raise ValueError('Wrong issuer.')

            # If auth request is from a G Suite domain:
            # if idinfo['hd'] != GSUITE_DOMAIN_NAME:
            #     raise ValueError('Wrong hosted domain.')

            # ID token is valid. Get the user's Google Account ID from the decoded token.
            userid = idinfo['sub']
            return "OK"

        except ValueError:
            # Invalid token
            pass

        return "False"
