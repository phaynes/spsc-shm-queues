#include "CPUStats.h"
#include <iostream>

using namespace vn::common;

int main (int argc, char** argv)
{
    CPUStats stats;
    std::cout << "Cache Line Size is: " << stats.getCacheLineSize() << " bytes" << std::endl;
    std::cout << "l1 Cache Size is: " << stats.getL1CacheSize() << " bytes" << std::endl;
    std::cout << "l2 Cache Size is: " << stats.getL2CacheSize() << " bytes" << std::endl;
    std::cout << "l3 Cache Size is: " << stats.getL3CacheSize() << " bytes" << std::endl;

}
