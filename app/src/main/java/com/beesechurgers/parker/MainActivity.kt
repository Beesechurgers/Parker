package com.beesechurgers.parker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beesechurgers.parker.utils.PrefKeys
import com.beesechurgers.parker.utils.Utils
import com.beesechurgers.parker.utils.putString
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            putString(PrefKeys.CAR_NUMBER, Utils.INVALID_STRING)
            startActivity(Intent(this, SplashActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}