package com.google.codelabs.appauth;

import android.content.Context;
import android.util.Log;
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
    RequestTask requestTask;

    public ProductManager(JsonFileReader jsonFileReader, Context context){
        this.jsonFileReader = jsonFileReader;
        this.context = context;
        this.requestTask = new RequestTask(jsonFileReader);
    }

    public boolean get_products() throws IOException, JSONException, ClassNotFoundException {
        if (is_net_on.isBoo()) {
            return listRequestHandle(jsonFileReader, context);
        }
        else {
            json_array = jsonFileReader.readJsonArrayFromFiles();
        }
        return false;
    }

    public void add_product(JSONObject jsonObject) throws JSONException, IOException {
        if (is_net_on.isBoo()){
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
                            //TODO message box
                        } else {
                            Log.e(LOG_TAG, "Request unsuccessful: " + response);
                        }
                    }
                }).execute();
    }

    public void modifyProduct(Product new_prod, Product old_prod) throws JSONException, IOException {
        Gson gson = new Gson();
        final String s_new_prod_info = gson.toJson(new_prod);
        final JSONObject new_prod_info = new JSONObject(s_new_prod_info);

        if (is_net_on.isBoo()){
            new HttpRequestTask(
                    new HttpRequest(SERVER_ADDR + "products/" + old_prod.id.toString() + "/",
                            HttpRequest.PUT,
                            s_new_prod_info,
                            "Bearer " + access_token),
                    new HttpRequest.Handler() {
                        @Override
                        public void response(HttpResponse response) {
                            if (response.code == 200) {
                                //request successfull -> save to file
                                try {
                                    new_prod_info.put("quantity", new_prod_info.get("quantity"));
                                    jsonFileReader.writeToFile(new_prod_info);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e(LOG_TAG, "Request unsuccessful: " + response);
                            }
                        }
                    }).execute();
        }
        else{
            new_prod_info.put("quantity", new_prod_info.get("quantity"));
            jsonFileReader.writeToFile(new_prod_info);
        }
    }

    public void removeProduct(Product product) throws JSONException {
        Gson gson = new Gson();
        final String prod = gson.toJson(product);
        final JSONObject prod_info = new JSONObject(prod);
        if (is_net_on.isBoo()){
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
