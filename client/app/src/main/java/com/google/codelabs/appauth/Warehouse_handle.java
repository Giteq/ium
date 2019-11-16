package com.google.codelabs.appauth;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.SERVER_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;


public class Warehouse_handle extends AppCompatActivity {

    private ListView mListView;

    Button mListProd;
    Button mAddProd;
    Button submit;
    Button return_to_main;
    EditText man_name, model_name, quantity, price;

    ArrayList<Product> listViewValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_handle);

        Bundle bundle = getIntent().getExtras();

        mListProd = (Button) findViewById(R.id.list_all_prod);
        submit = (Button) findViewById(R.id.submit);
        return_to_main = (Button) findViewById(R.id.return_to_main);
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

        return_to_main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Warehouse_handle.this, MainActivity.class);
                intent.setAction("from.warehouse_handle");
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(listPairedClickItem);

        changeAddVisibility(View.INVISIBLE);
        changeListVisibility(View.INVISIBLE);
    }

    private void list_prod(){
        ProductAdapter adapter = new ProductAdapter(this, listViewValues);
        mListView.setAdapter(adapter);
    }

    private void get_all_products() {
        listViewValues = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.GET, SERVER_ADDR + "products/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    JSONArray json_array = null;
                    try {
                        json_array = new JSONArray(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < json_array.length(); i++) {
                        try {
                            listViewValues.add(new Product(
                                    json_array.getJSONObject(i).get("man_name").toString(),
                                    json_array.getJSONObject(i).get("model_name").toString(),
                                    Integer.valueOf(json_array.getJSONObject(i).get("price").toString()),
                                    Integer.valueOf(json_array.getJSONObject(i).get("quantity").toString()),
                                    Integer.valueOf(json_array.getJSONObject(i).get("id").toString())
                            ));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    list_prod();
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer "+ access_token);
                return params;
            }
        };
        Log.d("MyOwnToken", access_token);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }


    private AdapterView.OnItemClickListener listPairedClickItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Product prod = (Product) parent.getAdapter().getItem(position);
            Intent intent = new Intent(Warehouse_handle.this, ModifyProduct.class);
            Gson gson = new Gson();
            String json = gson.toJson(prod);
            intent.putExtra("product", json);
            intent.putExtra("access_token", access_token);
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



