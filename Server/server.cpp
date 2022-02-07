#ifndef __main_cpp__
#define __main_cpp__

#include <cstdlib>
#include <cstdio>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <error.h>
#include <netdb.h>
#include <sys/epoll.h>
#include <unordered_set>
#include <signal.h>
#include <iostream>
#include <string>
#include <string.h>

#include "client.h"
#include "host.h"

int servFdForHosts;
int servFdForPlayers;
int epollFd;

void ctrl_c(int);
void sendToAllBut(int fd, char *buffer, int count);
uint16_t readPort(char *txt);
void setReuseAddr(int sock);

class : Handler
{
public:
    virtual void handleEvent(uint32_t events) override
    {
        if (events & EPOLLIN)
        {
            sockaddr_in clientAddr{};
            socklen_t clientAddrSize = sizeof(clientAddr);

            auto clientFd = accept(servFdForHosts, (sockaddr *)&clientAddr, &clientAddrSize);
            if (clientFd == -1)
                error(1, errno, "accept failed");

            printf("New Host from: %s:%hu (fd: %d)\n", inet_ntoa(clientAddr.sin_addr), ntohs(clientAddr.sin_port), clientFd);

            hosts.insert(new Host(clientFd, epollFd));
        }
        if (events & ~EPOLLIN)
        {
            error(0, errno, "Event %x on server socket", events);
            ctrl_c(SIGINT);
        }
    }
} servHandlerForHosts;

class : Handler
{
public:
    virtual void handleEvent(uint32_t events) override
    {
        if (events & EPOLLIN)
        {
            sockaddr_in clientAddr{};
            socklen_t clientAddrSize = sizeof(clientAddr);

            auto clientFd = accept(servFdForPlayers, (sockaddr *)&clientAddr, &clientAddrSize);
            if (clientFd == -1)
                error(1, errno, "accept failed");

            printf("New player from: %s:%hu (fd: %d)\n", inet_ntoa(clientAddr.sin_addr), ntohs(clientAddr.sin_port), clientFd);

            clients.insert(new Client(clientFd, epollFd));
        }
        if (events & ~EPOLLIN)
        {
            error(0, errno, "Event %x on server socket", events);
            ctrl_c(SIGINT);
        }
    }
} servHandlerForPlayers;

int main()
{
    // if (argc != 3)
    //     error(1, 0, "Need 2 arg (port)");
    // auto portForHosts = readPort(argv[1]);
    // auto portForPlayers = readPort(argv[2]);

    auto portForHosts = 1111;
    auto portForPlayers = 2222;

    servFdForHosts = socket(AF_INET, SOCK_STREAM, 0);
    servFdForPlayers = socket(AF_INET, SOCK_STREAM, 0);

    if (servFdForHosts == -1 || servFdForPlayers == -1)
        error(1, errno, "socket failed");

    signal(SIGINT, ctrl_c);
    signal(SIGPIPE, SIG_IGN);

    setReuseAddr(servFdForHosts);
    setReuseAddr(servFdForPlayers);

    sockaddr_in serverAddrForHosts{AF_INET, htons((short)portForHosts), {INADDR_ANY}, {0}};
    int res = bind(servFdForHosts, (sockaddr *)&serverAddrForHosts, sizeof(serverAddrForHosts));
    if (res)
        error(1, errno, "bind failed");

    res = listen(servFdForHosts, 1);
    if (res)
        error(1, errno, "listen failed");

    sockaddr_in serverAddrForPlayers{AF_INET, htons((short)portForPlayers), {INADDR_ANY}, {0}};
    res = bind(servFdForPlayers, (sockaddr *)&serverAddrForPlayers, sizeof(serverAddrForPlayers));
    if (res)
        error(1, errno, "bind failed");

    res = listen(servFdForPlayers, 1);
    if (res)
        error(1, errno, "listen failed");

    epollFd = epoll_create1(0);

    epoll_event eeH{EPOLLIN, {&servHandlerForHosts}};
    epoll_ctl(epollFd, EPOLL_CTL_ADD, servFdForHosts, &eeH);

    epoll_event eeP{EPOLLIN, {&servHandlerForPlayers}};
    epoll_ctl(epollFd, EPOLL_CTL_ADD, servFdForPlayers, &eeP);

    epoll_event ee;
    while (true)
    {
        if (-1 == epoll_wait(epollFd, &ee, 1, -1))
        {
            error(0, errno, "epoll_wait failed");
            ctrl_c(SIGINT);
        }
        cout << "Main while" << endl;
        ((Handler *)ee.data.ptr)->handleEvent(ee.events);
    }
}

uint16_t readPort(char *txt)
{
    char *ptr;
    auto port = strtol(txt, &ptr, 10);
    if (*ptr != 0 || port < 1 || (port > ((1 << 16) - 1)))
        error(1, 0, "illegal argument %s", txt);
    return port;
}

void setReuseAddr(int sock)
{
    const int one = 1;
    int res = setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &one, sizeof(one));
    if (res)
        error(1, errno, "setsockopt failed");
}

void ctrl_c(int)
{
    for (Client *client : clients)
        delete client;
    close(servFdForHosts);
    close(servFdForPlayers);
    printf("Closing server\n");
    exit(0);
}

#endif