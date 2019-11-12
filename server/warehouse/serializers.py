from rest_framework import serializers
from warehouse.models import Product


class ProductSerializer(serializers.ModelSerializer):
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
        instance.man_name = validated_data.get('man_name', instance.man_name)
        instance.model_name = validated_data.get('model_name', instance.model_name)
        instance.price = validated_data.get('price', instance.price)
        instance.quantity += validated_data.get('quantity', instance.quantity)
        instance.save()
        return instance

    class Meta:
        model = Product
        fields = ('id', 'man_name', 'model_name', 'price', 'quantity')
