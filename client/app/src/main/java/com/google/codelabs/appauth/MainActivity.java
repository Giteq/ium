package com.google.codelabs.appauth;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;
import static com.google.codelabs.appauth.MainApplication.OWN_CLIENT_ID;
import static com.google.codelabs.appauth.MainApplication.OWN_CLIENT_SECRET;
import static com.google.codelabs.appauth.MainApplication.OWN_OAUTH_ADDR;
import static com.google.codelabs.appauth.MainApplication.access_token;

public class MainActivity extends AppCompatActivity {

  private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
  private static final String AUTH_STATE = "AUTH_STATE";
  private static final String USED_INTENT = "USED_INTENT";
  private static final String LOGIN_HINT = "login_hint";
  static String GOOGLE_CLIENT_ID = "276416597205-fdi3s21e6dshg9384c8rgsj1ef28h6rr.apps.googleusercontent.com";

  MainApplication mMainApplication;

  // state
  AuthState mAuthState;

  // views
  AppCompatButton mAuthorize;
  AppCompatButton mSignOut;
  AppCompatButton mOwnAuthorize;
  AppCompatButton mRegister;

  // login hint
  protected String mLoginHint;

  // broadcast receiver for app restrictions changed broadcast
  BroadcastReceiver mRestrictionsReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mMainApplication = (MainApplication) getApplication();
    mAuthorize = (AppCompatButton) findViewById(R.id.authorize);
    mSignOut = (AppCompatButton) findViewById(R.id.signOut);
    mOwnAuthorize = (AppCompatButton) findViewById(R.id.own_authorize);
    mRegister = (AppCompatButton) findViewById(R.id.register);

    enablePostAuthorizationFlows();

    // wire click listeners
    mAuthorize.setOnClickListener(new AuthorizeListener(this));
    mOwnAuthorize.setOnClickListener(new OwnAuthorizeListener(this));
    mRegister.setOnClickListener(new RegisterListener(this));

