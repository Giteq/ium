package com.google.codelabs.appauth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.SERVER_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;
import static com.google.codelabs.appauth.MainApplication.is_net_on;
import static com.google.codelabs.appauth.MainApplication.json_array;


public class ProductManager {

    JsonFileReader jsonFileReader;
    Context context;
    ListRequestTask requestTask;

    public ProductManager(JsonFileReader jsonFileReader, Context context){
        this.jsonFileReader = jsonFileReader;
        this.context = context;
        this.requestTask = new ListRequestTask(jsonFileReader);
    }

    public boolean get_products() throws IOException, JSONException, ClassNotFoundException {
        if (is_net_on.isActive()) {
            return listRequestHandle(jsonFileReader, context);
        }
        else {
            json_array = jsonFileReader.readJsonArrayFromFiles();
        }
        return false;
    }

    public void add_product(JSONObject jsonObject) throws JSONException, IOException {
        if (is_net_on.isActive()){
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
                            } else if (response.code == 302){
                                Toast.makeText(context, response.body, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Log.e(LOG_TAG, "Request unsuccessful: " + response);
                            }
                        }
                    }).execute();
        }
        else{
            jsonObject.put("id", -1);
            jsonFileReader.writeToFile(jsonObject);
        }
    }

    public void sync_state() throws IOException, JSONException, ClassNotFoundException {
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
                                //TODO get_all_products();
                        } else if (response.code == 300){
                            Toast.makeText(context, response.body, Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(LOG_TAG, "Request unsuccessful: " + response);
                        }
                    }
                }).execute();
    }

    public void modifyProduct(final Product new_prod, final Product old_prod) throws JSONException, IOException {
        final JSONObject new_prod_to_send = new JSONObject();
        new_prod_to_send.put("man_name", new_prod.man_name);
        if ((new_prod.quantity != 0)){
            new_prod_to_send.put("quantity", new_prod.quantity);
        }
        if (!new_prod.price.equals(old_prod.price)){
            new_prod_to_send.put("price", new_prod.price - old_prod.price);
        }
        if (!new_prod.model_name.equals(old_prod.model_name)){
            new_prod_to_send.put("model_name", new_prod.model_name);
        }

        JSONArray backup = null;
        try {
            backup = jsonFileReader.readJsonArrayFromBackup();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        final JSONObject to_send = new JSONObject();
        to_send.put("old", backup);
        to_send.put("new", new_prod_to_send);

        if (is_net_on.isActive()){
            new HttpRequestTask(
                    new HttpRequest(SERVER_ADDR + "products/" + old_prod.id.toString() + "/",
                            HttpRequest.PUT,
                            to_send.toString(),
                            "Bearer " + access_token),
                    new HttpRequest.Handler() {
                        @Override
                        public void response(HttpResponse response) {
                            if (response.code == 200) {
                                //request successfull -> save to file
                                try {
                                    JSONObject toWrite = new JSONObject();
                                    toWrite.put("id", new_prod.id);
                                    toWrite.put("man_name", new_prod.man_name);
                                    toWrite.put("quantity", new_prod.quantity + old_prod.quantity);
                                    toWrite.put("model_name", new_prod.model_name);
                                    toWrite.put("price", new_prod.price);
                                    jsonFileReader.writeToFile(toWrite);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (response.code == 300){
                                Toast.makeText(context, response.body, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Log.e(LOG_TAG, "Request unsuccessful: " + response);
                            }
                        }
                    }).execute();
        }
        else{
            JSONObject toWrite = new JSONObject();
            toWrite.put("id", new_prod.id);
            toWrite.put("man_name", new_prod.man_name);
            toWrite.put("quantity", new_prod.quantity + old_prod.quantity);
            toWrite.put("model_name", new_prod.model_name);
            toWrite.put("price", new_prod.price);
            jsonFileReader.writeToFile(toWrite);
        }
    }

    public void removeProduct(Product product) throws JSONException, IOException, ClassNotFoundException {
        Gson gson = new Gson();
        final String prod = gson.toJson(product);
        final JSONObject prod_info = new JSONObject(prod);

        JSONArray backup = null;
        try {
            backup = jsonFileReader.readJsonArrayFromBackup();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        final JSONObject to_send = new JSONObject();
        to_send.put("old", backup);
        to_send.put("to_remove", prod_info);


        if (is_net_on.isActive()){
            new HttpRequestTask(
                    new HttpRequest(SERVER_ADDR + "products/" + product.id.toString() + "/",
                            HttpRequest.DELETE,
                            to_send.toString(),
                            "Bearer " + access_token),
                    new HttpRequest.Handler() {
                        @Override
                        public void response(HttpResponse response) {
                            if (response.code == 200) {

                            } else if (response.code == 300){
                                Toast.makeText(context, response.body, Toast.LENGTH_LONG).show();
                            }
                                else {
                                Log.e(LOG_TAG, "Request unsuccessful: " + response);
                            }
                        }
                    }).execute();
        }
        else{
            jsonFileReader.removeFile(prod_info);
        }
    }

    private boolean listRequestHandle(final JsonFileReader jsonFileReader, Context context){
        HttpRequestTask task = new HttpRequestTask(
                    new HttpRequest(SERVER_ADDR + "products/",
                            HttpRequest.PUT,
                            "{}",
                            "Bearer " + access_token),
                requestTask
            );
            task.execute();

            return requestTask.isFinish();
    }

    public boolean isFinish(){
        return  requestTask.isFinish();
    }

}
