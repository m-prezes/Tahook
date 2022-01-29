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
    epoll_event ee{EPOLLIN | EPOLLOUT | EPOLLRDHUP, {this}};
    epoll_ctl(_epollFd, EPOLL_CTL_ADD, _fd, &ee);
    string questionsRequest("Need questions!\n");
    write(questionsRequest.c_str());
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
        char buffer[1];
        ssize_t count = read(_fd, buffer, 1);
        if (count > 0)
        {
            if (buffer[0] != '\n')
            {
                _inputBuffer += buffer[0];
            }
            else
            {
                string mess = _inputBuffer;
                _inputBuffer = "";
                handleHostGame(mess);
                cout << _fd << ": " << mess << endl;
            }
        }
        else
            events |= EPOLLERR;
    }
    if (events & EPOLLOUT)
    {
        if (_outputBuffer.length() != 0)
        {
            writeFromBuffer();
        }
    }
    if ((events & ~EPOLLIN) & (events & ~EPOLLOUT))
    {
        remove();
    }
}

void Host::handleHostGame(string mess)
{
    if (_gameState == 0)
    {
        setQuestions(mess);
    }
    else if (_gameState == 1)
    {
        startGame(mess);
    }
    else if (_gameState == 2)
    {
        sendQuestion(mess);
    }
    else if (_gameState == 3)
    {
        endGame(mess);
    }
}

void Host::setQuestions(string mess)
{
    try
    {
        currentQuestion = 0;
        questions = json::parse(mess);
        setPin();
        _gameState++;
    }
    catch (const exception &e)
    {
        string questionIsInvalid("error:Cannot set questions!\n");
        write(questionIsInvalid.c_str());
    }
}

void Host::setPin()
{
    _pin = to_string(GAMEPIN);
    GAMEPIN++;
    string pinResponse("PIN:" + _pin + "\n");
    write(pinResponse.c_str());
}

void Host::startGame(string mess)
{
    if (mess == "start")
    {
        if (players.size() > 1)
        {
            string gameStartedMessage("Start game\n");
            write(gameStartedMessage.c_str());
            sendQuestion("send");
            _gameState++;
        }
        else
        {
            string toLowPlayers("error:Not enough players!\n");
            write(toLowPlayers.c_str());
        }
    }
    else
    {
        string startRequest("error:Need start request!\n");
        write(startRequest.c_str());
    }
}

void Host::sendQuestion(string mess)
{
    if (!_questionActive)
    {
        if (mess == "send")
        {
            currAnswers = 0;
            auto &el = questions[currentQuestion];
            string messageQuestion = "question:" + el.dump() + "\n";
            sendToAllPlayers(messageQuestion.c_str());
            write(messageQuestion.c_str());
            _questionActive = true;
            timer();
        }
        else
        {
            string questionSendRequest("error:Need question send request!\n");
            write(questionSendRequest.c_str());
        }
    }
    else
    {
        string questionActive("error:There is an active question!\n");
        write(questionActive.c_str());
    }
}

void Host::timer()
{
    thread t([=, this]()
             {
                 time_t time_1;
                 time_t time_2;
                 time(&time_1);
                 time(&time_2);

                 while (_questionActive && ((time_2 - time_1) * 1000) < (questions[currentQuestion]["answer_time"]))
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

    json rank = json::array();

    auto it = players.begin();
    while (it != players.end())
    {
        json playerPoints = json({});
        Client *player = *it;
        it++;
        playerPoints["userName"] = player->nick;
        playerPoints["points"] = player->points;
        rank.push_back(playerPoints);
    }
    string rankingMessage = "ranking:" + rank.dump() + "\n";
    write(rankingMessage.c_str());
    sendToAllPlayers(rankingMessage.c_str());
}

void Host::endGame(string mess)
{
    if (mess == "send")
    {
        string endInfo("Game has ended!\n");
        write(endInfo.c_str());
        sendToAllPlayers(endInfo.c_str());
        _gameState++;
    }
    else
    {
        string endRequest("error:Need end request!\n");
        write(endRequest.c_str());
    }
}

void Host::sendToAllPlayers(const char *buffer)
{
    auto it = players.begin();
    while (it != players.end())
    {
        Client *player = *it;
        it++;
        player->write(buffer);
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
    string playersList("players:" + playersNicks.dump() + "\n");
    write(playersList.c_str());
}

void Host::write(const char *buffer)
{
    _outputBuffer += string(buffer);
}

void Host::writeFromBuffer()
{
    int bytes = ::write(_fd, _outputBuffer.c_str(), _outputBuffer.length());
    _outputBuffer = _outputBuffer.substr(bytes, _outputBuffer.length());
}

void Host::remove()
{
    printf("removing %d\n", _fd);
    auto it = players.begin();
    while (it != players.end())
    {
        Client *player = *it;
        it++;
        if (_gameState != 4)
        {
            string hostDisconnect("critError:Host has disconnected!\n");
            write(hostDisconnect.c_str());
            player->write(hostDisconnect.c_str());
        }
        player->removeHost();
    }

    hosts.erase(this);
    delete this;
}

#endif