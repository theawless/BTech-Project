cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(src/main/cpp/sr-lib)
INCLUDE_DIRECTORIES(src/main/cpp/sr-lib/sr-lib/inc)
link_directories(src/main/cpp/sr-lib)

add_library(native-lib SHARED src/main/cpp/native-lib.cpp)
find_library(log-lib log)

target_link_libraries(native-lib ${log-lib})
target_link_libraries(native-lib sr-lib)