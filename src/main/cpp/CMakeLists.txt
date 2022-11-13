set(IPC_HEADERS 
  CPUStats.h CacheLine.h QueueMemoryMappedBuffer.h SpscMemoryMappedCacheLineQueue.h Atomic64.h)

set(IPC_SRC
  SpscMemoryMappedCacheLineQueue.cpp CPUStats.cpp QueueMemoryMappedBuffer.cpp)


add_library(IPCQueue STATIC ${IPC_SRC} ${IPC_HEADERS})
add_executable(CacheLineSize CPUStatsMain.cpp CPUStats.cpp CPUStats.h)
add_executable(IPCQueueMain IPCQueueMain.cpp ${IPC_HEADERS})
add_executable(spscMMQueueTests testSpscMemoryMappedQueue.cpp)

target_link_libraries(IPCQueueMain
    IPCQueue
    ${AC} )
 
target_link_libraries(spscMMQueueTests
    IPCQueue
    ${AC} )
 
add_test(NAME testSPSC COMMAND spscMMQueueTests)
