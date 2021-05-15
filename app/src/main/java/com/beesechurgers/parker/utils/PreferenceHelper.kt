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