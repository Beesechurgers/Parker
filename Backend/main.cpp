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

#include <firebase/app.h>
#include <firebase/auth.h>
#include <firebase/database.h>
#include <fstream>
#include <iostream>
#include <opencv2/opencv.hpp>
#include <future>
#include "database_constants.h"
#include "log.hpp"

struct CarUser {
    std::string carNumber = INVALID_STRING,
            user_uid = INVALID_STRING;
    long long enteredTime = 0LL;
};

firebase::auth::User *user = nullptr;
firebase::database::Database *pDatabaseInstance = nullptr;

std::vector<CarUser> numberPlates;
std::string API_KEY;
bool loginCompleted = false, qrScanned = true, errorTriggered = false;
Logger logger;

int getOption();

std::string getNumberPlate();

void cleanData();

void initApiKey();

std::string getSessionID();

void updateNumberList(const firebase::database::DataSnapshot &snapshot);

double getPayment(long long timeElapsed);

class UsersValueListener : public firebase::database::ValueListener {
public:
    ~UsersValueListener() override = default;

    void OnValueChanged(const firebase::database::DataSnapshot &snapshot) override {
        try {
            if (qrScanned) {
                std::async(std::launch::async, updateNumberList, snapshot).wait();
            }
        } catch (std::exception &e) {
            logger << "[UsersValueListener] Error: " << e.what() >> true;
            errorTriggered = true;
        }
    }

    void OnCancelled(const firebase::database::Error &error, const char *error_message) override {
        logger << "[UsersValueListener] DatabaseError: " << error_message >> true;
    }
};

class ActiveValueListener : public firebase::database::ValueListener {
public:
    ~ActiveValueListener() override = default;

    void OnValueChanged(const firebase::database::DataSnapshot &snapshot) override {
        try {
            if (!qrScanned) {
                std::async(std::launch::async, [&snapshot = snapshot]() {
                    std::vector<firebase::database::DataSnapshot> children = snapshot.children();
                    logger << "User scanned" >> false;
                    std::string currentSession = getSessionID(), user_uid;

                    auto it = std::find_if(children.begin(), children.end(),
                                           [&session = currentSession]
                                                   (const firebase::database::DataSnapshot &childSnap) -> bool {
                                               return childSnap.Child(SESSION).value().mutable_string() == session;
                                           });
                    if (it != children.end()) {
                        long long pos = it - children.begin();
                        firebase::database::DataSnapshot child = children[pos];
                        cv::destroyAllWindows();

                        user_uid = child.key_string();
                        logger << "User entered: " << user_uid >> false;
                        logger << "Session: " << child.Child(SESSION).value().mutable_string() >> false;

                        std::map<std::string, firebase::Variant> data;
                        data[CAR_STATUS] = ENTERED;
                        data[ENTERED_TIME] = std::time(nullptr);

                        pDatabaseInstance->GetReference(USERS).Child(user_uid).UpdateChildren(data);
                    }
                }).wait();
                qrScanned = true;
            }
        } catch (std::exception &e) {
            logger << "[ActiveValueListener] Error: " << e.what() >> true;
            errorTriggered = true;
        }
    }

    void OnCancelled(const firebase::database::Error &error, const char *error_message) override {
        logger << "[ActiveValueListener] DatabaseError: " << error_message >> true;
    }
};

