package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class Settings extends AppCompatActivity implements View.OnClickListener {

    private  LinearLayout version, logout, changePassword, feedback, contactUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        version = findViewById(R.id.version);
        logout = findViewById(R.id.logout);
        changePassword = findViewById(R.id.changePassword);
        feedback = findViewById(R.id.feedback);
        contactUs = findViewById(R.id.contactUs);

        logout.setOnClickListener(this);
        version.setOnClickListener(this);
        feedback.setOnClickListener(this);
        changePassword.setOnClickListener(this);
        contactUs.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.version:
                startActivity(new Intent(Settings.this, Version.class));
                break;

            case R.id.logout:
                logout();
                break;

            case R.id.changePassword:
                startActivity(new Intent(Settings.this, ChangePassword.class));
                break;
            case R.id.feedback:
                startActivity(new Intent(Settings.this, Feedback.class));
                break;
            case R.id.contactUs:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/pratikap41/Post/blob/master/README.md"));
                startActivity(intent);
                break;


        }
    }

    private void logout(){
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Toast.makeText(Settings.this, "Logged Out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(Settings.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Settings.this.finish();
        Intent intent = new Intent(Settings.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }
}

