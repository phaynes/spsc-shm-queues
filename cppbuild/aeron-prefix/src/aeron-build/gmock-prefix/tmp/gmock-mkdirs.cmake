# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/src/gmock"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/src/gmock-build"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/tmp"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/src/gmock-stamp"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/src"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/src/gmock-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/src/gmock-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/gmock-prefix/src/gmock-stamp${cfgdir}") # cfgdir has leading slash
endif()
