package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class Registration extends AppCompatActivity {
    private View progressBar, progressBarLayout, progressBarLabel, fromLayout;
    private EditText nameET, emailET, passwordET, confirmPasswordET;
    private Button registerBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        progressBar = findViewById(R.id.progressBar);
        progressBarLayout = findViewById(R.id.progressLayout);
        progressBarLabel = findViewById(R.id.progressLabel);
        fromLayout = findViewById(R.id.registrationForm);
        nameET = findViewById(R.id.rg_nameEditText);
        emailET = findViewById(R.id.rg_emailEditText);
        passwordET = findViewById(R.id.rg_passwordEditText);
        confirmPasswordET = findViewById(R.id.rg_repeatPasswordEditText);
        registerBTN = findViewById(R.id.rg_registrationButton);

        registerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                String name = nameET.getText().toString().trim(),
                        email = emailET.getText().toString().trim(),
                        password = passwordET.getText().toString(),
                        confirmPassword = confirmPasswordET.getText().toString();
                BackendlessUser user = new BackendlessUser();
                user.setEmail(email);
                user.setPassword(password);
                user.setProperty("name", name);
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(Registration.this, "Fill-Up All Entries Properly ", Toast.LENGTH_SHORT).show();
                    showProgress(false);
                } else {
                    if (password.equals(confirmPassword)) {

                        Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                startActivity(new Intent(Registration.this, Login.class));
                                Toast.makeText(Registration.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                Registration.this.finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                showProgress(false);
                                Toast.makeText(Registration.this, fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        showProgress(false);
                        Toast.makeText(Registration.this, "The Confirm Password Doesn't Match", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

    }

    public void showProgress(boolean show) {
        fromLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        progressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setVisibility(show ? View.VISIBLE : View.GONE);

    }
}