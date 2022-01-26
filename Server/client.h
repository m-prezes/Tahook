#ifndef __client_h__
#define __client_h__

#include "handler.cpp"
#include "host.h"

using namespace std;
class Host;

class Client : public Handler
{
    int _fd;
    int _epollFd;
    Host *_host;
    string _inputBuffer;

public:
    bool _connectedToGame;
    string nick;
    int points = 0;

    Client(int fd, int epollFd);
    virtual ~Client();
    int fd() const;
    virtual void handleEvent(uint32_t events) override;
    void remove();

    void write(const char *buffer, int count);
    void writeToHost(const char *buffer, int count);

    void handlePlayerGame(string mess);
    void joinGame(string pin);
    void setNick(string nickName);
    void sendAnswer(string mess);
    void endGame();
    void removeHost();
};

extern std::unordered_set<Client *> clients;

#endif