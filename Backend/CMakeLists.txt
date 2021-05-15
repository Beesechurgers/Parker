#
# Copyright (c) 2021, Shashank Verma <shashank.verma2002@gmail.com>(shank03)
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#

cmake_minimum_required(VERSION 3.16)
project(Parker)

set(CMAKE_CXX_STANDARD 14)

find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})

add_subdirectory(firebase_cpp_sdk)
include_directories(firebase_cpp_sdk/include)

add_executable(Parker main.cpp database_constants.h log.hpp)
target_link_libraries(Parker ${OpenCV_LIBS} firebase_auth firebase_database firebase_app pthread)
