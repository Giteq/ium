import datetime
import json

import django
from django.core.exceptions import ObjectDoesNotExist
from oauth2_provider.models import AbstractAccessToken, AccessToken
from rest_framework import status, permissions
from rest_framework.response import Response
from rest_framework.status import HTTP_300_MULTIPLE_CHOICES
from rest_framework.views import APIView
from django.contrib.auth.models import User, Group
import requests
from rest_framework.authtoken.models import Token

from warehouse.models import *
from warehouse.serializers import ProductSerializer, SyncSerializer


class IsManagerOrEmployee(permissions.BasePermission):

    def has_permission(self, request, view):
        manager_group = Group.objects.get(name="warehouse managers")
        employee_group = Group.objects.get(name="warehouse employees")
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
        request_copy = request.data.copy()
        tmp = json.loads(request_copy['old_prod_info'])
        tmp_request_copy = {"old_" + key: value for key, value in tmp.items() if key != 'id'}
        tmp = json.loads(request_copy['new_prod_info'])
        request_copy = tmp
        request_copy.update(tmp_request_copy)

        serializer = ProductSerializer(product, data=request_copy)
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


class AuthView(APIView):

    def post(self, request):
        access_token = request.data["token"]
        headers = {
            "Cache-Control": "no-cache",
            'Authorization': f'Bearer {access_token}'
        }
        ans = requests.get(f"http://localhost:8001/userinfo/?token={access_token}", headers=headers)
        data = ans.json()
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

        return Response({"access_token": token.key})


class SyncView(APIView):
    permission_classes = [IsManagerOrEmployee, permissions.IsAuthenticatedOrReadOnly]

    def put(self, request, format=None):
        req_copy = request.data.copy()
        seriable = dict(new=req_copy["new"], old=req_copy["old"])
        serializer = SyncSerializer(data=seriable)
        if serializer.is_valid():
            print(json.dumps(serializer.data, indent=4))
            self._mod_prod_handle(serializer.data)
            self._add_prod_handle(serializer.data)
            self._rm_prod_handle(serializer.data)
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def _mod_prod_handle(self, req_prods):
        for prod in req_prods['new']:
            db_prod = Product.objects.get(man_name=prod['man_name'])
            if db_prod.quantity + prod['quantity'] < 0:
                err = {
                    'error_msg': f'Cannot change quantity. You should sync with server'
                }
                return Response(err, status=HTTP_300_MULTIPLE_CHOICES)
            else:
                if prod['quantity'] != 0:
                    print(prod['quantity'])
                    db_prod.quantity = db_prod.quantity + prod['quantity']
                    db_prod.save()

    def _add_prod_handle(self, req_prods):
        for prod in req_prods['new']:
            if prod['id'] == -1:
                try:
                    db_prods = Product.objects.get(man_name=prod['man_name'])
                except ObjectDoesNotExist:
                    del(prod['id'])
                    new_prod = Product(**prod)
                    new_prod.save()
                else:
                    err = {
                        'error_msg': f'Someone has also added "{prod["man_name"]}". You should sync with server'
                    }
                    print(err)
                    return Response(err, status=HTTP_300_MULTIPLE_CHOICES)

    def _rm_prod_handle(self, req_prods):
        # for diff in list(dictdiffer.diff(req_prods["old"], req_prods["new"])):
        new_prod_only_keys = [item['man_name'] for item in req_prods["new"]]
        old_prod_only_keys = [item['man_name'] for item in req_prods["old"]]
        to_remove = [item for item in old_prod_only_keys if item not in new_prod_only_keys]

        objects_to_remove = [item for item in req_prods["old"] if item["man_name"] in to_remove]
        print(to_remove)
        for item in objects_to_remove:
            db_prod = Product.objects.get(man_name=item['man_name'])
            tmp_prod = Product(**item)
            if tmp_prod == db_prod:
                print(True)
            else:
                print(False)
