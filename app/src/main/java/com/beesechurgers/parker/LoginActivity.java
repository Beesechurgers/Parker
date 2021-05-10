package com.beesechurgers.parker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button buttonOne = findViewById(R.id.sign_up);
        buttonOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("LoginActivity", "button was clicked");
                Intent activity2Intent = new Intent(getApplicationContext(), SignUp.class);
                startActivity(activity2Intent);
            }
        });
    }
}