package com.beesechurgers.parker;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    AppCompatEditText carNumberInput, emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        carNumberInput = findViewById(R.id.sign_up_number_plate);
        emailInput = findViewById(R.id.sign_up_email);
        passwordInput = findViewById(R.id.sign_up_password);

        findViewById(R.id.sign_up_final).setOnClickListener(v -> {
            String number = validateCarNumber(), email = validateEmail(), password = validatePassword();
            if (!email.equals("None") && !password.equals("None") && !number.equals("None")) {
                Log.d(TAG, "SignUp: onClick: Continue Sign up");
            }
        });
    }

    @NotNull
    private String validateCarNumber() {
        String number = carNumberInput.getText().toString().trim();
        if (number.isEmpty()) {
            carNumberInput.setError("Empty");
            return "None";
        } else if (Pattern.matches("[A-Z]{2}[0-9]{1,2}(?:[A-Z])?(?:[A-Z]*)?[0-9]{4}", number)) {
            return number;
        } else {
            carNumberInput.setError("Invalid Number");
            return "None";
        }
    }

    @NotNull
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

    @NotNull
    private String validatePassword() {
        String password = passwordInput.getText().toString().trim();
        if (password.isEmpty()) {
            passwordInput.setError("Empty Password");
            return "None";
        } else if (password.length() < 6) {
            passwordInput.setError("Password length < 6");
            return "None";
        } else {
            return password;
        }
    }
}