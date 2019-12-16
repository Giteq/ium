
from django.db import models


class Product(models.Model):
    man_name = models.CharField(max_length=100, blank=False, default='')
    model_name = models.CharField(max_length=100, blank=False, default='')
    price = models.IntegerField()
    price_ts = models.FloatField(default=0)


    class Meta:
        ordering = ['man_name']


class ProductDiff(models.Model):
    user_name = models.CharField(max_length=200)
    man_name = models.CharField(max_length=100, blank=False, default='')
    diff = models.IntegerField()

    def __str__(self):
        return f"{self.man_name}: {self.diff}"

    class Meta:
        ordering = ['man_name']
