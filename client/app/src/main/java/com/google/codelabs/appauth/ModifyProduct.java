package com.google.codelabs.appauth;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.SERVER_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;
import static com.google.codelabs.appauth.MainApplication.is_net_on;
import static com.google.codelabs.appauth.MainApplication.productManager;

public class ModifyProduct extends AppCompatActivity {

    EditText man_name, model_name, quantity, price;
    Button mModify, mPlus, mMinus, mRemove;
    Product product;
    TextView wrong_fields;

    JsonFileReader jsonFileReader;

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
        wrong_fields = (TextView) findViewById(R.id.wrong_fields);

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
                if (is_product_valid(new_prod, product)){
                    wrong_fields.setText("Fields valid");
                    try {
                        send_product(new_prod);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    wrong_fields.setText("Fields not valid");
                }

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
                try {
                    remove_product();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        ContextWrapper c = new ContextWrapper(this);
        jsonFileReader = new JsonFileReader(c.getFilesDir().getPath());
    }

    private boolean is_product_valid(Product new_product, Product old_product){
        if ( new_product.price < 0){
            return false;
        }
        return true;
    }

    private void send_product(final Product new_prod) throws IOException, JSONException {
        productManager.modifyProduct(new_prod, product);
        Intent intent = new Intent(ModifyProduct.this, Warehouse_handle.class);
        //intent.putExtra("Net_stat", is_net_on);
        startActivity(intent);
    }

    private void remove_product() throws JSONException {
        productManager.removeProduct(product);
        Intent intent = new Intent(ModifyProduct.this, Warehouse_handle.class);
        //intent.putExtra("Net_stat", is_net_on);
        startActivity(intent);
    }
}
