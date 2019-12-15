
from django.db import models


class Product(models.Model):
    man_name = models.CharField(max_length=100, blank=False, default='')
    model_name = models.CharField(max_length=100, blank=False, default='')
    price = models.IntegerField()
    quantity = models.IntegerField()

    def __str__(self):
        return f"{self.man_name}({self.id}): {self.quantity}"

    class Meta:
        ordering = ['man_name']

