package com.google.codelabs.appauth;

import android.util.Log;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.json_array;

public class ListRequestTask implements HttpRequest.Handler {

    JsonFileReader jsonFileReader;
    boolean isFinish = false;

    public ListRequestTask(JsonFileReader jsonFileReader){
        this.jsonFileReader = jsonFileReader;
    }

    @Override
    public void response(HttpResponse response) {
        if (response.code == 200) {
            isFinish = true;
            try {
                json_array = new JSONArray(response.body);
                jsonFileReader.clearDirectory();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < json_array.length(); i++) {
                try {
                    jsonFileReader.writeToFile(json_array.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                jsonFileReader.backup();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "Request unsuccessful: " + response);
        }
    }

    public boolean isFinish(){
        return isFinish;
    }

}
