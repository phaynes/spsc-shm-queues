# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

if("/Users/philiphaynes/devel/spscmqueue/cppbuild/Catch-1.2.1.tar.gz" STREQUAL "")
  message(FATAL_ERROR "LOCAL can't be empty")
endif()

if(NOT EXISTS "/Users/philiphaynes/devel/spscmqueue/cppbuild/Catch-1.2.1.tar.gz")
  message(FATAL_ERROR "File not found: /Users/philiphaynes/devel/spscmqueue/cppbuild/Catch-1.2.1.tar.gz")
endif()

if("MD5" STREQUAL "")
  message(WARNING "File will not be verified since no URL_HASH specified")
  return()
endif()

if("70ee4cc959f8ff74ae43259de31d6eea" STREQUAL "")
  message(FATAL_ERROR "EXPECT_VALUE can't be empty")
endif()

message(STATUS "verifying file...
     file='/Users/philiphaynes/devel/spscmqueue/cppbuild/Catch-1.2.1.tar.gz'")

file("MD5" "/Users/philiphaynes/devel/spscmqueue/cppbuild/Catch-1.2.1.tar.gz" actual_value)

if(NOT "${actual_value}" STREQUAL "70ee4cc959f8ff74ae43259de31d6eea")
  message(FATAL_ERROR "error: MD5 hash of
  /Users/philiphaynes/devel/spscmqueue/cppbuild/Catch-1.2.1.tar.gz
does not match expected value
  expected: '70ee4cc959f8ff74ae43259de31d6eea'
    actual: '${actual_value}'
")
endif()

message(STATUS "verifying file... done")
