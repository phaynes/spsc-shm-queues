# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch"
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-build"
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix"
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/tmp"
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp"
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src"
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp${cfgdir}") # cfgdir has leading slash
endif()
