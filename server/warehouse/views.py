import datetime

import django
from django.core.exceptions import ObjectDoesNotExist
from django.db.models import Sum
from oauth2_provider.models import AccessToken
from rest_framework import status, permissions
from rest_framework.response import Response
from rest_framework.status import HTTP_300_MULTIPLE_CHOICES
from rest_framework.views import APIView
from django.contrib.auth.models import User, Group
import requests
from rest_framework.authtoken.models import Token

from warehouse.models import *
from warehouse.serializers import ProductSerializer, SyncSerializer


def _is_products_same(prod1, prod2):
    return all(
        (
            prod1.man_name == prod2.man_name,
            prod1.model_name == prod2.model_name,
            prod1.price == prod2.price,
            ProductsDetail._get_quantity(prod1.man_name) == ProductsDetail._get_quantity(prod2.man_name)
         )
    )

def _end(msg):
    err = {
        'error_msg': msg
    }
    print(err)
    return Response(err, status=HTTP_300_MULTIPLE_CHOICES)

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

    def put(self, request, format=None):
        products = Product.objects.all()
        serializer = ProductSerializer(products, many=True)
        for ser_data in serializer.data:
            ser_data['quantity'] = ProductsDetail._get_quantity(ser_data['man_name'])
        return Response(serializer.data)

    def post(self, request):
        serializer = ProductSerializer(data=request.data)

        if serializer.is_valid():
            try:
                Product.objects.get(man_name=serializer.validated_data['man_name'])
            except Product.DoesNotExist:
                product_diff = ProductDiff(
                    user_name=request.user.username,
                    man_name=request.data['man_name'],
                    diff=request.data['quantity']
                )
                product_diff.save()
                serializer.save()
                return Response(serializer.data, status=status.HTTP_201_CREATED)
            return Response({'error': 'Product already exists'}, status=status.HTTP_302_FOUND)
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
            return _end(f'There is no product. Consider sync with server.')

        changes = request.data['new']
        for prod in request.data['old']:
            if prod['man_name'] == request.data['new']['man_name']:
                old = prod
                break
        error_fields = []
        for atr in changes:
            if atr =='quantity':
                product_diff = ProductDiff.objects.get(user_name=request.user.username, man_name=changes['man_name'])
                if self._get_quantity(changes['man_name']) + changes['quantity'] >= 0:
                    product_diff.diff += changes['quantity']
                    product_diff.save()
                else:
                    return _end(f'Quantity cannot be less than 0.')
            if atr == 'price':
                ts = float(changes['price']['ts'])
                if ts > product.price_ts:
                    product.price = changes['price']['value']
                    product.price_ts = ts
                    product.save()
            if atr == 'model_name':
                product.model_name = changes['model_name']

        return Response({}, status=status.HTTP_200_OK)

    def delete(self, request, pk, format=None):
        product = self._get_product(pk)
        if product is None:
            return _end(f'There is no product. Consider sync with server.')
        print(request.data)

        changes = request.data['to_remove']
        for prod in request.data['old']:
            if prod['man_name'] == request.data['to_remove']['man_name']:
                old = prod
                break
        error_fields = []
        del(changes['quantity'])
        del(old['quantity'])
        for atr in changes:
            if not self._can_modify(atr, product, old):
                error_fields.append(atr)
        if error_fields:
            return _end(f'Someone has modified "{error_fields}". You should sync with server')
        else:
            ProductDiff.objects.filter(user_name=request.user.username, man_name=changes['man_name']).delete()
            product.delete()

        return Response({}, status=status.HTTP_200_OK)

    def _get_product(self, id):
        try:
            return Product.objects.get(id=id)
        except Product.DoesNotExist:
            return None

    def _can_modify(self, param_name, product, old):
        if getattr(product, param_name) == getattr(Product(**old), param_name):
            return True
        else:
            return False

    @staticmethod
    def _get_quantity(man_name):
        return ProductDiff.objects.filter(man_name=man_name).aggregate(Sum('diff'))['diff__sum']

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
        err = self._add_prod_handle(request.data)
        if err is not None:
            return Response(err, status=status.HTTP_300_MULTIPLE_CHOICES)
        err = self._rm_prod_handle(request.data)
        if err is not None:
            return Response(err, status=status.HTTP_300_MULTIPLE_CHOICES)
        err = self._mod_prod_handle(request.data, request.user.username)
        if err is not None:
            return Response(err, status=status.HTTP_300_MULTIPLE_CHOICES)
        return Response({})

    def _mod_prod_handle(self, req_prods, user_name):
        # 0-> new , 1->old
        for new in self._match_old_to_new(req_prods):
            prod_name = new[0]["man_name"]
            try:
                db_prod = Product.objects.get(man_name=new[0]['man_name'])
            except Product.DoesNotExist:
                return self._end(f'Product {prod_name} does not exist any more. You should sync with server')

            else:
                # Check if product is modified.
                news = new[0]
                if 'quantity' in news:
                    product_diff = ProductDiff.objects.get(user_name=user_name,
                                                           man_name=news['man_name'])
                    if ProductsDetail._get_quantity(news['man_name']) + news['quantity'] >= 0:
                        product_diff.diff += news['quantity']
                        product_diff.save()
                    else:
                        return self._end(f'Quantity cannot be less than 0.')

                if 'price' in news:
                    ts = float(news['price']['ts'])
                    if ts > db_prod.price_ts:
                        db_prod.price = news['price']['value']
                        db_prod.price_ts = ts
                        db_prod.save()

    def _match_old_to_new(self, req_prods):
        prods = []
        for new in req_prods['new']:
            for old in req_prods['old']:
                if old['man_name'] == new['man_name']:
                    prods.append((new, old))
        return prods

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
                    return self._end(f'Someone has also added "{prod["man_name"]}". You should sync with server')

    def _rm_prod_handle(self, req_prods):
        # for diff in list(dictdiffer.diff(req_prods["old"], req_prods["new"])):
        new_prod_only_keys = [item['man_name'] for item in req_prods["new"]]
        old_prod_only_keys = [item['man_name'] for item in req_prods["old"]]
        to_remove = [item for item in old_prod_only_keys if item not in new_prod_only_keys]

        objects_to_remove = [item for item in req_prods["old"] if item["man_name"] in to_remove]
        for item in objects_to_remove:
            db_prod = Product.objects.get(man_name=item['man_name'])
            del(item['quantity'])
            tmp_prod = Product(**item)
            print(item, db_prod.man_name)
            if _is_products_same(tmp_prod, db_prod):
                db_prod.delete()

            else:
                return self._end(f'Someone has modified "{db_prod.man_name}". You should sync with server')

    def _end(self, msg):
        err = {
            'error_msg': msg
        }
        print(err)
        return err
