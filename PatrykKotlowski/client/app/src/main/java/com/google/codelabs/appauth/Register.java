package com.google.codelabs.appauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apptakk.http_request.HttpRequest;
import com.apptakk.http_request.HttpRequestTask;
import com.apptakk.http_request.HttpResponse;
import com.google.codelabs.appauth.Login;
import com.google.codelabs.appauth.R;
import com.google.codelabs.appauth.Warehouse_handle;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.OWN_CLIENT_ID;
import static com.google.codelabs.appauth.MainApplication.OWN_CLIENT_SECRET;
import static com.google.codelabs.appauth.MainApplication.OWN_OAUTH_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;

public class Register extends AppCompatActivity {

    Button submit;
    EditText login;
    EditText password;
    EditText retype_password;
    TextView wrong_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        login = (EditText)findViewById(R.id.reg_login);
        password = (EditText)findViewById(R.id.reg_password);
        retype_password = (EditText)findViewById(R.id.sec_password);
        submit = (Button) findViewById(R.id.register_submit);
        wrong_pass = (TextView) findViewById(R.id.wrong_pass_textview);

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (password.getText().toString().compareTo(retype_password.getText().toString()) == 0) {
                    wrong_pass.setVisibility(View.GONE);
                    JSONObject jObjectType = new JSONObject();
                    try {
                        jObjectType.put("password", password.getText().toString());
                        jObjectType.put("username", login.getText().toString());
                        jObjectType.put("email", "aasdasdsad@wp.pl");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new HttpRequestTask(
                            new HttpRequest(OWN_OAUTH_ADDR + "users/register", HttpRequest.POST, jObjectType.toString()),
                            new HttpRequest.Handler() {
                                @Override
                                public void response(HttpResponse response) {
                                    if (response.code == 201) {
                                        Intent intent = new Intent(Register.this, MainActivity.class);
                                        intent.setAction("from.register");
                                        startActivity(intent);
                                    } else {
                                        Log.e(LOG_TAG, "Request unsuccessful: " + response);
                                    }
                                }
                            }).execute();
                }
                else{
                    wrong_pass.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
