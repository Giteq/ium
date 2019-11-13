import datetime

import django
from oauth2_provider.models import AbstractAccessToken, AccessToken
from rest_framework import status, permissions
from rest_framework.response import Response
from rest_framework.views import APIView
from django.contrib.auth.models import User, Group

from warehouse.models import *
from warehouse.serializers import ProductSerializer


class IsManagerOrEmployee(permissions.BasePermission):

    def has_permission(self, request, view):
        print(request.user.groups.all())
        manager_group = Group.objects.get(name="Managers")
        employee_group = Group.objects.get(name="Workers")
        if manager_group in request.user.groups.all() or employee_group in request.user.groups.all():
            # DELETE request allowed only for group "warehouse manager"
            if request.method == "DELETE" and not manager_group in request.user.groups.all():
                return False
            return True
        else:
            return False


class ProductsList(APIView):
    permission_classes = [IsManagerOrEmployee, permissions.IsAuthenticatedOrReadOnly]

    def get(self, request, format=None):
        products = Product.objects.all()
        serializer = ProductSerializer(products, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = ProductSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_201_CREATED)


class ProductsDetail(APIView):
    permission_classes = [IsManagerOrEmployee, permissions.IsAuthenticatedOrReadOnly]

    def get(self, request, pk, format=None):
        product = self._get_product(pk)
        if product is None:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer = ProductSerializer(product)
        return Response(serializer.data)

    def put(self, request, pk, format=None):
        product = self._get_product(pk)
        if product is None:
            return Response(status=status.HTTP_404_NOT_FOUND)
        serializer = ProductSerializer(product, data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        product = self._get_product(pk)
        if product is None:
            return Response(status=status.HTTP_404_NOT_FOUND)

        product.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

    def _get_product(self, id):
        try:
            return Product.objects.get(id=id)
        except Product.DoesNotExist:
            return None


import requests
from rest_framework.authtoken.models import Token
class AuthView(APIView):

    def post(self, request):
        access_token = request.data["token"]
        headers = {
            "Cache-Control": "no-cache",
            'Authorization': f'Bearer {access_token}'
        }
        ans = requests.get(f"http://localhost:8001/userinfo/?token={access_token}", headers=headers)
        data = ans.json()
        print(data)
        try:
            user = User.objects.create_user(data['nickname'])
            user.save()
        except django.db.utils.IntegrityError:
            user = User.objects.get(username=data['nickname'])
        try:
            token = Token.objects.create(user=user)
        except django.db.utils.IntegrityError:
            token = Token.objects.get(user=user)
        try:
            AccessToken.objects.create(user=user, expires=datetime.datetime.today() + datetime.timedelta(days=1),
                                       token=token)
        except django.db.utils.IntegrityError:
            AccessToken.objects.get(user=user, token=token)

        print(token.key)
        return Response({"access_token": token.key})
# c86d1cd89df7cd3ea6e89b10f2c1ab28755a2dbc