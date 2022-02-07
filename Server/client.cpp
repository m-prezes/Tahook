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
#include <exception>

#include "client.h"

using namespace std;

unordered_set<Client *> clients;

Client::Client(int fd, int epollFd) : _fd(fd), _epollFd(epollFd), _connectedToGame(false)
{
    epoll_event ee{EPOLLIN | EPOLLOUT | EPOLLRDHUP, {this}};
    epoll_ctl(_epollFd, EPOLL_CTL_ADD, _fd, &ee);
    string nickRequest("Enter nick:\n");
    write(nickRequest.c_str());
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
                handlePlayerGame(mess);
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
        else
        {
            epoll_event ee{EPOLLIN | EPOLLRDHUP, {this}};
            epoll_ctl(_epollFd, EPOLL_CTL_MOD, _fd, &ee);
        }
    }
    if ((events & ~EPOLLIN) & (events & ~EPOLLOUT))
    {
        remove();
    }
}

void Client::handlePlayerGame(string mess)
{
    if (!_connectedToGame && nick.empty())
    {
        setNick(mess);
    }
    else if (!_connectedToGame)
    {
        joinGame(mess);
    }
    else if (mess == "Game has ended!")
    {
        endGame();
    }
    else
    {
        sendAnswer(mess);
    }
}

void Client::setNick(string nickName)
{
    if (nickName != "")
    {
        nick = nickName;
        string pinRequest("Enter pin:\n");
        write(pinRequest.c_str());
    }
    else
    {
        string nickIsEmpty("error:Invalid nick!\n");
        write(nickIsEmpty.c_str());
    }
}

void Client::joinGame(string pin)
{
    auto it = hosts.begin();
    while (it != hosts.end())
    {
        Host *h = *it;
        it++;
        if (h->pin() == pin && h->gameState() == 1)
        {
            h->players.insert(this);
            _host = h;
            _connectedToGame = true;
            break;
        }
    }
    if (!_connectedToGame)
    {
        string pinIsNotValid("error:Invalid pin!\n");
        write(pinIsNotValid.c_str());
    }
    else
    {
        string pinIsValid("Joined game\n");
        write(pinIsValid.c_str());
        _host->getAllPlayersNicks();
    }
}

void Client::sendAnswer(string mess)
{
    if (_host->gameState() == 2 && _host->questionActive())
    {

        try
        {
            json answer = json::parse(mess);
            int currQue = _host->currentQuestion;

            if (answer["question"] == currQue)
            {

                string messageForHost("answers:{\"currAnswers\":" + to_string(_host->currAnswers + 1) + "}\n");
                writeToHost(messageForHost.c_str());

                if (answer["answer"].dump() == _host->questions[currQue]["correctAnswer"].dump())
                {
                    points += 100;
                    string isAnswerCorrect("isAnswerCorrect:true\n");
                    write(isAnswerCorrect.c_str());
                }
                _host->currAnswers++;
            }
        }
        catch (const exception &e)
        {
            string answerIsInvalid("error:Invalid answer format!\n");
            write(answerIsInvalid.c_str());
        }
    }
    else
    {

        string gameNotStarted("error:Question has not send yet!\n");
        write(gameNotStarted.c_str());
    }
}

void Client::endGame()
{
    printf("removing %d\n", _fd);
    delete this;
}

void Client::write(const char *buffer)
{
    _outputBuffer += string(buffer);
    epoll_event ee{EPOLLIN | EPOLLOUT | EPOLLRDHUP, {this}};
    epoll_ctl(_epollFd, EPOLL_CTL_MOD, _fd, &ee);
}

void Client::writeFromBuffer()
{
    int bytes = ::write(_fd, _outputBuffer.c_str(), _outputBuffer.length());
    _outputBuffer = _outputBuffer.substr(bytes, _outputBuffer.length());
}

void Client::writeToHost(const char *buffer)
{
    _host->write(buffer);
}

void Client::removeHost()
{
    _host = nullptr;
}

void Client::remove()
{
    printf("removing %d\n", _fd);
    if (_host != nullptr)
    {

        _host->players.erase(this);
        _host->getAllPlayersNicks();
        _host = nullptr;
    }
    delete this;
}

#endif