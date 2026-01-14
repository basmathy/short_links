package me.basmathy.shortlinks.cli;

import me.basmathy.shortlinks.config.StorageConfig;
import me.basmathy.shortlinks.data.StoredLink;
import me.basmathy.shortlinks.data.Storage;

import java.util.Scanner;

public final class Console {

    private Console() {}

    public static void startSession() {
        Storage<String, StoredLink> storage = new Storage<>(StorageConfig.STORAGE_FILE);
        storage.load();

        CommandRouter router = new CommandRouter(storage);

        System.out.println("Здравствуйте! Консольный сервис коротких ссылок.");
        System.out.println("Напишите help, чтобы увидеть команды.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String input = scanner.nextLine();
                Command command = CommandParser.parse(input);

                boolean shouldExit = router.handle(command);
                if (shouldExit) {
                    storage.save();
                    System.out.println("Выход выполнен. Данные сохранены.");
                    break;
                }
            }
        }
    }
}