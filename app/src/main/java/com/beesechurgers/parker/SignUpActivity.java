package com.beesechurgers.parker;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.beesechurgers.parker.utils.Utils;

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
            String number = Utils.validateCarNumber(carNumberInput),
                email = Utils.validateEmail(emailInput), password = Utils.validatePassword(passwordInput);
            if (!email.equals(Utils.INVALID_STRING) && !password.equals(Utils.INVALID_STRING) && !number.equals(Utils.INVALID_STRING)) {
                Log.d(TAG, "SignUp: onClick: Continue Sign up");
            }
        });
    }
}