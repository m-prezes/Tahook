cmake_minimum_required(VERSION 3.10)


# set the project name
project(TahookServer)

find_package(Threads REQUIRED)
set(THREADS_PREFER_PTHREAD_FLAG ON)



# add the executable
add_executable(TahookServer client.cpp handler.cpp host.cpp server.cpp)


target_link_libraries(TahookServer Threads::Threads)