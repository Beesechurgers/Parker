const admin = require('firebase-admin');

var serviceAccount = require("./parker-649a6-firebase-adminsdk-3kh7r-9f0ce7ce45.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://parker-649a6-default-rtdb.asia-southeast1.firebasedatabase.app"
});

if (process.argv.length != 5) {
    console.log("Invalid args");
    process.exit(1);
}

admin.messaging().send({
    data: {
        to: process.argv[2],
        min: process.argv[3],
        amount: process.argv[4]
    },
    topic: "cheeseCpp"
}).then(response => {
    console.log("Sent: ", response);
    process.exit(0);
}).catch(error => {
    console.log("Error: ", error);
    process.exit(1);
});
