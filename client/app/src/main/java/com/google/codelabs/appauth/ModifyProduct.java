package com.google.codelabs.appauth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class ModifyProduct extends AppCompatActivity {

    EditText man_name, model_name, quantity, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_product);

        man_name = (EditText)findViewById(R.id.man_name);
        model_name = (EditText)findViewById(R.id.model_name);
        quantity = (EditText)findViewById(R.id.quantity);
        price = (EditText)findViewById(R.id.price);
        
    }
}
