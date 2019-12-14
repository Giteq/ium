package com.google.codelabs.appauth;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.SERVER_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;
import static com.google.codelabs.appauth.MainApplication.is_net_on;


public class Warehouse_handle extends AppCompatActivity {

    private ListView mListView;

    Button mListProd;
    Button mAddProd;
    Button submit;
    Button return_to_main, net_on_off;
    EditText man_name, model_name, quantity, price;

    ArrayList<Product> listViewValues;

    JSONArray json_array;
    JsonFileReader jsonFileReader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_handle);
        mListProd = (Button) findViewById(R.id.list_all_prod);
        submit = (Button) findViewById(R.id.submit);
        return_to_main = (Button) findViewById(R.id.return_to_main);
        man_name = (EditText)findViewById(R.id.man_name);
        model_name = (EditText)findViewById(R.id.model_name);
        quantity = (EditText)findViewById(R.id.quantity);
        price = (EditText)findViewById(R.id.price);
        mAddProd = (Button) findViewById(R.id.add_prod);
        net_on_off = (Button) findViewById(R.id.net_on_off);
        mListView = (ListView) findViewById(R.id.some_list);

        if (net_on_off.getText().equals("UNDEFINED")){
            net_on_off.setText("Turn NET OFF");
            is_net_on = true;
        }

        mAddProd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeAddVisibility(View.VISIBLE);
                changeListVisibility(View.INVISIBLE);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    addNewProduct();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        mListProd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                changeAddVisibility(View.INVISIBLE);
                changeListVisibility(View.VISIBLE);
                try {
                    get_all_products();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        return_to_main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Warehouse_handle.this, MainActivity.class);
                intent.setAction("android.intent.action.net");
                startActivity(intent);
            }
        });

        net_on_off.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                net_on_off();
            }
        });

        mListView.setOnItemClickListener(listPairedClickItem);

        changeAddVisibility(View.INVISIBLE);
        changeListVisibility(View.INVISIBLE);

        ContextWrapper c = new ContextWrapper(this);
        jsonFileReader = new JsonFileReader(c.getFilesDir().getPath());

        is_net_on = getIntent().getBooleanExtra("Net_stat", true);
        change_net_button();
    }

    private void list_prod(){
        ProductAdapter adapter = new ProductAdapter(this, listViewValues);
        mListView.setAdapter(adapter);
    }

    private void get_all_products() throws IOException, JSONException, ClassNotFoundException {

        listViewValues = new ArrayList<>();

        if (is_net_on){
            StringRequest request = new StringRequest(Request.Method.GET, SERVER_ADDR + "products/", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!response.equals(null)) {
                        try {
                            jsonFileReader.backup();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        try {
                            json_array = new JSONArray(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            jsonFileReader.clearDirectory();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        for (int i = 0; i < json_array.length(); i++) {
                            try {
                                jsonFileReader.writeToFile(json_array.getJSONObject(i));
                                listViewValues.add(jsonFileReader.jsonToProd(json_array.getJSONObject(i)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(LOG_TAG, "IO Error");
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
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
        }
        else{
            json_array = jsonFileReader.readJsonArrayFromFiles();
            for (int i = 0; i < json_array.length(); i++) {
                try {
                    jsonFileReader.writeToFile(json_array.getJSONObject(i));
                    listViewValues.add(jsonFileReader.jsonToProd(json_array.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "IO Error");
                }
            }
            list_prod();
        }

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
            intent.putExtra("sizeOfArray", json_array.length());
            startActivity(intent);
        }
    };

    private void addNewProduct() throws IOException, JSONException, ClassNotFoundException {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("man_name", man_name.getText());
            jsonObject.put("model_name", model_name.getText());
            jsonObject.put("price", price.getText());
            jsonObject.put("quantity", quantity.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (is_net_on){
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
        }
        else{
            jsonObject.put("id", -1);
            jsonFileReader.writeToFile(jsonObject);
        }
        quantity.setText("");
        price.setText("");
        model_name.setText("");
        man_name.setText("");

        get_all_products();

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

    private void net_on_off() {
        is_net_on = !is_net_on;
        change_net_button();
    }

    private void change_net_button(){
        if (is_net_on){
            net_on_off.setText("Turn NET OFF");
            try {
                sync_state();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            net_on_off.setText("Turn NET ON");
        }
    }

    private void sync_state() throws IOException, JSONException, ClassNotFoundException {
        JSONArray new_prods = jsonFileReader.readJsonArrayFromFiles();
        JSONArray backup = jsonFileReader.readJsonArrayFromBackup();
        JSONObject to_send = new JSONObject();
        to_send.put("old", backup);
        to_send.put("new", new_prods);

        new HttpRequestTask(
                new HttpRequest(SERVER_ADDR + "sync/",
                        HttpRequest.PUT,
                        to_send.toString(),
                        "Bearer " + access_token),
                new HttpRequest.Handler() {
                    @Override
                    public void response(HttpResponse response) {
                        if (response.code == 200) {
                            try {
                                get_all_products();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else if (response.code == 300){
                            //TODO message box
                        } else {
                            Log.e(LOG_TAG, "Request unsuccessful: " + response);
                        }
                    }
                }).execute();
    }
}



