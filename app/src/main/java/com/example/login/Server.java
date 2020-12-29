package com.example.login;

import android.app.Application;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class Server extends Application {

    public static final String APPLICATION_ID = "D5FF8EDD-0293-8D54-FF68-D3AA86A13700";
    public static final String API_KEY = "DA859CC6-22F5-4707-9D7F-8FA184A4A024";
    public static final String SERVER_URL = "https://api.backendless.com";
    public static final String REST_API_KEY = "CC14847A-4F3A-4F49-8F93-DA2742453516";
    public static final String ImageUrl = "https://backendlessappcontent.com/" + APPLICATION_ID + "/" + REST_API_KEY + "/files/";
    public static BackendlessUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        Backendless.setUrl(SERVER_URL);
        Backendless.initApp(getApplicationContext(), APPLICATION_ID, API_KEY);
    }
}
