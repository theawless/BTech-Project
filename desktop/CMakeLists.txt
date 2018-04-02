cmake_minimum_required(VERSION 3.6)

project(Speechy)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})
add_subdirectory(../sr-lib sr-lib/word)
include_directories(../sr-lib/word/inc)

add_library(Speechy SHARED src/main/cpp/Speechy.cpp src/main/cpp/Speechy.h)
target_link_libraries(Speechy word)
