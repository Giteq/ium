package com.google.codelabs.appauth;

public class Product {
    public String man_name;
    public String model_name;
    public Integer price;
    public Integer quantity;
    public Integer id;

    public Product(String man_name, String model_name, Integer price, Integer quantity, Integer id) {
        this.man_name = man_name;
        this.model_name = model_name;
        this.price = price;
        this.quantity = quantity;
        this.id = id;
    }

}
