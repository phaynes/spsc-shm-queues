# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

file(MAKE_DIRECTORY
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/src/hdr_histogram"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/src/hdr_histogram-build"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/tmp"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/src/hdr_histogram-stamp"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/src"
  "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/src/hdr_histogram-stamp"
)

set(configSubDirs )
foreach(subDir IN LISTS configSubDirs)
    file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/src/hdr_histogram-stamp/${subDir}")
endforeach()
if(cfgdir)
  file(MAKE_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cppbuild/aeron-prefix/src/aeron-build/hdr_histogram-prefix/src/hdr_histogram-stamp${cfgdir}") # cfgdir has leading slash
endif()
