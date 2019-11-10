from rest_framework import serializers
from warehouse.models import User, Product


class ProductSerializer(serializers.Serializer):
    man_name = serializers.CharField(required=True, allow_blank=True, max_length=100)
    model_name = serializers.CharField(required=True, allow_blank=True, max_length=100)
    price = serializers.IntegerField()
    quantity = serializers.IntegerField()

    def create(self, validated_data):
        """
        Create and return a new `Snippet` instance, given the validated data.
        """
        return Product.objects.create(**validated_data)

    def update(self, instance, validated_data):
        """
        Update and return an existing `Snippet` instance, given the validated data.
        """
        instance.man_name = validated_data.get('man_name', instance.title)
        instance.model_name = validated_data.get('model_name', instance.code)
        instance.price = validated_data.get('price', instance.linenos)
        instance.quantity = validated_data.get('quantity', instance.linenos)
        instance.save()
        return instance


class UserSerializer(serializers.Serializer):
    id = serializers.IntegerField(read_only=True)
    name = serializers.CharField(required=True, allow_blank=True, max_length=100)
    surname = serializers.CharField(required=True, allow_blank=True, max_length=100)
    job_title = serializers.CharField(required=True, allow_blank=True, max_length=100)

    def create(self, validated_data):
        """
        Create and return a new `Snippet` instance, given the validated data.
        """
        return User.objects.create(**validated_data)

    def update(self, instance, validated_data):
        """
        Update and return an existing `Snippet` instance, given the validated data.
        """
        instance.name = validated_data.get('name', instance.title)
        instance.surname = validated_data.get('surname', instance.code)
        instance.linenos = validated_data.get('job_title', instance.linenos)
        instance.save()
        return instance
