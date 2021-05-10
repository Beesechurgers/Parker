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
#include "database_constants.h"

enum ProgramStatus {
    ENTER_QR_SCAN, EXIT_QR_SCAN, IDLE
};

ProgramStatus sProgramStatus = ProgramStatus::IDLE;

firebase::auth::User *user = nullptr;
firebase::database::Database *pDatabaseInstance = nullptr;

std::vector<std::string> numberPlates;
bool loginCompleted = false;

int getOption();

std::string getNumberPlate();

void cleanData();

class UsersValueListener : public firebase::database::ValueListener {
public:
    ~UsersValueListener() override = default;

    void OnValueChanged(const firebase::database::DataSnapshot &snapshot) override {
        if (!loginCompleted) return;
        if (sProgramStatus == ProgramStatus::ENTER_QR_SCAN) {
            sProgramStatus = ProgramStatus::IDLE;
            return;
        }
        if (sProgramStatus == ProgramStatus::EXIT_QR_SCAN) {
            sProgramStatus = ProgramStatus::IDLE;
            return;
        }
        if (sProgramStatus == ProgramStatus::IDLE) {
            numberPlates.clear();
            for (const auto &child : snapshot.children()) {
                if (child.Child(CAR_STATUS).value().mutable_string() == ENTERED) {
                    numberPlates.push_back(child.Child(NUMBER_PLATE).value().mutable_string());
                }
            }
        }
    }

    void OnCancelled(const firebase::database::Error &error, const char *error_message) override {

    }
};

int main() {
    auto pFirebaseApp = std::unique_ptr<::firebase::App>(::firebase::App::Create());
    auto pFirebaseAuth = std::unique_ptr<::firebase::auth::Auth>(::firebase::auth::Auth::GetAuth(pFirebaseApp.get()));

    pDatabaseInstance = ::firebase::database::Database::GetInstance(pFirebaseApp.get());
    auto mUserRef = pDatabaseInstance->GetReference(USERS);
    mUserRef.AddValueListener(new UsersValueListener());

    try {
        // ----------------------------------------------------------------------------------------------
        //                                      ADMIN LOGIN
        // ----------------------------------------------------------------------------------------------
        if (!pFirebaseAuth->current_user()) {
            std::cout << "Logging in...\n";
            pFirebaseAuth->SignInWithEmailAndPassword("admin@beesechurgers.com", "cheese")
                    .OnCompletion([](const firebase::Future<firebase::auth::User *> &res) {
                        user = *res.result();

                        pDatabaseInstance->GetReference(USERS).GetValue()
                                .OnCompletion([](const firebase::Future<firebase::database::DataSnapshot> &snapshot) {
                                    if (snapshot.status() == firebase::kFutureStatusComplete &&
                                        snapshot.error() == firebase::database::kErrorNone) {

                                        if (!snapshot.result()->HasChild(user->uid())) {
                                            std::map<std::string, firebase::Variant> data, location;
                                            data[NUMBER_PLATE] = "xyz-xyz-xyz";
                                            data[CAR_STATUS] = EXITED;
                                            data[ENTERED_TIME] = INVALID_TIME;
                                            data[EXITED_TIME] = INVALID_TIME;
                                            data[PAYMENT] = PAYMENT_COMPLETED;

                                            location[LAT] = INVALID_LOCATION;
                                            location[LONG] = INVALID_LOCATION;
                                            data[LAST_LOCATION] = location;

                                            pDatabaseInstance->GetReference(USERS).Child(user->uid())
                                                    .UpdateChildren(data).OnCompletion([](const auto &res) {
                                                        std::cout << "User created; Logged in: " << user->uid() << "\n";
                                                        loginCompleted = true;
                                                    });
                                        } else {
                                            std::cout << "User exists; Logged in: " << user->uid() << "\n";
                                            loginCompleted = true;
                                        }
                                    }
                                });
                    });
        }

        // ----------------------------------------------------------------------------------------------
        //                                   PROCESS / TASK (?)
        // ----------------------------------------------------------------------------------------------
        loop:
        cleanData();
        if (!loginCompleted) goto loop;

        int option = getOption();
//        system("clear");

        if (option == '1') {
            std::cout << "Detecting\n";
            int completed = system("cd helpers && python3 detector.py 1");
            if (completed == 0) {
                std::cout << "Task completed\n";
                std::string plate = getNumberPlate();
                if (plate == "None") {
                    std::cout << "Invalid License Number: " << plate << "\n";
                    goto loop;
                }
                std::cout << "License Number: " << plate << "\n";


                auto it = find(numberPlates.begin(), numberPlates.end(), plate);
                if (it == numberPlates.end()) {
                    numberPlates.push_back(plate);

                    // Update car status
                    // std::map<std::string, firebase::Variant> data;
                    // data[plate] = "Entered";
                    // pDatabaseInstance->GetReference().Child("number_plates").UpdateChildren(data);

                    std::cout << "Car entered\n";
                }

                std::cout << "\nShowing QR Code\n";
                cv::imshow("QrCode", cv::imread("helpers/qrcode.jpg", cv::IMREAD_GRAYSCALE));
                cv::waitKey();
                goto loop;
            }

        } else if (option == '2') {
            std::cout << "Detecting\n";
            int completed = system("cd helpers && python3 detector.py 2");
            if (completed == 0) {
                std::cout << "Task completed\n";
                std::string plate = getNumberPlate();
                if (plate == "None") {
                    std::cout << "Invalid License Number: " << plate << "\n";
                }
                std::cout << "License Number: " << plate << "\n";

                auto it = find(numberPlates.begin(), numberPlates.end(), plate);
                if (it != numberPlates.end()) {
                    std::cout << "Car was parked\n";
                    numberPlates.erase(remove(numberPlates.begin(), numberPlates.end(), plate), numberPlates.end());

                    // Update car status
                    // std::map<std::string, firebase::Variant> data;
                    // data[plate] = "Exited";
                    // pDatabaseInstance->GetReference().Child("number_plates").UpdateChildren(data);

                    std::cout << "Car exited\n\n";
                }
            }

        } else if (option == 'q') {
            cleanData();
        }
    } catch (std::exception &e) {
        std::cout << "FATAL: " << e.what() << "\n";
    }

    // Logout and clear all instances
    delete pDatabaseInstance;
    mUserRef.RemoveAllValueListeners();
    if (pFirebaseAuth->current_user()) {
        if (user != nullptr) {
            std::cout << "Logging out: " << user->uid() << "\n";
        }
        pFirebaseAuth->SignOut();
        if (!pFirebaseAuth->current_user()) {
            std::cout << "Logged out\n";
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
    return line;
}

void cleanData() {
    // Clear last detections
    std::ofstream cLicenseFile("helpers/license_number.txt");
    cLicenseFile << "None";
    cLicenseFile.close();

    // Clear last generated QR Code
    remove("helpers/qrcode.jpg");
}
