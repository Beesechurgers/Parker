package com.beesechurgers.parker.utils

import android.util.Patterns
import androidx.appcompat.widget.AppCompatEditText
import java.util.regex.Pattern

object Utils {

    const val INVALID_STRING = "#@$"

    @JvmStatic
    fun validateEmail(emailInputField: AppCompatEditText): String {
        val email = emailInputField.text?.toString()?.trim()
        email ?: return INVALID_STRING
        return when {
            email.isEmpty() -> {
                emailInputField.error = "Empty Email"
                INVALID_STRING
            }
            Patterns.EMAIL_ADDRESS.matcher(email).matches() -> email
            else -> {
                emailInputField.error = "Invalid Email"
                INVALID_STRING
            }
        }
    }

    @JvmStatic
    fun validatePassword(passwordInputField: AppCompatEditText): String {
        val password = passwordInputField.text?.toString()?.trim()
        password ?: return INVALID_STRING
        return when {
            password.isEmpty() -> {
                passwordInputField.error = "Empty Email"
                INVALID_STRING
            }
            password.length < 6 -> {
                passwordInputField.error = "Password length < 6"
                INVALID_STRING
            }
            else -> password
        }
    }

    @JvmStatic
    fun validateCarNumber(carNumberInput: AppCompatEditText): String {
        val number = carNumberInput.text?.toString()?.trim()
        number ?: return INVALID_STRING
        return when {
            number.isEmpty() -> {
                carNumberInput.error = "Empty"
                INVALID_STRING
            }
            Pattern.matches("[A-Z]{2}[0-9]{1,2}(?:[A-Z])?(?:[A-Z]*)?[0-9]{4}", number) -> number
            else -> {
                carNumberInput.error = "Invalid Number"
                INVALID_STRING
            }
        }
    }
}