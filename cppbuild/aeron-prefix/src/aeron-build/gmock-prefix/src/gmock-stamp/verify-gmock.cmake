# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

if("/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/gmock-1.7.0.zip" STREQUAL "")
  message(FATAL_ERROR "LOCAL can't be empty")
endif()

if(NOT EXISTS "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/gmock-1.7.0.zip")
  message(FATAL_ERROR "File not found: /Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/gmock-1.7.0.zip")
endif()

if("MD5" STREQUAL "")
  message(WARNING "File will not be verified since no URL_HASH specified")
  return()
endif()

if("073b984d8798ea1594f5e44d85b20d66" STREQUAL "")
  message(FATAL_ERROR "EXPECT_VALUE can't be empty")
endif()

message(STATUS "verifying file...
     file='/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/gmock-1.7.0.zip'")

file("MD5" "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/gmock-1.7.0.zip" actual_value)

if(NOT "${actual_value}" STREQUAL "073b984d8798ea1594f5e44d85b20d66")
  message(FATAL_ERROR "error: MD5 hash of
  /Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/gmock-1.7.0.zip
does not match expected value
  expected: '073b984d8798ea1594f5e44d85b20d66'
    actual: '${actual_value}'
")
endif()

message(STATUS "verifying file... done")
