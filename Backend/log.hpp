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

#pragma once

class Logger {
public:

    Logger() {
        try {
            std::ofstream truncate("log.txt", std::ios::trunc);
            auto time = std::time(nullptr);
            truncate << "Session started " << std::ctime(&time) << "\n";
            truncate.close();
        } catch (std::exception &e) {
        }
    }

    Logger &operator<<(const std::string &in) {
        stream << in;
        return *this;
    }

    Logger &operator<<(const char *in) {
        stream << in;
        return *this;
    }

    Logger &operator<<(long long in) {
        stream << in;
        return *this;
    }

    void operator>>(bool print) {
        write(print);
    }

private:
    std::stringstream stream;

    void write(bool print) {
        std::ofstream logFile("log.txt", std::ios::app);

        auto milli = std::time(nullptr);
        char *time = std::ctime(&milli);
        time[strcspn(time, "\n")] = '\0';

        if (logFile.is_open()) logFile << "[" << time << "] " << stream.str() << "\n";
        logFile.close();

        if (print) std::cout << stream.str() << "\n";
        stream.str(std::string());
        stream.clear();
    }
};
