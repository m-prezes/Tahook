#ifndef __host_h__
#define __host_h__

#include "handler.cpp"
#include "client.h"
#include "nlohmann/json.hpp"
#include <atomic>

using namespace std;
using json = nlohmann::json;

class Client;

class Host : public Handler
{
    int _epollFd;
    int _gameState;
    string _pin;
    atomic<bool> _questionActive;
    string _inputBuffer = "";

public:
    int _fd;
    int currentQuestion = 0;
    unordered_set<Client *> players;
    json questions;
    int currAnswers = 0;

    Host(int fd, int epollFd);
    ~Host();
    void sendToAllPlayers(const char *buffer, int count);
    void getAllPlayersNicks();

    int fd() const;
    int gameState() const;
    string pin() const;
    bool questionActive() const;

    virtual void handleEvent(uint32_t events) override;
    void write(const char *buffer, int count);

    void handleHostGame(string mess);
    void setQuestions(string mess);
    void setPin();
    void startGame(string mess);
    void sendQuestion(string mess);
    void showRank();
    void endGame(string mess);

    void remove();
    void timer();
};

extern unordered_set<Host *> hosts;

#endif