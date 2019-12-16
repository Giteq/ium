from rest_framework import serializers
from warehouse.models import Product


class ProductSerializer(serializers.HyperlinkedModelSerializer):
    man_name = serializers.CharField(required=True, allow_blank=True, max_length=100)
    model_name = serializers.CharField(required=True, allow_blank=True, max_length=100)
    price = serializers.IntegerField()
    quantity = serializers.IntegerField()
    id = serializers.IntegerField(required=False)

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
        if instance.price <= 0:
            raise serializers.ValidationError("Price must be larger than 0")
        instance.quantity += validated_data.get('quantity', instance.quantity)
        if instance.quantity < 0:
            instance.quantity = 0
        instance.save()
        return instance

    class Meta:
        model = Product
        fields = ('id', 'man_name', 'model_name', 'price', 'quantity')


class SyncSerializer(serializers.Serializer):
    new = ProductSerializer(many=True)
    old = ProductSerializer(many=True)

    class Meta:
        fields = ('products',)
