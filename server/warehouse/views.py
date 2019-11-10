from rest_framework import status, permissions, generics
from rest_framework.parsers import JSONParser
from rest_framework.response import Response
from rest_framework.views import APIView

from warehouse import serializers
from warehouse.models import Product
from warehouse.serializers import ProductSerializer
from warehouse.custom_permissions import IsUserAllowed


from warehouse.models import *

class ProductsList(APIView):
    # permission_classes = [permissions.IsAuthenticatedOrReadOnly | IsUserAllowed]
    permission_classes = [permissions.IsAuthenticatedOrReadOnly]

    def get(self, request, format=None):
        products = Product.objects.all()
        print(products)
        serializer = ProductSerializer(products, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = ProductSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_201_CREATED)


class ProductsDetail(APIView):
    def get(self, request, pk, format=None):
        if self._get_product(request, pk, format) is None:
            return Response(status=status.HTTP_404_NOT_FOUND)

        serializer = ProductSerializer(self.product)
        return Response(serializer.data)

    def put(self, request, pk, format=None):
        if self._get_product(request, pk, format) is None:
            return Response(status=status.HTTP_404_NOT_FOUND)

        data = JSONParser().parse(request)
        serializer = ProductSerializer(self.product, data=data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def delete(self, request, pk, format=None):
        if self._get_product(request, pk, format) is None:
            return Response(status=status.HTTP_404_NOT_FOUND)

        self.product.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)

    def _get_product(self, request, pk, format=None):
        try:
            self.product = Product.objects.get(pk=pk)
        except Product.DoesNotExist:
            return None

