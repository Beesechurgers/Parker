package com.beesechurgers.parker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beesechurgers.parker.utils.PrefKeys
import com.beesechurgers.parker.utils.Utils
import com.beesechurgers.parker.utils.putString
import kotlinx.android.synthetic.main.activity_car_number.*

class CarNumberActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_number)

        reg_btn.setOnClickListener {
            with(Utils.validateCarNumber(reg_car_number)) {
                if (this != Utils.INVALID_STRING) {
                    this@CarNumberActivity.putString(PrefKeys.CAR_NUMBER, this)
                    startActivity(Intent(this@CarNumberActivity, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
    }
}