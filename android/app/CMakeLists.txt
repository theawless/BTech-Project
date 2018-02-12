cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(../../sr-lib sr-lib/sr-lib)
include_directories(../../sr-lib/sr-lib/inc)

add_library(direct SHARED src/main/cpp/direct.cpp)
add_library(hmm SHARED src/main/cpp/hmm.cpp)

target_link_libraries(direct sr-lib)
target_link_libraries(hmm sr-lib)