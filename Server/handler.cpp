#ifndef __handler_cpp__
#define __handler_cpp__

#include <arpa/inet.h>

struct Handler
{
    virtual ~Handler() {}
    virtual void handleEvent(uint32_t events) = 0;
};

#endif