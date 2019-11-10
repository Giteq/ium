package com.google.codelabs.appauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.SERVER_ADDR;

public class Warehouse_handle extends AppCompatActivity {

    private ListView mListView;

    Button mListProd;
    Button mAddProd;
    Button submit;
    EditText man_name, model_name, quantity, price;

    ArrayList<Product> listViewValues;
    String access_token;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_handle);

        Bundle bundle = getIntent().getExtras();
        access_token = bundle.getString("access_token");

        mListProd = (Button) findViewById(R.id.list_all_prod);
        submit = (Button) findViewById(R.id.submit);
        man_name = (EditText)findViewById(R.id.man_name);
        model_name = (EditText)findViewById(R.id.model_name);
        quantity = (EditText)findViewById(R.id.quantity);
        price = (EditText)findViewById(R.id.price);
        mAddProd = (Button) findViewById(R.id.add_prod);
        mListView = (ListView) findViewById(R.id.some_list);

        mAddProd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeAddVisibility(View.VISIBLE);
                changeListVisibility(View.INVISIBLE);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewProduct();
            }
        });

        mListProd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeAddVisibility(View.INVISIBLE);
                changeListVisibility(View.VISIBLE);
                get_all_products();
            }
        });

        mListView.setOnItemClickListener(listPairedClickItem);

        changeAddVisibility(View.INVISIBLE);
        changeListVisibility(View.INVISIBLE);
    }

    public void list_all_products(){
        // Initialise a listview adapter with the project titles and use it
        // in the listview to show the list of project.
        ProductAdapter adapter = new ProductAdapter(this, listViewValues);
        mListView.setAdapter(adapter);

    }

    private void get_all_products(){
        listViewValues = new ArrayList<>();
        new HttpRequestTask(
                new HttpRequest(SERVER_ADDR + "products/", HttpRequest.GET),
                new HttpRequest.Handler() {
                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            try {
                                JSONArray json_array = new JSONArray(response.body);
                                Log.d(LOG_TAG, json_array.toString());
                                // Add the project titles to display in a list for the listview adapter.
                                for (int i = 0; i < json_array.length(); i++) {
                                    listViewValues.add(new Product(
                                            json_array.getJSONObject(i).get("man_name").toString(),
                                            json_array.getJSONObject(i).get("model_name").toString(),
                                            Integer.valueOf(json_array.getJSONObject(i).get("price").toString()),
                                            Integer.valueOf(json_array.getJSONObject(i).get("quantity").toString())
                                    ));
                                }
                                list_all_products();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(LOG_TAG, "Request unsuccessful: " + response);
                        }
                    }
                }).execute();

    }

    private AdapterView.OnItemClickListener listPairedClickItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Product prod = (Product) parent.getAdapter().getItem(position);
            Intent intent = new Intent(Warehouse_handle.this, ModifyProduct.class);
            intent.putExtra("Product", prod);
            startActivity(intent);
        }
    };

    private void addNewProduct(){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("man_name", man_name.getText());
            jsonObject.put("model_name", model_name.getText());
            jsonObject.put("price", price.getText());
            jsonObject.put("quantity", quantity.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new HttpRequestTask(
                new HttpRequest(SERVER_ADDR + "products/",
                        HttpRequest.POST,
                        jsonObject.toString(),
                        "Bearer " + access_token),
                new HttpRequest.Handler() {
                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            Log.d(LOG_TAG, response.body);
                        } else {
                            Log.e(LOG_TAG, "Request unsuccessful: " + response);
                        }
                    }
                }).execute();
        quantity.setText("");
        price.setText("");
        model_name.setText("");
        man_name.setText("");

    }

    private void changeAddVisibility(int visibility){
        man_name.setVisibility(visibility);
        model_name.setVisibility(visibility);
        quantity.setVisibility(visibility);
        price.setVisibility(visibility);
        submit.setVisibility(visibility);
    }

    private void changeListVisibility(int visibility){
        mListView.setVisibility(visibility);
    }

}


