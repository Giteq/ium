import datetime

from django.contrib.auth.base_user import AbstractBaseUser, BaseUserManager
from django.contrib.auth.models import AbstractUser
from django.db import models


class Product(models.Model):
    man_name = models.CharField(max_length=100, blank=False, default='')
    model_name = models.CharField(max_length=100, blank=False, default='')
    price = models.IntegerField()
    quantity = models.IntegerField()

    class Meta:
        ordering = ['man_name']
