# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

if("/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/HdrHistogram_c-master.zip" STREQUAL "")
  message(FATAL_ERROR "LOCAL can't be empty")
endif()

if(NOT EXISTS "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/HdrHistogram_c-master.zip")
  message(FATAL_ERROR "File not found: /Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/HdrHistogram_c-master.zip")
endif()

if("MD5" STREQUAL "")
  message(WARNING "File will not be verified since no URL_HASH specified")
  return()
endif()

if("1b1d7a86417d52d25639c395730f0b34" STREQUAL "")
  message(FATAL_ERROR "EXPECT_VALUE can't be empty")
endif()

message(STATUS "verifying file...
     file='/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/HdrHistogram_c-master.zip'")

file("MD5" "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/HdrHistogram_c-master.zip" actual_value)

if(NOT "${actual_value}" STREQUAL "1b1d7a86417d52d25639c395730f0b34")
  message(FATAL_ERROR "error: MD5 hash of
  /Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron/cppbuild/HdrHistogram_c-master.zip
does not match expected value
  expected: '1b1d7a86417d52d25639c395730f0b34'
    actual: '${actual_value}'
")
endif()

message(STATUS "verifying file... done")
