# Generated by Django 2.2.7 on 2019-12-16 16:53

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('warehouse', '0008_auto_20191216_1643'),
    ]

    operations = [
        migrations.CreateModel(
            name='ProductDiff',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('user_name', models.CharField(max_length=200)),
                ('man_name', models.CharField(default='', max_length=100)),
                ('diff', models.IntegerField()),
            ],
            options={
                'ordering': ['man_name'],
            },
        ),
    ]