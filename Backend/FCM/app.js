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

const uuid = require('uuid');
const fs = require('fs');
const admin = require('firebase-admin');

const serviceAccount = require("./parker-649a6-firebase-adminsdk-3kh7r-9f0ce7ce45.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://parker-649a6-default-rtdb.asia-southeast1.firebasedatabase.app"
});

// Args length should be 6:
// 1. node
// 2. app.js
// 3. user_uid
// 4. time_elapsed
// 5. amount
// 6. paymentRequired
if (process.argv.length !== 6) {

    // Another way to saying that we need uuid
    // Refer sessions value to detect that we don't generate any used UUID
    admin.database().ref("Session_Used").get().then(snapshot => {
        let generatedUUID = uuid.v4();
        if (snapshot.hasChildren()) {
            while (snapshot.hasChild(generatedUUID)) {   // Loop until generated UUID is not used
                generatedUUID = uuid.v4();
            }
        }
        // Write that to file
        fs.writeFile('helpers/session.txt', generatedUUID, function (err) {
            console.log("File error:", err);
            process.exit(0);
        });
    });
    return;
}

const uid = process.argv[2];
// Take "Tokens" table reference
admin.database().ref("Tokens").child(uid).get().then(snapshot => {

    // Get token from user uid
    const userToken = snapshot.val();
    if (userToken === null) {
        console.log("User token was null");
        process.exit(1);
    }

    // Send notification
    admin.messaging().send({
        data: {
            to: uid,
            min: process.argv[3],
            amount: process.argv[4],
            paymentRequired: process.argv[5]
        },
        token: userToken
    }).then(_ => {
        console.log("Sent Notification");
        process.exit(0);
    }).catch(error => {
        console.log("Error: ", error);
        process.exit(1);
    });
});
