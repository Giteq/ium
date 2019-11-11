from rest_framework import status, permissions
from rest_framework.parsers import JSONParser
from rest_framework.response import Response
from rest_framework.views import APIView

from warehouse.models import *
from warehouse.serializers import ProductSerializer


class ProductsList(APIView):
    # permission_classes = [permissions.IsAuthenticatedOrReadOnly | IsUserAllowed]
    permission_classes = [permissions.IsAuthenticatedOrReadOnly]

    def get(self, request, format=None):
        products = Product.objects.all()
        serializer = ProductSerializer(products, many=True)
        print(serializer.data)
        return Response(serializer.data)

    def post(self, request):
        serializer = ProductSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_201_CREATED)


class ProductsDetail(APIView):
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
        print(request.data)
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

