package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.material.appbar.MaterialToolbar;

public class EditProfile extends AppCompatActivity {

    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private MaterialToolbar appTopBar;
    private EditText editNameET;
    private Button updateBTN, cancelBTN;
    private String currentName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //appTopBar
        appTopBar = findViewById(R.id.appTopBar);
        appTopBar.setTitle("Edit Profile");
        //progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressLayout);
        progressBarLabel = findViewById(R.id.progressLabel);
        fromLayout = findViewById(R.id.editProfileLayout);

        editNameET = findViewById(R.id.editNameET);
        updateBTN = findViewById(R.id.updateBTN);
        cancelBTN = findViewById(R.id.cancelBTN);


        try {
            currentName = Server.currentUser.getProperty("name").toString();
        }
        catch (Exception e){
            currentName = "";
            e.printStackTrace();
        }

        editNameET.setText(currentName);
        updateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editNameET.getText().toString();
                if(newName.isEmpty()){
                    Toast.makeText(EditProfile.this, "Name Cannot Be Empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(!newName.equals(currentName)){
                        showProgress(true);
                        Server.currentUser.setProperty("name", newName);
                        Backendless.UserService.update(Server.currentUser, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                Toast.makeText(EditProfile.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                                EditProfile.this.finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(EditProfile.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                                showProgress(false);
                            }
                        });
                    }
                    else{
                        Toast.makeText(EditProfile.this, "Please Enter Different Name", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfile.this.finish();
            }
        });

    }

    //    progressbar
    public void showProgress(boolean show) {
        fromLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setVisibility(show ? View.VISIBLE : View.GONE);

    }
}