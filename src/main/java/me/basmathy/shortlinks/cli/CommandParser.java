package me.basmathy.shortlinks.cli;

final class CommandParser {

    private static final String SHORT_PREFIX = "clck.ru/";

    private CommandParser() {}

    static Command parse(String input) {
        if (input == null) return new Command(CommandType.UNKNOWN, null, "");

        String normalized = input.replaceAll("\\s+", " ").trim();

        if (normalized.equals("help")) return new Command(CommandType.HELP, null, normalized);
        if (normalized.equals("exit") || normalized.equals("quit")) return new Command(CommandType.EXIT, null, normalized);
        if (normalized.equals("logout")) return new Command(CommandType.LOGOUT, null, normalized);

        if (normalized.startsWith("login")) {
            String[] parts = normalized.split(" ");
            String arg = (parts.length == 2) ? parts[1] : null;
            return new Command(CommandType.LOGIN, arg, normalized);
        }

        if (normalized.startsWith("http")) {
            return new Command(CommandType.SHORTEN, normalized, normalized);
        }

        if (normalized.startsWith(SHORT_PREFIX)) {
            return new Command(CommandType.OPEN, normalized, normalized);
        }

        return new Command(CommandType.UNKNOWN, null, normalized);
    }
}
