cmake_minimum_required(VERSION 2.6)
 
find_program( GCOV_PATH gcov )
find_program( LCOV_PATH  NAMES lcov lcov.bat lcov.exe lcov.perl)

# Locate GTest
find_package(GTest REQUIRED)
include_directories(${GTEST_INCLUDE_DIRS})
 
#include( cmake/CodeCoverage.cmake )
SET(CMAKE_CXX_FLAGS "-g -O0 --coverage -fprofile-arcs -ftest-coverage")
SET(CMAKE_C_FLAGS "-g -O0 --coverage -fprofile-arcs -ftest-coverage")

# Link runTests with what we want to test and the GTest and pthread library
add_executable(runTests ESTest.cpp)
target_link_libraries(runTests ${GTEST_LIBRARIES} pthread)


