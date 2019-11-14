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

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.OWN_CLIENT_ID;
import static com.google.codelabs.appauth.MainApplication.OWN_CLIENT_SECRET;
import static com.google.codelabs.appauth.MainApplication.OWN_OAUTH_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;

public class Login extends AppCompatActivity {

    Button submit;
    EditText login;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (EditText)findViewById(R.id.login);
        password = (EditText)findViewById(R.id.password);

        submit = (Button) findViewById(R.id.login_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                JSONObject jObjectType = new JSONObject();
                try {
                    jObjectType.put("grant_type", "password");
                    jObjectType.put("client_id", OWN_CLIENT_ID);
                    jObjectType.put("client_secret", OWN_CLIENT_SECRET);
                    jObjectType.put("password", password.getText().toString());
                    jObjectType.put("username", login.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(LOG_TAG, "Start sending");
                new HttpRequestTask(
                        new HttpRequest(OWN_OAUTH_ADDR + "auth/token", HttpRequest.POST, jObjectType.toString()),
                        new HttpRequest.Handler() {
                            @Override
                            public void response(HttpResponse response) {
                                if (response.code == 200) {
                                    try {
                                        JSONObject json = new JSONObject(response.body);
                                        access_token = json.get("access_token").toString();
                                        Intent intent = new Intent(Login.this, Warehouse_handle.class);
                                        intent.putExtra("access_token", access_token);
                                        startActivity(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.e(LOG_TAG, "Request unsuccessful: " + response);
                                }
                            }
                        }).execute();
            }
        });

    }
}