int main(int argc, char *argv[]) {
    if (argc != 3) {
        std::cout << "Invalid arguments\n";
        return 0;
    }

    initApiKey();
    auto pFirebaseApp = std::unique_ptr<::firebase::App>(::firebase::App::Create());
    auto pFirebaseAuth = std::unique_ptr<::firebase::auth::Auth>(::firebase::auth::Auth::GetAuth(pFirebaseApp.get()));

    pDatabaseInstance = ::firebase::database::Database::GetInstance(pFirebaseApp.get());
    auto mRootRef = pDatabaseInstance->GetReference();
    mRootRef.Child(USERS).AddValueListener(new UsersValueListener());
    mRootRef.Child(ACTIVE).AddValueListener(new ActiveValueListener());

    try {
        // ----------------------------------------------------------------------------------------------
        //                                      ADMIN LOGIN
        // ----------------------------------------------------------------------------------------------
        if (!pFirebaseAuth->current_user()) {
            logger << "Logging in..." >> true;
            pFirebaseAuth->SignInWithEmailAndPassword(argv[1], argv[2])
                    .OnCompletion([](const firebase::Future<firebase::auth::User *> &res) {
                        user = *res.result();
                        logger << "Logged in: " << user->uid() >> true;

                        pDatabaseInstance->GetReference(USERS).GetValue()
                                .OnCompletion([](const firebase::Future<firebase::database::DataSnapshot> &snapshot) {
                                    if (snapshot.status() == firebase::kFutureStatusComplete &&
                                        snapshot.error() == firebase::database::kErrorNone) {

                                        if (!snapshot.result()->HasChild(user->uid())) {
                                            std::map<std::string, firebase::Variant> data, location, payment;
                                            data[NUMBER_PLATE] = "xyz-xyz-xyz";
                                            data[CAR_STATUS] = EXITED;
                                            data[ENTERED_TIME] = INVALID_TIME;
                                            data[EXITED_TIME] = INVALID_TIME;

                                            payment[PAYMENT_STATUS] = PAYMENT_COMPLETED;
                                            payment[PAYMENT_AMOUNT] = 0;
                                            data[PAYMENT] = payment;

                                            location[LAT] = INVALID_LOCATION;
                                            location[LONG] = INVALID_LOCATION;
                                            data[LAST_LOCATION] = location;

                                            pDatabaseInstance->GetReference(USERS).Child(user->uid())
                                                    .UpdateChildren(data).OnCompletion([](const auto &res) {
                                                        logger << "User created; Logged in: " << user->uid() >> true;
                                                        loginCompleted = true;
                                                    });
                                        } else {
                                            loginCompleted = true;
                                        }
                                    }
                                });

                        pDatabaseInstance->GetReference(ACTIVE).GetValue()
                                .OnCompletion([](const firebase::Future<firebase::database::DataSnapshot> &snapshot) {
                                    if (snapshot.status() == firebase::kFutureStatusComplete &&
                                        snapshot.error() == firebase::database::kErrorNone) {

                                        if (!snapshot.result()->HasChild(user->uid())) {
                                            std::map<std::string, firebase::Variant> data;
                                            data[SESSION] = INVALID_SESSION;
                                            pDatabaseInstance->GetReference(ACTIVE).Child(user->uid())
                                                    .UpdateChildren(data);
                                        }
                                    }
                                });
                    });
        } else {
            loginCompleted = true;
            errorTriggered = true;
            logger << "User was already present\nPlease re-run the program" >> true;
        }

        // ----------------------------------------------------------------------------------------------
        //                                   PROCESS / TASK (?)
        // ----------------------------------------------------------------------------------------------
        loop:
        if (!loginCompleted) goto loop;
        if (!qrScanned) goto loop;
        cleanData();

        int option;
        if (errorTriggered) {
            option = 'q';
        } else {
            option = getOption();
        }
//        system("clear");

        if (option == '1') {
            logger << "|Option: Enter car|" >> false;
            int completed = system(&("cd helpers && python3 detector.py " + API_KEY + " 1")[0]);
            if (completed == 0) {
                logger << "Task completed" >> true;
                std::string plate = getNumberPlate();
                if (plate == "None") {
                    logger << "Invalid License Number" >> true;
                    goto loop;
                }
                logger << "License Number: " << plate >> true;

                qrScanned = false;

                logger << "Showing QR Code" >> true;
                cv::Mat qr = cv::imread("helpers/qrcode.png");
                cv::namedWindow("QrCode");
                cv::moveWindow("QrCode", (1980 - 675) / 2, (1080 - 675) / 2);
                cv::imshow("QrCode", qr);

                cv::waitKey();
            }
            goto loop;

        } else if (option == '2') {
            logger << "|Option: Exit car|" >> false;
            int completed = system(&("cd helpers && python3 detector.py " + API_KEY + " 2")[0]);
            if (completed == 0) {
                logger << "Task completed" >> true;
                std::string plate = getNumberPlate();
                if (plate == "None") {
                    logger << "Invalid License Number: " << plate >> true;
                    goto loop;
                }
                logger << "License Number: " << plate >> true;

                auto it = std::find_if(numberPlates.begin(), numberPlates.end(),
                                       [&number = plate](const CarUser &c) -> bool {
                                           return c.carNumber == number;
                                       });
                if (it != numberPlates.end()) {
                    logger << "Valid car exited" >> true;
                    logger << "This car belongs to " << it->user_uid >> false;

                    std::map<std::string, firebase::Variant> data, payment;
                    data[CAR_STATUS] = EXITED;
                    data[ENTERED_TIME] = INVALID_TIME;
                    data[EXITED_TIME] = std::time(nullptr);

                    payment[PAYMENT_STATUS] = PAYMENT_PENDING;
                    payment[PAYMENT_AMOUNT] = getPayment(std::time(nullptr) - it->enteredTime);
                    data[PAYMENT] = payment;

                    pDatabaseInstance->GetReference(ACTIVE).Child(it->user_uid).RemoveValue();
                    pDatabaseInstance->GetReference(USERS).Child(it->user_uid).UpdateChildren(data);
                } else {
                    logger << "Invalid car trying to exit: " << plate >> true;
                }
            }
            qrScanned = true;
            goto loop;

        } else {
            cleanData();
        }
    } catch (std::exception &e) {
        logger << "FATAL: " << e.what() >> true;
    }

    // Logout and clear all instances
    delete pDatabaseInstance;
    mRootRef.RemoveAllValueListeners();
    if (pFirebaseAuth->current_user()) {
        if (user != nullptr) {
            logger << "Logging out: " << user->uid() >> true;
        }
        pFirebaseAuth->SignOut();
        if (!pFirebaseAuth->current_user()) {
            logger << "Logged out" >> true;
        }
    }
    return 0;
}

