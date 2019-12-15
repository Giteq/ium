import datetime

import django
from django.core.exceptions import ObjectDoesNotExist
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
            prod1.quantity == prod2.quantity
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
        print(products)
        serializer = ProductSerializer(products, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = ProductSerializer(data=request.data)

        if serializer.is_valid():
            try:
                Product.objects.get(man_name=serializer.validated_data['man_name'])
            except Product.DoesNotExist:
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
            if self._can_modify(atr, product, old):
                if atr =='quantity':
                    product.quantity = old['quantity'] + changes['quantity']
                    if old['quantity'] + changes['quantity'] < 0:
                        return _end(f'Quantity cannot be less than 0.')
                if atr == 'price':
                    product.price = old['price'] + changes['price']
                if atr == 'model_name':
                    product.model_name = changes['model_name']
            else:
                error_fields.append(atr)
        if error_fields:
            return _end(f'Someone has modified "{error_fields}". You should sync with server')
        else:
            product.save()

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
        for atr in changes:
            if not self._can_modify(atr, product, old):
                error_fields.append(atr)
        if error_fields:
            return _end(f'Someone has modified "{error_fields}". You should sync with server')
        else:
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
            # print(json.dumps(serializer.data, indent=4))
            err = self._add_prod_handle(serializer.data)
            if err is not None:
                return Response(err, status=status.HTTP_300_MULTIPLE_CHOICES)
            err = self._rm_prod_handle(serializer.data)
            if err is not None:
                return Response(err, status=status.HTTP_300_MULTIPLE_CHOICES)
            err = self._mod_prod_handle(serializer.data)
            if err is not None:
                return Response(err, status=status.HTTP_300_MULTIPLE_CHOICES)
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    def _mod_prod_handle(self, req_prods):
        # 0-> new , 1->old
        for prod in self._match_old_to_new(req_prods):
            # Check if product is modified
            if not _is_products_same(Product(**prod[1]), Product(**prod[0])):
                prod_name = prod[0]["man_name"]
                try:
                    db_prod = Product.objects.get(man_name=prod[0]['man_name'])
                except Product.DoesNotExist:
                    err = {
                        'error_msg': f'Product {prod_name} does not exist any more. You should sync with server'
                    }
                    print(err)
                    return err

                if db_prod.quantity + prod[0]['quantity'] < 0:
                    err = {
                        'error_msg': f'Cannot change quantity of {prod_name}. You should sync with server'
                    }
                    return err
                else:
                    # Check if product is modified.
                    new_prod = Product(**prod[0])
                    old_prod = Product(**prod[1])
                    errs = []
                    for attr in ("quantity", "price"):
                        # Check if attribute was changed
                        print(attr, getattr(new_prod, attr), getattr(old_prod, attr))
                        if getattr(new_prod, attr) != getattr(old_prod, attr):
                            # Yes, check if actual value is due to database
                            if getattr(old_prod, attr) == getattr(db_prod, attr):
                                # Yes
                                setattr(db_prod, attr, getattr(new_prod, attr))
                            else:
                                errs.append(attr)
                    if errs:
                        err = {
                            'error_msg': f'Cannot make changes in {errs} for product {new_prod.man_name}. You should sync with server'
                        }
                        print(err)
                        return err
                    else:
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
                    err = {
                        'error_msg': f'Someone has also added "{prod["man_name"]}". You should sync with server'
                    }
                    print(err)
                    return err

    def _rm_prod_handle(self, req_prods):
        # for diff in list(dictdiffer.diff(req_prods["old"], req_prods["new"])):
        new_prod_only_keys = [item['man_name'] for item in req_prods["new"]]
        old_prod_only_keys = [item['man_name'] for item in req_prods["old"]]
        to_remove = [item for item in old_prod_only_keys if item not in new_prod_only_keys]

        objects_to_remove = [item for item in req_prods["old"] if item["man_name"] in to_remove]
        for item in objects_to_remove:
            db_prod = Product.objects.get(man_name=item['man_name'])
            tmp_prod = Product(**item)
            if _is_products_same(tmp_prod, db_prod):
                db_prod.delete()

            else:
                err = {
                    'error_msg': f'Someone has modified "{db_prod.man_name}". You should sync with server'
                }
                print(err)
                return err
