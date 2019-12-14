
from django.db import models


class Product(models.Model):
    man_name = models.CharField(max_length=100, blank=False, default='')
    model_name = models.CharField(max_length=100, blank=False, default='')
    price = models.IntegerField()
    quantity = models.IntegerField()

    class Meta:
        ordering = ['man_name']

