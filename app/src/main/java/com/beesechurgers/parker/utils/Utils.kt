package com.beesechurgers.parker.utils

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.regex.Pattern

object Utils {

    const val INVALID_STRING = "#@$"

    @JvmStatic
    fun clearUserData(context: Context) {
        with(INVALID_STRING) {
            context.putString(PrefKeys.CAR_NUMBER, this)
            context.putString(PrefKeys.USER_PHOTO, this)
        }
    }

    @JvmStatic
    fun validateCarNumber(carNumberInput: AppCompatEditText): String {
        val number = carNumberInput.text?.toString()?.trim()?.replace(" ", "")
        number ?: return INVALID_STRING
        return when {
            number.isEmpty() -> {
                carNumberInput.error = "Empty"
                INVALID_STRING
            }
            number.isValidCarNumber() -> number
            else -> {
                carNumberInput.error = "Invalid Number"
                INVALID_STRING
            }
        }
    }

    fun String.isValidCarNumber() = Pattern.matches("[A-Z]{2}[0-9]{1,2}(?:[A-Z])?(?:[A-Z]*)?[0-9]{4}", this)

    fun DatabaseReference.valueEvenListener(onDataChange: (snapshot: DataSnapshot) -> Unit, onCancelled: (error: DatabaseError) -> Unit = {}) {
        this.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                this@valueEvenListener.removeEventListener(this)
                onDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) = onCancelled(error)
        })
    }
}