cmake_minimum_required(VERSION 3.6)

project(Speechy)
add_library(Speechy SHARED src/main/cpp/Speechy.cpp src/main/cpp/Speechy.h)

find_package(JNI REQUIRED)
target_include_directories(Speechy PRIVATE ${JNI_INCLUDE_DIRS})
add_subdirectory(../sr-lib sr-lib)
target_link_libraries(Speechy word)
target_include_directories(Speechy PRIVATE ../sr-lib/word/inc)
