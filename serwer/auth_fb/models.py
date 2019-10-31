from django.db import models
from django.contrib.auth.models import AbstractBaseUser, PermissionsMixin



class User:
    """
    Custom user realization based on Django AbstractUser and PermissionMixin.
    """
    email = models.EmailField(
        ('email address'),
        unique=True,
        error_messages={
            'unique': ("A user with that email already exists."),
        })

    class Meta:
        'email'
