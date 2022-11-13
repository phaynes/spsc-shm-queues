cmake_minimum_required(VERSION 2.8 FATAL_ERROR)

project("case-queue")

enable_testing()

include(ExternalProject)


if(${CMAKE_SYSTEM_NAME} MATCHES "SunOS")
    set(SOLARIS TRUE)
endif(${CMAKE_SYSTEM_NAME} MATCHES "SunOS")

##########################################################
# Platform flags, etc.

# all UNIX-based platform compiler flags
if(UNIX)
    set(CMAKE_CXX_FLAGS "-Wall -std=c++11 -fexceptions -g -m64 -Werror ${CXX_FLAGS_OS_DEPENDENT}")
    set(CMAKE_CXX_FLAGS_DEBUG "-O0")
    set(CMAKE_CXX_FLAGS_RELEASE "-O3")
endif()

# platform specific flags
if(WIN32)
    add_definitions(-D_CRT_SECURE_NO_WARNINGS)
    add_definitions(-DNOMINMAX)

    set(CMAKE_CXX_FLAGS_DEBUG "/MTd /Od /Zi")
    set(CMAKE_CXX_FLAGS_RELEASE "/MT")

    # TODO: add MSVC flags
endif()

##########################################################
# Project variables, etc.

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/cppbuild/binaries")
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY  "${CMAKE_SOURCE_DIR}/cppbuild/lib")


##########################################################
# Catch usage

ExternalProject_Add(
    catch
    URL ${CMAKE_CURRENT_SOURCE_DIR}/cppbuild/Catch-1.2.1.tar.gz
    URL_MD5 70ee4cc959f8ff74ae43259de31d6eea
    BUILD_COMMAND ""
    CONFIGURE_COMMAND ""
    INSTALL_COMMAND ""
)

ExternalProject_Get_Property(catch source_dir)
set(CATCH_PATH ${source_dir})

# Catch include
include_directories("${CATCH_PATH}/single_include/")

##########################################################
# Aeron usage

ExternalProject_Add(
    aeron
    URL ${CMAKE_CURRENT_SOURCE_DIR}/cppbuild/Aeron-0.1.2-cm.tar.gz
    URL_MD5 041897be7241d67c05423f39d6977b33
    INSTALL_COMMAND ""
)

ExternalProject_Get_Property(aeron source_dir)
set(AERON_SOURCE_DIR ${source_dir})
ExternalProject_Get_Property(aeron binary_dir)
set(AERON_BINARY_DIR ${binary_dir})
ExternalProject_Get_Property(aeron download_dir)
set(AERON_PATH ${download_dir})

set(AERON_LIBS
    ${AERON_BINARY_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}aeron_client${CMAKE_STATIC_LIBRARY_SUFFIX}
)

set(AERON_COMMON_PATH "${AERON_PATH}/aeron/aeron-common/src/main/cpp")
set(AERON_3RDPARTY_PATH "${AERON_COMMON_PATH}/3rdparty")
set(AERON_CLIENT_PATH "${AERON_PATH}/aeron/aeron-client/src/main/cpp")

include_directories(${AERON_COMMON_PATH} ${AERON_CLIENT_PATH})

set(AC "${AERON_LIBS}")
##########################################################
# 3rd party libs

set(MM_IPC_PATH "${CMAKE_SOURCE_DIR}/src/main/cpp")
if(UNIX)
	if(APPLE)
	else(APPLE)
		# Assume SUN
		include_directories(/opt/local/include)
	endif(APPLE)
endif(UNIX)




# Uncomment these to make the Xcode build run.


##########################################################


add_subdirectory(${MM_IPC_PATH})


