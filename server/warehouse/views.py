from rest_framework import status, permissions
from rest_framework.parsers import JSONParser
from rest_framework.permissions import BasePermission
from rest_framework.response import Response
from rest_framework.views import APIView
from django.contrib.auth.models import User, Group

from warehouse.models import *
from warehouse.serializers import ProductSerializer


class IsManagerOrEmployee(permissions.BasePermission):

    def has_permission(self, request, view):
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

