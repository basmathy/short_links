package me.basmathy.shortlinks.cli;

public enum CommandType {
    HELP,
    EXIT,
    LOGIN,
    LOGOUT,
    SHORTEN, // http(s)://...
    OPEN, // clck.ru/XXXXXXXX
    UNKNOWN
}