    // Retrieve app restrictions and take appropriate action
    getAppRestrictions();
  }

  @Override
  protected void onResume(){
    super.onResume();

    // Retrieve app restrictions and take appropriate action
    getAppRestrictions();

    // Register a receiver for app restrictions changed broadcast
    registerRestrictionsReceiver();
  }

  @Override
  protected void onStop(){
    super.onStop();

    // Unregister receiver for app restrictions changed broadcast
    unregisterReceiver(mRestrictionsReceiver);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    try {
      checkIntent(intent);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void checkIntent(@Nullable Intent intent) throws JSONException {
    if (intent != null) {
      String action = intent.getAction();
      switch (action) {
        case "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE":
          if (!intent.hasExtra(USED_INTENT)) {
            handleAuthorizationResponse(intent);
            intent.putExtra(USED_INTENT, true);
          }
          break;
        default:
        break;
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    try {
      checkIntent(getIntent());
    } catch (JSONException e) {
      e.printStackTrace();
    }

    // Register a receiver for app restrictions changed broadcast
    registerRestrictionsReceiver();
  }

  private void enablePostAuthorizationFlows() {
    mAuthState = restoreAuthState();
    if (mAuthState != null && mAuthState.isAuthorized()) {
      if (mSignOut.getVisibility() == View.GONE) {
        mSignOut.setVisibility(View.VISIBLE);
        mSignOut.setOnClickListener(new SignOutListener(this));
      }
    } else {
      mSignOut.setVisibility(View.GONE);
    }
  }

  /**
   * Exchanges the code, for the {@link TokenResponse}.
   *
   * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
   */
  private void handleAuthorizationResponse(@NonNull Intent intent) throws JSONException {
    AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
    AuthorizationException error = AuthorizationException.fromIntent(intent);
    final AuthState authState = new AuthState(response, error);
    if (response != null) {
      Log.i(LOG_TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
      AuthorizationService service = new AuthorizationService(this);
      service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
        @Override
        public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
          if (exception != null) {
            Log.w(LOG_TAG, "Token Exchange failed", exception);
          } else {
            if (tokenResponse != null) {
              authState.update(tokenResponse, exception);
              persistAuthState(authState);
              Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));
              convertToken(tokenResponse);
            }
          }
        }
      });


    }
  }

  private void convertToken(TokenResponse tokenResponse){
    JSONObject jObjectType = new JSONObject();
    try {
        jObjectType.put("grant_type", "convert_token");
        jObjectType.put("client_id", OWN_CLIENT_ID);
        jObjectType.put("client_secret", OWN_CLIENT_SECRET);
        jObjectType.put("backend", "google-oauth2");
        jObjectType.put("token", tokenResponse.accessToken.toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }
      Log.d(LOG_TAG, "Start sending");
      new HttpRequestTask(
              new HttpRequest(OWN_OAUTH_ADDR + "auth/convert-token", HttpRequest.POST, jObjectType.toString()),
              new HttpRequest.Handler() {
                @Override
                public void response(HttpResponse response) {
                  if (response.code == 200) {
                    try {
                      JSONObject json = new JSONObject(response.body);
                      access_token = json.get("access_token").toString();
                      change_activity();
                    } catch (JSONException e) {
                      e.printStackTrace();
                    }
                  } else {
                    Log.e(LOG_TAG, "Request unsuccessful: " + response);
                  }
                }
              }).execute();
  }

  private void change_activity(){
    Intent intent = new Intent(MainActivity.this, Warehouse_handle.class);
    intent.putExtra("access_token", access_token);
    startActivity(intent);
  }

  private void persistAuthState(@NonNull AuthState authState) {
    getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
        .putString(AUTH_STATE, authState.toJsonString())
        .commit();
    enablePostAuthorizationFlows();
  }

  private void clearAuthState() {
    getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove(AUTH_STATE)
        .apply();
  }

  @Nullable
  private AuthState restoreAuthState() {
    String jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .getString(AUTH_STATE, null);
    if (!TextUtils.isEmpty(jsonString)) {
      try {
        return AuthState.fromJson(jsonString);
      } catch (JSONException jsonException) {
        // should never happen
      }
    }
    return null;
  }

  /**
   * Kicks off the authorization flow.
   */
  public static class AuthorizeListener implements Button.OnClickListener {

    private final MainActivity mMainActivity;

    public AuthorizeListener(@NonNull MainActivity mainActivity) {
      mMainActivity = mainActivity;
    }

    @Override
    public void onClick(View view) {
      AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
          Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
          Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
      );
      AuthorizationService authorizationService = new AuthorizationService(view.getContext());
      String clientId = GOOGLE_CLIENT_ID;
      Uri redirectUri = Uri.parse("com.google.codelabs.appauth:/oauth2callback");
      AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
          serviceConfiguration,
          clientId,
          AuthorizationRequest.RESPONSE_TYPE_CODE,
          redirectUri
      );
      builder.setScopes("profile");

      if(mMainActivity.getLoginHint() != null){
        Map loginHintMap = new HashMap<String, String>();
        loginHintMap.put(LOGIN_HINT,mMainActivity.getLoginHint());
        builder.setAdditionalParameters(loginHintMap);

        Log.i(LOG_TAG, String.format("login_hint: %s", mMainActivity.getLoginHint()));
      }

      AuthorizationRequest request = builder.build();
      String action = "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE";
      Intent postAuthorizationIntent = new Intent(action);
      PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), request.hashCode(), postAuthorizationIntent, 0);
      authorizationService.performAuthorizationRequest(request, pendingIntent);
    }
  }

  /**
   * Kicks off the authorization flow.
   */
  public class OwnAuthorizeListener implements Button.OnClickListener {

    private final MainActivity mMainActivity;

    public OwnAuthorizeListener(@NonNull MainActivity mainActivity) {
      mMainActivity = mainActivity;
    }
  //http://localhost:8000/authorize?client_id=651462&redirect_uri=http://example.org/&response_type=code&scope=openid email profile&state=123123
    @Override
    public void onClick(View view) {
      Intent intent = new Intent(MainActivity.this, Login.class);
      intent.putExtra("access_token", access_token);
      startActivity(intent);
  }

  }

  /**
   * Kicks off the authorization flow.
   */
  public class RegisterListener implements Button.OnClickListener {

    private final MainActivity mMainActivity;

    public RegisterListener(@NonNull MainActivity mainActivity) {
      mMainActivity = mainActivity;
    }
    @Override
    public void onClick(View view) {
      Intent intent = new Intent(MainActivity.this, Register.class);
      intent.putExtra("access_token", access_token);
      startActivity(intent);
    }

  }

  public static class SignOutListener implements Button.OnClickListener {

    private final MainActivity mMainActivity;

    public SignOutListener(@NonNull MainActivity mainActivity) {
      mMainActivity = mainActivity;
    }

    @Override
    public void onClick(View view) {
      mMainActivity.mAuthState = null;
      mMainActivity.clearAuthState();
      mMainActivity.enablePostAuthorizationFlows();
      new HttpRequestTask(
              new HttpRequest(OWN_OAUTH_ADDR + "logout/", HttpRequest.GET),
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
  }

  private void getAppRestrictions(){
    RestrictionsManager restrictionsManager =
            (RestrictionsManager) this
                    .getSystemService(Context.RESTRICTIONS_SERVICE);

    Bundle appRestrictions = restrictionsManager.getApplicationRestrictions();

    // Block user if KEY_RESTRICTIONS_PENDING is true, and save login hint if available
    if(!appRestrictions.isEmpty()){
      if(appRestrictions.getBoolean(UserManager.
              KEY_RESTRICTIONS_PENDING)!=true){
        mLoginHint = appRestrictions.getString(LOGIN_HINT);
      }
      else {
        Toast.makeText(this,R.string.restrictions_pending_block_user,
                Toast.LENGTH_LONG).show();
        finish();
      }
    }
  }

  private void registerRestrictionsReceiver(){
    IntentFilter restrictionsFilter =
            new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);

    mRestrictionsReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        getAppRestrictions();
      }
    };

    registerReceiver(mRestrictionsReceiver, restrictionsFilter);
  }

  public String getLoginHint(){
    return mLoginHint;
  }
}
