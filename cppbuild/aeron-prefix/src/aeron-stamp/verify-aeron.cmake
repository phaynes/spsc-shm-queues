# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

if("/Users/philiphaynes/devel/spscmqueue/cppbuild/Aeron-0.1.2-cm.tar.gz" STREQUAL "")
  message(FATAL_ERROR "LOCAL can't be empty")
endif()

if(NOT EXISTS "/Users/philiphaynes/devel/spscmqueue/cppbuild/Aeron-0.1.2-cm.tar.gz")
  message(FATAL_ERROR "File not found: /Users/philiphaynes/devel/spscmqueue/cppbuild/Aeron-0.1.2-cm.tar.gz")
endif()

if("MD5" STREQUAL "")
  message(WARNING "File will not be verified since no URL_HASH specified")
  return()
endif()

if("041897be7241d67c05423f39d6977b33" STREQUAL "")
  message(FATAL_ERROR "EXPECT_VALUE can't be empty")
endif()

message(STATUS "verifying file...
     file='/Users/philiphaynes/devel/spscmqueue/cppbuild/Aeron-0.1.2-cm.tar.gz'")

file("MD5" "/Users/philiphaynes/devel/spscmqueue/cppbuild/Aeron-0.1.2-cm.tar.gz" actual_value)

if(NOT "${actual_value}" STREQUAL "041897be7241d67c05423f39d6977b33")
  message(FATAL_ERROR "error: MD5 hash of
  /Users/philiphaynes/devel/spscmqueue/cppbuild/Aeron-0.1.2-cm.tar.gz
does not match expected value
  expected: '041897be7241d67c05423f39d6977b33'
    actual: '${actual_value}'
")
endif()

message(STATUS "verifying file... done")
