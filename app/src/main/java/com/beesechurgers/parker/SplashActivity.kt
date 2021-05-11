package com.beesechurgers.parker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beesechurgers.parker.utils.PrefKeys
import com.beesechurgers.parker.utils.Utils.isValidCarNumber
import com.beesechurgers.parker.utils.getString
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val user = FirebaseAuth.getInstance().currentUser
        startActivity(Intent(this, if (user == null) LoginActivity::class.java else {
            if (this.getString(PrefKeys.CAR_NUMBER).isValidCarNumber()) {
                MainActivity::class.java
            } else CarNumberActivity::class.java
        }).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}