package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class ChangePassword extends AppCompatActivity {

    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;//progress bar
    private EditText currentPasswordET, newPasswordET, confirmNewPasswordET;
    private TextView warningTV;
    private Button changePasswordBTN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //progress bar
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressLayout);
        progressBarLabel = findViewById(R.id.progressLabel);
        fromLayout = findViewById(R.id.changePasswordLayout);

        currentPasswordET = findViewById(R.id.curretPasswordET);
        newPasswordET = findViewById(R.id.newPasswordET);
        confirmNewPasswordET = findViewById(R.id.confirmNewPasswordET);
        warningTV = findViewById(R.id.warningTV);
        changePasswordBTN = findViewById(R.id.changePasswordBTN);

        warningTV.setText("");
        ColorStateList colour = newPasswordET.getTextColors();

        changePasswordBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warningTV.setText("");
                currentPasswordET.setTextColor(colour);
                newPasswordET.setTextColor(colour);
                confirmNewPasswordET.setTextColor(colour);
                showProgress(true);
                changePassword();
            }
        });

    }

    private void changePassword(){
        String currentPassword = currentPasswordET.getText().toString().trim();
        String newPassword = newPasswordET.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordET.getText().toString().trim();

// check for empty edit texts
        if(currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()){
            warningTV.setText("Fill-UP All Required Fields");
            showProgress(false);
        }
        else{

//            check for new password and confirm new password
            if(newPassword.equals(confirmNewPassword)){

//                current user login to confirm current password
                Backendless.UserService.login(Server.currentUser.getEmail(), currentPassword, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        Server.currentUser.setPassword(newPassword);
//                        updating password into database
                        Backendless.UserService.update(Server.currentUser, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                Toast.makeText(ChangePassword.this, "Password Changed Succesfully", Toast.LENGTH_SHORT).show();
                                ChangePassword.this.finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                warningTV.setText(fault.getMessage());
                                showProgress(false);
                            }
                        });
                    }
                    @Override
                    public void handleFault(BackendlessFault fault) {
                        warningTV.setText(fault.getMessage());
                        currentPasswordET.setTextColor(getResources().getColor(R.color.red));
                        showProgress(false);
                    }
                });
            }
            else{
                warningTV.setText("Passwords Not Matching");
                newPasswordET.setTextColor(getResources().getColor(R.color.red));
                confirmNewPasswordET.setTextColor(getResources().getColor(R.color.red));
                showProgress(false);
            }
        }
    }


//    progressbar
    public void showProgress(boolean show) {
        fromLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setVisibility(show ? View.VISIBLE : View.GONE);

    }
}

