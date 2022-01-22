#ifndef __host_cpp__
#define __host_cpp__

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
#include <thread>
#include <chrono>
#include <ctime>

#include "host.h"
#include "nlohmann/json.hpp"

using json = nlohmann::json;
using namespace std;

int GAMEPIN = 123456;

std::unordered_set<Host *> hosts;

Host::Host(int fd, int epollFd) : _epollFd(epollFd), _gameState(0), _questionActive(false), _fd(fd)
{
    epoll_event ee{EPOLLIN | EPOLLRDHUP, {.ptr = this}};
    epoll_ctl(_epollFd, EPOLL_CTL_ADD, _fd, &ee);
    string questionsRequest("Need questions!");
    write(questionsRequest.c_str(), questionsRequest.length());
}

Host::~Host()
{
    epoll_ctl(_epollFd, EPOLL_CTL_DEL, _fd, nullptr);
    shutdown(_fd, SHUT_RDWR);
    close(_fd);
}

int Host::fd() const { return _fd; }
int Host::gameState() const { return _gameState; }
string Host::pin() const { return _pin; }
bool Host::questionActive() const { return _questionActive; }

void Host::handleEvent(uint32_t events)
{
    if (events & EPOLLIN)
    {
        char buffer[256];
        ssize_t count = read(_fd, buffer, 256);
        if (count > 0)
        {
            string mess(buffer);
            mess = mess.substr(0, count);

            if (_gameState == 0)
            {
                setQuestions(mess);
            }
            else if (_gameState == 1)
            {
                setPin(mess);
            }
            else if (_gameState == 2)
            {
                startGame(mess);
            }
            else if (_gameState == 3)
            {
                sendQuestion(mess);
            }
            else if (_gameState == 4)
            {
                endGame(mess);
            }
            printf("%d: %.*s", _fd, (int)count, buffer);
        }
        else
            events |= EPOLLERR;
    }
    if (events & ~EPOLLIN)
    {
        remove();
    }
}

void Host::setQuestions(string mess)
{
    // cout << mess << endl; // [TODO] - pozniej wujebac ale teraz compiler ma problem bo unused
    try
    {
        currentQuestion = 0;
        // string mess1 = R"([{"question":"aaaa","anwser_a":"a","anwser_b":"b","anwser_c":"c","anwser_d":"d", "time":20000,"correct":"a"},{"question":"bbbb","anwser_a":"a","anwser_b":"b","anwser_c":"c","anwser_d":"d", "time":20000,"correct":"a"}])";
        questions = json::parse(mess);
        _gameState++;
    }
    catch (const exception &e)
    {
        string questionIsInvalid("Cannot set question!");
        write(questionIsInvalid.c_str(), questionIsInvalid.length());
    }
}

void Host::setPin(string mess) // [TODO]: dynimiczny pin
{
    if (mess.substr(0, mess.length() - 1) == "pin") //[TODO]: -1 bo nowa lina w terminalu
    {
        _pin = to_string(GAMEPIN);
        GAMEPIN++;
        write(_pin.c_str(), strlen(_pin.c_str()));
        _gameState++;
    }
    else
    {
        string pinRequest("Need pin request!");
        write(pinRequest.c_str(), pinRequest.length());
    }
}

void Host::startGame(string mess)
{
    if (mess.substr(0, mess.length() - 1) == "start") //[TODO]: -1 bo nowa lina w terminalu
    {
        if (players.size() > 1)
        {
            sendQuestion("send "); //[TODO]: usunac spacje
            _gameState++;
        }
        else
        {
            string toLowPlayers("Za mało graczy!");
            write(toLowPlayers.c_str(), toLowPlayers.length());
        }
    }
    else
    {
        string startRequest("Need start request!");
        write(startRequest.c_str(), startRequest.length());
    }
}

void Host::sendQuestion(string mess)
{
    if (!_questionActive)
    {
        if (mess.substr(0, mess.length() - 1) == "send") //[TODO]: -1 bo nowa lina w terminalu
        {
            currAnswers = 0;
            auto &el = questions[currentQuestion];
            sendToAllPlayers(el.dump().c_str(), el.dump().length());
            write(el.dump().c_str(), el.dump().length());
            _questionActive = true;
            timer();
        }
        else
        {
            string questionSendRequest("Need question send request!");
            write(questionSendRequest.c_str(), questionSendRequest.length());
        }
    }
    else
    {
        string questionActive("There is active question!");
        write(questionActive.c_str(), questionActive.length());
    }
}

// void Host::timer()
// {
//     thread t([=]()
//              {
//                  std::this_thread::sleep_for(std::chrono::milliseconds(questions[currentQuestion]["time"]));
//                  if (currentQuestion == questions.size() - 1)
//                  {
//                      _gameState++;
//                  }
//                  else
//                  {
//                      currentQuestion++;
//                  }
//                  _questionActive = false;
//                  showRank();
//                  return;
//              });
//     t.detach();
// }

void Host::timer()
{
    thread t([=, this]()
             {
                 time_t time_1;
                 time_t time_2;
                 time(&time_1);
                 time(&time_2);

                 while (_questionActive && ((time_2 - time_1) * 1000) < (questions[currentQuestion]["time"]))
                 {
                     time(&time_2);
                     if (float(currAnswers) / float(players.size()) >= 2.0 / 3.0)
                     {
                         _questionActive = false;
                     }
                 }

                 if (currentQuestion == int(questions.size() - 1))
                 {
                     _gameState++;
                 }
                 else
                 {
                     currentQuestion++;
                 }
                 _questionActive = false;
                 showRank();
                 return;
             });
    t.detach();
}

void Host::showRank()
{
    sendToAllPlayers(string("timeout").c_str(), 7);
    write(string("timeout").c_str(), 7);

    json rank = json({});
    auto it = players.begin();
    while (it != players.end())
    {
        Client *player = *it;
        it++;
        rank[player->nick] = player->points;
    }
    write(rank.dump().c_str(), rank.dump().length());
    sendToAllPlayers(rank.dump().c_str(), rank.dump().length());
}

void Host::endGame(string mess)
{
    if (mess.substr(0, mess.length() - 1) == "end") //[TODO]: -1 bo nowa lina w terminalu
    {
        string endInfo("Game has ended!");
        write(endInfo.c_str(), endInfo.length());

        auto it = players.begin();
        while (it != players.end())
        {
            Client *player = *it;
            it++;
            player->write(endInfo.c_str(), endInfo.length());
            player->_connectedToGame = false;
        }
        remove();
    }
    else
    {
        string endRequest("Need end request!");
        write(endRequest.c_str(), endRequest.length());
    }
}

void Host::sendToAllPlayers(const char *buffer, int count)
{
    auto it = players.begin();
    while (it != players.end())
    {
        Client *player = *it;
        it++;
        player->write(buffer, count);
    }
}

void Host::getAllPlayersNicks()
{
    json playersNicks = json({});
    int i = 0;
    auto it = players.begin();
    while (it != players.end())
    {
        Client *player = *it;
        it++;
        playersNicks[to_string(i)] = player->nick;
        i++;
    }
    cout << playersNicks.dump() << endl;
    write(playersNicks.dump().c_str(), playersNicks.dump().length());
}

void Host::write(const char *buffer, int count)
{
    if (count != ::write(_fd, buffer, count))
        remove();
}

void Host::remove()
{
    printf("removing %d\n", _fd);
    hosts.erase(this);
    delete this;
}

#endif