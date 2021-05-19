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

// Code to send notification to specific user

const admin = require('firebase-admin');

const serviceAccount = require("./parker-649a6-firebase-adminsdk-3kh7r-9f0ce7ce45.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://parker-649a6-default-rtdb.asia-southeast1.firebasedatabase.app"
});

if (process.argv.length !== 6) {
    console.log("Invalid args");
    process.exit(1);
}

admin.messaging().send({
    data: {
        to: process.argv[2],
        min: process.argv[3],
        amount: process.argv[4],
        paymentRequired: process.argv[5]
    },
    topic: "cheeseCpp"
}).then(_ => {
    console.log("Sent Notification");
    process.exit(0);
}).catch(error => {
    console.log("Error: ", error);
    process.exit(1);
});
