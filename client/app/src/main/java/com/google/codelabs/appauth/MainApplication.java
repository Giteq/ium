// Copyright 2016 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.appauth;

import android.app.Application;
import android.widget.Button;

import org.json.JSONArray;

public class MainApplication extends Application {

  public static final String LOG_TAG = "AppAuthSample";
  public static final String SERVER_ADDR = "http://10.0.2.2:8001/";
  public static final String OWN_OAUTH_ADDR = "http://10.0.2.2:8000/";
  public static String access_token = "";
  public static String OWN_CLIENT_ID = "Bdyp1m2NVOoI4qILQwZ9yuFQeabVl1tKiV79KZFG";
  public static String OWN_CLIENT_SECRET = "abWTPhEj7DZp00g3FastrYAahASfnP4aL2cvZEA9cU3HpZiKs7usKJ4RDH9Flk5gNBgkhxmtd306h9nSn1CqroN7Rrpz0z4ZWB89nARwWr7oupfil8S23jC24YV63vrX";
  public static NetworkListener is_net_on = new NetworkListener();
  public static JSONArray json_array;
  public static ProductManager productManager;
  public static JsonFileReader jsonFileReader;
  public static Button net_on_off;

  @Override
  public void onCreate() {
    super.onCreate();
  }
}
