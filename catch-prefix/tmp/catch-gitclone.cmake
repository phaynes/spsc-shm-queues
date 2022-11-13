# Distributed under the OSI-approved BSD 3-Clause License.  See accompanying
# file Copyright.txt or https://cmake.org/licensing for details.

cmake_minimum_required(VERSION 3.5)

if(EXISTS "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitclone-lastrun.txt" AND EXISTS "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitinfo.txt" AND
  "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitclone-lastrun.txt" IS_NEWER_THAN "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitinfo.txt")
  message(STATUS
    "Avoiding repeated git clone, stamp file is up to date: "
    "'/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitclone-lastrun.txt'"
  )
  return()
endif()

execute_process(
  COMMAND ${CMAKE_COMMAND} -E rm -rf "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch"
  RESULT_VARIABLE error_code
)
if(error_code)
  message(FATAL_ERROR "Failed to remove directory: '/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch'")
endif()

# try the clone 3 times in case there is an odd git clone issue
set(error_code 1)
set(number_of_tries 0)
while(error_code AND number_of_tries LESS 3)
  execute_process(
    COMMAND "/usr/local/bin/git" 
            clone --no-checkout --config "advice.detachedHead=false" "ssh://git@bitbucket.cd.sac.int.threatmetrix.com:7999/cmtp/catchunittests.git" "catch"
    WORKING_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src"
    RESULT_VARIABLE error_code
  )
  math(EXPR number_of_tries "${number_of_tries} + 1")
endwhile()
if(number_of_tries GREATER 1)
  message(STATUS "Had to git clone more than once: ${number_of_tries} times.")
endif()
if(error_code)
  message(FATAL_ERROR "Failed to clone repository: 'ssh://git@bitbucket.cd.sac.int.threatmetrix.com:7999/cmtp/catchunittests.git'")
endif()

execute_process(
  COMMAND "/usr/local/bin/git" 
          checkout "tmx-base" --
  WORKING_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch"
  RESULT_VARIABLE error_code
)
if(error_code)
  message(FATAL_ERROR "Failed to checkout tag: 'tmx-base'")
endif()

set(init_submodules TRUE)
if(init_submodules)
  execute_process(
    COMMAND "/usr/local/bin/git" 
            submodule update --recursive --init 
    WORKING_DIRECTORY "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch"
    RESULT_VARIABLE error_code
  )
endif()
if(error_code)
  message(FATAL_ERROR "Failed to update submodules in: '/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch'")
endif()

# Complete success, update the script-last-run stamp file:
#
execute_process(
  COMMAND ${CMAKE_COMMAND} -E copy "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitinfo.txt" "/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitclone-lastrun.txt"
  RESULT_VARIABLE error_code
)
if(error_code)
  message(FATAL_ERROR "Failed to copy script-last-run stamp file: '/Users/philiphaynes/devel/spscmqueue/cpp/catch-prefix/src/catch-stamp/catch-gitclone-lastrun.txt'")
endif()