// ----------------------------------------------------------------------------------------------
//                                      UTILS
// ----------------------------------------------------------------------------------------------
int getOption() {
    std::cout << "Options:\n";
    std::cout << "[1] Car entering\n";
    std::cout << "[2] Car exiting\n";
    std::cout << "[q] Exit\n";
    std::cout << "Enter:";

    char option;
    std::cin >> option;
    return option;
}

std::string getNumberPlate() {
    std::fstream licenseFile("helpers/license_number.txt");
    std::string line;
    getline(licenseFile, line);
    licenseFile.close();
    return line;
}

void cleanData() {
    // Clear last detections
    std::ofstream cLicenseFile("helpers/license_number.txt");
    cLicenseFile << "None";
    cLicenseFile.close();

    std::ofstream cSessionFile("helpers/session.txt");
    cSessionFile << "None";
    cSessionFile.close();

    // Clear last generated QR Code
    remove("helpers/qrcode.png");
}

void initApiKey() {
    std::ifstream apiTxt("utils/api.txt");
    getline(apiTxt, API_KEY);
    apiTxt.close();
}

std::string getSessionID() {
    std::ifstream sessionFile("helpers/session.txt");
    std::string line;
    getline(sessionFile, line);
    sessionFile.close();
    return line;
}

void updateNumberList(const firebase::database::DataSnapshot &snapshot) {
    logger << "-------------- UPDATING CAR LIST --------------" >> false;
    numberPlates.clear();
    long long count = 0;
    for (const auto &child : snapshot.children()) {
        if (child.Child(CAR_STATUS).value().mutable_string() == ENTERED) {
            count++;
            CarUser car;
            car.carNumber = child.Child(NUMBER_PLATE).value().mutable_string();
            car.user_uid = child.key_string();
            car.enteredTime = (long long) child.Child(ENTERED_TIME).value().int64_value();

            logger << count << ": number = " << car.carNumber >> false;
            logger << "uid = " << car.user_uid >> false;
            logger << "enter time = " << car.enteredTime >> false;

            numberPlates.push_back(car);
            logger << "Pushed: " << car.carNumber >> false;
        }
    }
    logger << "-------------------------------------------" >> false;
}

double getPayment(long long timeElapsed) {
    double payment = 0.0;
    logger << "Payment: sec = " << timeElapsed >> false;
    timeElapsed = timeElapsed / 60;
    logger << "Payment: min = " << timeElapsed >> false;
    if (timeElapsed >= 60) {
        long long multiple = timeElapsed / 60;
        payment += PER_HOUR * multiple;
        timeElapsed -= 60 * multiple;
    }
    payment += PER_15_MIN * (double) ((long double) timeElapsed / 15.0);
    return payment;
}
