cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(src/main/cpp/sr-lib)
INCLUDE_DIRECTORIES(src/main/cpp/sr-lib/sr-lib/inc)
link_directories(src/main/cpp/sr-lib)

add_library(direct SHARED src/main/cpp/direct.cpp)
add_library(hmm SHARED src/main/cpp/hmm.cpp)

target_link_libraries(direct sr-lib)
target_link_libraries(hmm sr-lib)