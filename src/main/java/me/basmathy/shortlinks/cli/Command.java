package me.basmathy.shortlinks.cli;

public record Command(CommandType type, String argument, String rawInput) { }