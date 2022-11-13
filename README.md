# Shared Memory: Single Producer Single Consumer Queues

In a concurrent system the passing of messages represents an effective way to coordinage different threads and processes.
However, in Java, and other environments the queueing abstraction is not terribly fast. 

In a series of blog posts Nitsan Wakart introduces the concept of lock free queues to achieve
message passing concurrency at a throughput significantly higher than the standard JDK.

http://psy-lob-saw.blogspot.com/2013/03/single-producerconsumer-lock-free-queue.html

Then came two goals:
1. The ability / need to share data betweeen C++ and Java processes; and 
2. Separate different processing elements to specific CPU cores to avoid processing contention. 

Through a shared memory buffer with a cache coherent queue size, it turned out possible to implement a very fast and high 
thoughput cross language message queue for x86 processes informed by a Herb Sutter talk [1]

The work was later adapted and incorporated into the Aeron messaging library and has evolved over time.

The source currently reflects older work that achieves 200-300 million msgs / second on an x86 processor. 

On an M1 mac the cross language capability is not currently working through the x86 translation area.

```bash

For the C++ version in one process 
 ./binaries/IPCQueueMain -m sink -p /Volumes/ram-disk/queue.ipc

And in another process window run:

bash-3.2$ ./binaries/IPCQueueMain -m source -p /Volumes/ram-disk/queue.ipc
Queue size is 4096
0 - ops/sec=273561635.716207 - SpscMemoryMappedCacheLineQueue 84746
1 - ops/sec=244582497.676466 - SpscMemoryMappedCacheLineQueue 127089
2 - ops/sec=271538673.895630 - SpscMemoryMappedCacheLineQueue 124988
3 - ops/sec=240008640.311051 - SpscMemoryMappedCacheLineQueue 126630
4 - ops/sec=306663804.471158 - SpscMemoryMappedCacheLineQueue 110985
5 - ops/sec=311274357.218452 - SpscMemoryMappedCacheLineQueue 120732
6 - ops/sec=134218879.675011 - SpscMemoryMappedCacheLineQueue 164788
7 - ops/sec=326550160.825954 - SpscMemoryMappedCacheLineQueue 115706
8 - ops/sec=291214071.463933 - SpscMemoryMappedCacheLineQueue 120360
9 - ops/sec=165958117.703029 - SpscMemoryMappedCacheLineQueue 153100

&

Similarly for the Java version run:
 java  -XX:+UseCondCardMark -XX:CompileThreshold=100000 -jar spscsmqueue-1.0.0-SNAPSHOT-jar-with-dependencies.jar  -q /Volumes/ram-disk/queue.ipc -m source
&
 java  -XX:+UseCondCardMark -XX:CompileThreshold=100000 -jar spscsmqueue-1.0.0-SNAPSHOT-jar-with-dependencies.jar  -q /Volumes/ram-disk/queue.ipc -m sink
 
 ```






[1] https://herbsutter.com/2013/02/11/atomic-weapons-the-c-memory-model-and-modern-hardware/. 
The slides for this talk are included in the doc directory. 

[2]https://github.com/real-logic/aeron/tree/master/aeron-client/src/main/cpp/concurrent
