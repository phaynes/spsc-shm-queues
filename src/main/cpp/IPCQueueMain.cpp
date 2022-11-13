#include <chrono>
#include <iomanip>
#include <limits>
#include <locale>
#include <thread>
#include <iostream>
#include <sstream>
#include <util/CommandOptionParser.h>
#include <stdlib.h>

#include "SpscMemoryMappedCacheLineQueue.h"
#include "Atomic64.h"

using namespace aeron::util;
using namespace vn::common::collections;

static CacheLine* sharedBuffer (0);

struct Settings
{
    std::string memoryMappedFile = "queue.ipc";
    std::string mode = "sink";
};

/**
 * Parse the command line to pull out options.
 */
Settings parseCmdLine(CommandOptionParser& cp, int argc, char** argv);

/**
  * Synchronize multiple runs by blocking will a certain state is entered.
 */
static void waitTillNextState(int stateToWaitTill, int nextState);

/**
 * Produce a set of cache lines with the data populated with an integer.
 */
static void producerRun(SpscMemoryMappedCacheLineQueue& queue, int runNumber);

/**
 * Consume a set of cache lines with a single integer.
 */
static void consumerRun(SpscMemoryMappedCacheLineQueue& queue, int runNumber);

static const char optHelp     = 'h';
static const char optPath     = 'p';
static const char optMode     = 'm';

static const int REPETITIONS = 60000000;

/**
 * Command line test rig to either consume or produce a set of Cache lines.
 */
int main (int argc, char** argv)
{
    CommandOptionParser cp;
    cp.addOption(CommandOption (optHelp,     0, 0, "                Displays help information."));
    cp.addOption(CommandOption (optPath,  	 1, 1, "path       		Memory Mapped File Path."));
    cp.addOption(CommandOption (optMode,  	 1, 1, "mode       		Processing Mode {sink | source}."));

    try
    {
        Settings settings = parseCmdLine(cp, argc, argv);
        QueueMemoryMappedBuffer mappedBuffer(settings.memoryMappedFile.c_str());
        std::cout << "Queue size is " << mappedBuffer.getCapacity() << std::endl;

        mappedBuffer.mapProducerBuffer();

        SpscMemoryMappedCacheLineQueue queue(mappedBuffer,
            (settings.mode == "source") ? SpscMemoryMappedCacheLineQueue::IS_PRODUCER : SpscMemoryMappedCacheLineQueue::IS_CONSUMER );
        sharedBuffer = queue.getBuffer();

        // Reset the coordination mechanism if starting source.
        if (settings.mode == "source")
        {
          sharedBuffer->putInt64Ordered(SpscMemoryMappedCacheLineQueue::SYNC_ADDRESS_POS, 0);
        }

        for (int run = 0; run < 10; run++)
        {
            if (settings.mode == "source")
            {
                producerRun(queue, run);
            }
            else
            {
                consumerRun(queue, run);
            }
        }
    }
    catch (CommandOptionException& e)
    {
        std::cerr << "ERROR: " << e.what() << std::endl << std::endl;
        cp.displayOptionsHelp(std::cerr);
        return -1;
    }
    return 0;
}

/**
 * Formatting util to make it easier to read the results.
 */
template<class T>
std::string FormatWithCommas(T value)
{
    std::stringstream ss;
    ss.imbue(std::locale("C"));
    ss << std::fixed << value;
    return ss.str();
}

static void producerRun(SpscMemoryMappedCacheLineQueue& queue, int runNumber)
{
    waitTillNextState(3 * runNumber, 3 * runNumber+1);

    std::chrono::time_point<std::chrono::system_clock> start, end;
    start = std::chrono::system_clock::now();

    int i = REPETITIONS;
    int f = 0;
    CacheLine cacheLine(0, 0);
    do
    {
        while (!queue.preOffer(cacheLine))
        {
            std::this_thread::yield();
            f++;
        }
        cacheLine.putInt32(0, i);
        queue.offer();
    }
    while (0 != --i);

    end = std::chrono::system_clock::now();

    waitTillNextState(3 * runNumber + 2, 3 * runNumber + 3);

    std::chrono::duration<double> duration = end-start;

    double ops = REPETITIONS / duration.count();
    std::cout.precision(9);
    std::cout << runNumber << " - ops/sec=" << FormatWithCommas(ops) << " - SpscMemoryMappedCacheLineQueue " << f << std::endl;
}

static void consumerRun(SpscMemoryMappedCacheLineQueue& queue, int runNumber)  {

	int outOfOrderErrors = 0;

    waitTillNextState(3 * runNumber + 1, 3 * runNumber + 1);

    std::chrono::time_point<std::chrono::system_clock> start, end;
    start = std::chrono::system_clock::now();

    int i = REPETITIONS;
    int expectedResult = REPETITIONS;
    int queueEmpty = 0;
    do
    {
        while (queue.poll() == 0) {
            queueEmpty++;
            std::this_thread::yield();
        }
        long result = queue.getCurrentCacheLine().getInt32(0);
        if (result != i)
        {
        	expectedResult = result;
        	outOfOrderErrors++;
        }
        queue.releaseCacheLine();
        expectedResult--;
    }
    while (0 != --i);

    end = std::chrono::system_clock::now();
    waitTillNextState(3 * runNumber + 1, 3 * runNumber + 2);

    std::chrono::duration<double> duration = end-start;
    std::cout << "Duration is " << (1000 * duration.count()) << " ms " << queueEmpty << std::endl;
    std::cout << "Out Of Order Errors  " << outOfOrderErrors << std::endl;
}

static void waitTillNextState(int stateToWaitTill, int nextState)
{
    while (!sharedBuffer->compareAndSetInt64(SpscMemoryMappedCacheLineQueue::SYNC_ADDRESS_POS, stateToWaitTill, nextState))
    {
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }
}

Settings parseCmdLine(CommandOptionParser& cp, int argc, char** argv)
{
    cp.parse(argc, argv);
    if (cp.getOption(optHelp).isPresent())
    {
        cp.displayOptionsHelp(std::cout);
        exit(0);
    }

    Settings s;
    s.memoryMappedFile = cp.getOption(optPath).getParam(0, s.memoryMappedFile);
    s.mode = cp.getOption(optMode).getParam(0, s.mode);
    return s;
}
