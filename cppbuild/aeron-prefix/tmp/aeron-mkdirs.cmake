# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/tmp"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-stamp"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-stamp${cfgdir}") # cfgdir has leading slash
endif()
