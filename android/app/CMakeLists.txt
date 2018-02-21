cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(../../sr-lib sr-lib/word)
include_directories(../../sr-lib/word/inc)

add_library(hmm SHARED src/main/cpp/hmm.cpp)

target_link_libraries(hmm word)