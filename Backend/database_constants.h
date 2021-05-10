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

/*
 * Database
 *    |
 *    `- Users
 *        |
 *        `- 28373-dijifdiufvh8732-dfoijdf09u
 *        |      |
 *        |      `- NUMBER_PLATE: XYZ-XYZ-XYZ
 *        |      `- CAR_STATUS: `entered` / `exited`
 *        |      `- ENTERED_TIME: -1 / 82372323223
 *        |      `- EXITED_TIME: -1 / 29898749843
 *        |      `- LAST_LOCATION
 *        |             |
 *        |             `- LAT: 101.20020202
 *        |             `- LONG: 2.3844944949
 *        |
 *        |
 *        `- ujf74-dj37r0fk-keiby3img-kwmcg6u
 *              |
 *              `- NUMBER_PLATE: ABC-ABC-ABC
 *              `- CAR_STATUS: `entered` / `exited`
 *              `- ENTERED_TIME: -1 / 2535322879
 *              `- EXITED_TIME: -1 / 6478690504
 *               `- LAST_LOCATION
 *                      |
 *                      `- LAT: 101.20020202
 *                      `- LONG: 2.3844944949
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
#define PAYMENT_COMPLETED "completed"
#define PAYMENT_PENDING "pending"

#define LAST_LOCATION "last_location"
#define LAT "latitude"
#define LONG "longitude"
#define INVALID_LOCATION 0.0
