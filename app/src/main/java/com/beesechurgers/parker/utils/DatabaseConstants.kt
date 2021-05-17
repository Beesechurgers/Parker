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

object DatabaseConstants {

    const val USERS = "Users"
    const val NUMBER_PLATE = "number_plate"

    const val CAR_STATUS = "status"
    const val ENTERED = "entered"
    const val EXITED = "exited"

    const val ENTERED_TIME = "enter_stamp"
    const val EXITED_TIME = "exit_stamp"
    const val INVALID_TIME = -1L

    const val PAYMENT = "payment"
    const val PAYMENT_STATUS = "status"
    const val PAYMENT_COMPLETED = "completed"
    const val PAYMENT_PENDING = "pending"
    const val PAYMENT_AMOUNT = "amount"

    const val LAST_LOCATION = "last_location"
    const val LAT = "latitude"
    const val LONG = "longitude"
    const val INVALID_LOCATION = 0.0

    const val ACTIVE = "Active"
    const val SESSION = "session_id"
    const val INVALID_SESSION = "__null__"

    const val HISTORY = "History"
}