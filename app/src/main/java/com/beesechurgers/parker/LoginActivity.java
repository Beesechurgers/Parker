package com.beesechurgers.parker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    AppCompatEditText emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.login_email);
        passwordInput = findViewById(R.id.login_password);

        findViewById(R.id.login_btn).setOnClickListener(v -> {
            String email = validateEmail(), password = validatePassword();
            if (!email.equals("None") && !password.equals("None")) {
                Log.d(TAG, "Login: onClick: Continue Login");
            }
        });

        findViewById(R.id.sign_up_btn).setOnClickListener(v -> startActivity(new Intent(this, SignUp.class)));
    }

    private String validateEmail() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            emailInput.setError("Empty Email");
            return "None";
        } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return email;
        } else {
            emailInput.setError("Invalid Email");
            return "None";
        }
    }

    private String validatePassword() {
        String password = passwordInput.getText().toString().trim();
        if (password.isEmpty()) {
            emailInput.setError("Empty Password");
            return "None";
        } else if (password.length() < 6) {
            emailInput.setError("Password length < 6");
            return "None";
        } else {
            return password;
        }
    }
}