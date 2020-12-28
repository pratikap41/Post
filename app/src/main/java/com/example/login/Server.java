package com.example.login;

import android.app.Application;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class Server extends Application {

    public static final String APPLICATION_ID = "D5FF8EDD-0293-8D54-FF68-D3AA86A13700";
    public static final String API_KEY = "CAD3CC5F-82A1-4B5E-A6EE-DDE4D9800EC7";
    public static final String SERVER_URL = "https://api.backendless.com";
    public static final String REST_API_KEY = "3679BA87-EC28-4666-910A-40FE3AD998DD";

    public static final String ImageUrl = "https://backendlessappcontent.com/" + APPLICATION_ID + "/" + REST_API_KEY + "/files/";
    public static BackendlessUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        Backendless.setUrl(SERVER_URL);
        Backendless.initApp(getApplicationContext(), APPLICATION_ID, API_KEY);
    }
}
