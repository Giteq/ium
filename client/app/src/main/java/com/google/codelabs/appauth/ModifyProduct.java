package com.google.codelabs.appauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.google.gson.Gson;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.SERVER_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;

public class ModifyProduct extends AppCompatActivity {

    EditText man_name, model_name, quantity, price;
    Button mModify, mPlus, mMinus, mRemove;
    Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_product);

        Bundle bundle = getIntent().getExtras();

        final String productString = bundle.getString("product");
        Gson gson = new Gson();
        product = gson.fromJson(productString, Product.class);


        Log.d(LOG_TAG, product.man_name);
        man_name = (EditText)findViewById(R.id.modifyManName);
        model_name = (EditText)findViewById(R.id.modifyModelName);
        quantity = (EditText)findViewById(R.id.modifyQuantity);
        price = (EditText)findViewById(R.id.modifyPrice);
        mModify = (Button) findViewById(R.id.modifyModify);
        mRemove = (Button) findViewById(R.id.modifyRemove);
        mPlus = (Button) findViewById(R.id.modifyPlus);
        mMinus = (Button) findViewById(R.id.modifyMinus);

        man_name.setText(product.man_name);
        model_name.setText(product.model_name);
        quantity.setText(product.quantity.toString());
        price.setText(product.price.toString());

        mModify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Product new_prod = new Product(
                        man_name.getText().toString(),
                        model_name.getText().toString(),
                        Integer.valueOf(price.getText().toString()),
                        Integer.valueOf(quantity.getText().toString()) - product.quantity,
                        product.id
                );
                Gson gson = new Gson();
                String json = gson.toJson(new_prod);
                Log.d(LOG_TAG, json);
                new HttpRequestTask(
                        new HttpRequest(SERVER_ADDR + "products/" + product.id.toString() + "/",
                                HttpRequest.PUT,
                                json,
                                "Bearer " + access_token),
                        new HttpRequest.Handler() {
                            @Override
                            public void response(HttpResponse response) {
                                if (response.code == 200) {

                                } else {
                                    Log.e(LOG_TAG, "Request unsuccessful: " + response);
                                }
                            }
                        }).execute();
                Intent intent = new Intent(ModifyProduct.this, Warehouse_handle.class);
                intent.putExtra("tmp", "tmp");
                startActivity(intent);
            }
        });

        mPlus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer actual_value = Integer.valueOf(quantity.getText().toString()) + 1;
                quantity.setText(actual_value.toString());
            }
        });

        mMinus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer actual_value = Integer.valueOf(quantity.getText().toString()) - 1;
                quantity.setText(actual_value.toString());
            }
        });

        mRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new HttpRequestTask(
                        new HttpRequest(SERVER_ADDR + "products/" + product.id.toString() + "/",
                                HttpRequest.DELETE,
                                "{}",
                                "Bearer " + access_token),
                        new HttpRequest.Handler() {
                            @Override
                            public void response(HttpResponse response) {
                                if (response.code == 200) {

                                } else {
                                    Log.e(LOG_TAG, "Request unsuccessful: " + response);
                                }
                            }
                        }).execute();
                Intent intent = new Intent(ModifyProduct.this, Warehouse_handle.class);
                intent.putExtra("tmp", "tmp");
                startActivity(intent);
            }
        });
    }
}
