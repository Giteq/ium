from django.db import models


class Product(models.Model):
    man_name = models.CharField(max_length=100, blank=False, default='')
    model_name = models.CharField(max_length=100, blank=False, default='')
    price = models.IntegerField()
    quantity = models.IntegerField()

    class Meta:
        ordering = ['man_name']


class User(models.Model):
    name = models.CharField(max_length=100, blank=False, default='')
    surname = models.CharField(max_length=100, blank=False, default='')
    job_title = models.CharField(max_length=100, blank=False, default='')

    class Meta:
        ordering = ['surname']
