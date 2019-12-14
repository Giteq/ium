# Generated by Django 2.2.7 on 2019-12-13 09:36

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('warehouse', '0004_auto_20191213_1024'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='fullproductinfo',
            name='prod_info',
        ),
        migrations.AddField(
            model_name='fullproductinfo',
            name='prod_info',
            field=models.ForeignKey(default=None, on_delete=django.db.models.deletion.CASCADE, related_name='new_prod_info', to='warehouse.Product'),
            preserve_default=False,
        ),
    ]