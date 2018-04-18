cmake_minimum_required(VERSION 3.6)

project(Speechy)

add_subdirectory(../../sr-lib sr-lib/word)
include_directories(../../sr-lib/word/inc)

add_library(Speechy SHARED src/main/cpp/Speechy.cpp)
target_link_libraries(Speechy word)
