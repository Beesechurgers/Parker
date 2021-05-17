/*
 * Copyright (c) 2021, Shashank Verma <shashank.verma2002@gmail.com>(shank03)
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

#define INVALID_STRING "#@$"

/*
 * Database
 *    |
 *    `- Users
 *    |   |
 *    |   `- USER_UID
 *    |   |      |
 *    |   |      `- NUMBER_PLATE: XYZ-XYZ-XYZ
 *    |   |      `- CAR_STATUS: `entered` / `exited`
 *    |   |      `- ENTERED_TIME: -1 / 82372323223
 *    |   |      `- EXITED_TIME: -1 / 29898749843
 *    |   |      `- LAST_LOCATION
 *    |   |             |
 *    |   |             `- LAT: 101.20020202
 *    |   |             `- LONG: 2.3844944949
 *    |   |      `- PAYMENT
 *    |   |            |
 *    |   |            `- PAYMENT_STATUS: Completed / Pending
 *    |   |            `- PAYMENT_AMOUNT: 0 / amount
 *    |   |
 *    |   |
 *    |   `- USER_UID
 *    |         |
 *    |         `- NUMBER_PLATE: ABC-ABC-ABC
 *    |         `- CAR_STATUS: `entered` / `exited`
 *    |         `- ENTERED_TIME: -1 / 2535322879
 *    |         `- EXITED_TIME: -1 / 6478690504
 *    |         `- LAST_LOCATION
 *    |                 |
 *    |                 `- LAT: 101.20020202
 *    |                 `- LONG: 2.3844944949
 *    |         `- PAYMENT
 *    |               |
 *    |               `- PAYMENT_STATUS: Completed / Pending
 *    |               `- PAYMENT_AMOUNT: 0 / amount
 *    |
 *    `- Active
 *    |    |
 *    |    `- USER UID
 *    |          `- SESSION: 3d76ee7b-6439-4232-abc2-184b67416c9e [SESSION UUID]
 *    |
 *    `- History
 *         |
 *         `- USER_UID
 *              |
 *              `- PAYMENT:
 *                      ...
 *                      ...
 *                      ...
 */

#define USERS "Users"
#define NUMBER_PLATE "number_plate"

#define CAR_STATUS "status"
#define ENTERED "entered"
#define EXITED "exited"

#define ENTERED_TIME "enter_stamp"
#define EXITED_TIME "exit_stamp"
#define INVALID_TIME -1L

#define PAYMENT "payment"
#define PAYMENT_STATUS "status"
#define PAYMENT_COMPLETED "completed"
#define PAYMENT_PENDING "pending"
#define PAYMENT_AMOUNT "amount"
#define PER_HOUR 50     // Rupees
#define PER_15_MIN 10   // Rupees

#define ACTIVE "Active"
#define SESSION "session_id"
#define INVALID_SESSION "__null__"
