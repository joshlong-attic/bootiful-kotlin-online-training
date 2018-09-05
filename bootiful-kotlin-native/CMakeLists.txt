cmake_minimum_required(VERSION 3.8)

set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_LIST_DIR}/KotlinCMakeModule)

project(untitled Kotlin)

konanc_executable(
        NAME HelloWorld
        SOURCES hello.kt
)