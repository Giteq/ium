package com.google.codelabs.appauth;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.google.codelabs.appauth.MainApplication.access_token;
import static com.google.codelabs.appauth.MainApplication.is_net_on;
import static com.google.codelabs.appauth.MainApplication.jsonFileReader;
import static com.google.codelabs.appauth.MainApplication.json_array;
import static com.google.codelabs.appauth.MainApplication.net_on_off;
import static com.google.codelabs.appauth.MainApplication.productManager;


public class Warehouse_handle extends AppCompatActivity {

    private ListView mListView;

    Button mListProd;
    Button mAddProd;
    Button submit;
    Button return_to_main;
    EditText man_name, model_name, quantity, price;

    ArrayList<Product> listViewValues = new ArrayList<>();

    int sync_cnt = 0;

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


        if (is_net_on.isActive()){
            net_on_off.setText("Turn NET OFF");
        }
        else{
            net_on_off.setText("Turn NET ON");
        }

    }

    private void get_all_products() throws IOException, JSONException, ClassNotFoundException {
        listViewValues = new ArrayList<>();
        Handler handler=new Handler();
        productManager.get_products();
        if (is_net_on.isActive()){
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground( final Void ... params ) {
                    while (!productManager.isFinish());

                    return null;
                }

                @Override
                protected void onPostExecute( final Void result ) {
                    list_prods();
                }
            }.execute();
        }
        else{
            list_prods();
        }

    }

    private void list_prods(){
        for (int i = 0; i < json_array.length(); i++) {
            try {
                listViewValues.add(jsonFileReader.jsonToProd(json_array.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ProductAdapter adapter = new ProductAdapter(this, listViewValues);
        mListView.setAdapter(adapter);
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
        productManager.add_product(jsonObject);
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
        is_net_on.setActive(!is_net_on.isActive());
    }

}



