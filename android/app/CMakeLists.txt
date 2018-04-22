cmake_minimum_required(VERSION 3.6)

project(Speechy)
add_library(Speechy SHARED src/main/cpp/Speechy.cpp)

add_subdirectory(../../sr-lib sr-lib/word)
target_link_libraries(Speechy word)
target_include_directories(Speechy PRIVATE ../../sr-lib/word/inc)
