package com.beesechurgers.parker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent activity2Intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(activity2Intent);
    }
}