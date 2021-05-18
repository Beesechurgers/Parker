/*
 * Copyright (c) 2021, Beesechurgers <https://github.com/Beesechurgers>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.beesechurgers.parker.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    fun String.formatAmount(): String {
        return if (this.contains('.')) {
            if (this.split('.')[1].length > 2) {
                this.substring(IntRange(0, this.indexOf('.') + 2))
            } else this
        } else "$this.00"
    }

    fun DatabaseReference.valueEvenListener(onDataChange: (snapshot: DataSnapshot) -> Unit, onCancelled: (error: DatabaseError) -> Unit = {}) {
        this.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                this@valueEvenListener.removeEventListener(this)
                onDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) = onCancelled(error)
        })
    }

    fun Context.isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        activeNetwork ?: return false

        val capabilities = cm.getNetworkCapabilities(activeNetwork)
        capabilities ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    inline fun <T : Any> checkNull(vararg elements: T?, then: (List<T>) -> Unit) {
        if (elements.all { it != null && it != "null" }) then(elements.filterNotNull())
    }
}