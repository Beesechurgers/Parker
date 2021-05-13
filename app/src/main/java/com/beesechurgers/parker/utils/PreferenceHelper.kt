package com.beesechurgers.parker.utils

import android.content.Context
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences

const val PKG_PREF = "_parker"

object PrefKeys {
    // String key
    const val CAR_NUMBER = "car_number"

    const val USER_PHOTO = "photo_url"
}

fun Context.putString(key: String, value: String) =
    getPrefHandler(this).edit().putString(key, value).apply()

fun Context.getString(key: String) =
    getPrefHandler(this).getString(key, Utils.INVALID_STRING) ?: Utils.INVALID_STRING

fun getPrefHandler(context: Context): Preferences = BinaryPreferencesBuilder(context.applicationContext)
    .name(PKG_PREF).build()