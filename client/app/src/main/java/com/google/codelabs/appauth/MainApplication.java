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

public class MainApplication extends Application {

  public static final String LOG_TAG = "AppAuthSample";
  public static final String SERVER_ADDR = "http://192.168.1.11:8001/";
  public static final String OWN_OAUTH_ADDR = "http://192.168.1.11:8000/";
  public static String access_token = "";
  public static String OWN_CLIENT_ID = "O9uCHE6i96mHXnC7M8ehbRymGyJPTkgKCvhQPTAz";
  public static String OWN_CLIENT_SECRET = "0amdvcIRcvj8vl0khrPxC5eG80XPvnxNYUHZ8CR2qid4hAxDsyuAHTwGsj0AhPnCb2HUAcuoslziAyrwqG20PdM32oCkowGSOWjBrIPa6W7somA1Uld4vkX13jDqFI4C";

  @Override
  public void onCreate() {
    super.onCreate();
  }
}
