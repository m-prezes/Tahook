#ifndef __client_cpp__
#define __client_cpp__

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

using namespace std;

unordered_set<Client *> clients;

Client::Client(int fd, int epollFd) : _fd(fd), _epollFd(epollFd), _connectedToGame(false)
{
    epoll_event ee{EPOLLIN | EPOLLRDHUP, {.ptr = this}};
    epoll_ctl(_epollFd, EPOLL_CTL_ADD, _fd, &ee);
    string nickRequest("Enter nick: ");
    write(nickRequest.c_str(), nickRequest.length());
}

Client::~Client()
{
    epoll_ctl(_epollFd, EPOLL_CTL_DEL, _fd, nullptr);
    shutdown(_fd, SHUT_RDWR);
    close(_fd);
}

int Client::fd() const { return _fd; }

void Client::handleEvent(uint32_t events)
{
    if (events & EPOLLIN)
    {
        char buffer[256];
        ssize_t count = read(_fd, buffer, 256);
        if (count > 0)
        {

            string mess(buffer);
            mess = mess.substr(0, count);

            if (!_connectedToGame && nick.empty())
            {
                setNick(mess);
            }
            else if (!_connectedToGame)
            {
                joinGame(mess);
            }
            else
            {
                sendAnswer(mess);
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

void Client::setNick(string nickName)
{
    nick = nickName.substr(0, nickName.length() - 1); // [TODO]: -1 bo nowa linia w terminalu
    string pinRequest("Enter pin: ");
    write(pinRequest.c_str(), pinRequest.length());
}

void Client::joinGame(string pin)
{
    auto it = hosts.begin();
    while (it != hosts.end())
    {
        Host *h = *it;
        it++;
        if (h->pin() == pin.substr(0, pin.length() - 1) && h->gameState() == 2) // [TODO]: -1 bo nowa linia w terminalu
        {
            h->players.insert(this);
            _host = h;
            _connectedToGame = true;
            break;
        }
    }
    if (!_connectedToGame)
    {
        string pinIsNotValid("Invalid pin!");
        write(pinIsNotValid.c_str(), pinIsNotValid.length());
    }
    else
    {
        string pinIsValid("Joined game");
        write(pinIsValid.c_str(), pinIsValid.length());
        _host->getAllPlayersNicks();
    }
}

void Client::sendAnswer(string mess)
{
    if (_host->gameState() == 3 && _host->questionActive())
    {

        try
        {
            json answer = json::parse(mess.substr(0, mess.length() - 1)); //[TODO]: -1 bo nowa linia w terminalu
            int currQue = _host->currentQuestion;

            if (answer["question"] == currQue)
            {
                _host->currAnswers++;
                string messageForHost("{'currAnswers':" + to_string(_host->currAnswers) + "}");
                cout << messageForHost << endl;
                writeToHost(messageForHost.c_str(), messageForHost.length());

                if (answer["answer"].dump() == _host->questions[currQue]["correct"].dump())
                {
                    points += 100;
                }
            }
        }
        catch (const exception)
        {
            string answerIsInvalid("Invalid answer format!");
            write(answerIsInvalid.c_str(), answerIsInvalid.length());
        }
    }
    else
    {
        string gameNotStarted("Question has not send yet!");
        write(gameNotStarted.c_str(), gameNotStarted.length());
    }
}

void Client::write(const char *buffer, int count)
{
    if (count != ::write(_fd, buffer, count))
        remove();
}

void Client::writeToHost(const char *buffer, int count)
{
    if (count != ::write(_host->_fd, buffer, count))
        remove();
}

void Client::remove()
{
    printf("removing %d\n", _fd);
    _host->players.erase(this);
    delete this;
}

#endif