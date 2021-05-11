package com.beesechurgers.parker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.beesechurgers.parker.utils.Utils;

import org.jetbrains.annotations.NotNull;

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
            String email = Utils.validateEmail(emailInput), password = Utils.validatePassword(passwordInput);
            if (!email.equals(Utils.INVALID_STRING) && !password.equals(Utils.INVALID_STRING)) {
                Log.d(TAG, "Login: onClick: Continue Login");
            }
        });

        findViewById(R.id.sign_up_btn).setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }
}