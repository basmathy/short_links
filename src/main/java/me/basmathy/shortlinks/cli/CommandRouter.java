package me.basmathy.shortlinks.cli;

import me.basmathy.shortlinks.config.StorageConfig;
import me.basmathy.shortlinks.data.StoredLink;
import me.basmathy.shortlinks.data.Storage;
import me.basmathy.shortlinks.service.ShortCodeGenerator;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.regex.Pattern;

public final class CommandRouter {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private static final Pattern LONG_URL_PATTERN = Pattern.compile(
            "^https?://(www\\.)?\\w+(\\.\\w+)+.*$"
    );

    private static final Pattern SHORT_URL_PATTERN = Pattern.compile(
            "^clck\\.ru/[a-zA-Z0-9]{8}$"
    );

    private static final String SHORT_PREFIX = "clck.ru/";

    private UUID currentUserId;
    private final Storage<String, StoredLink> storage;

    public CommandRouter(Storage<String, StoredLink> storage) {
        this.storage = storage;
    }

    /**
     * @return true если нужно завершить программу, иначе false
     */
    public boolean handle(Command command) {
        return switch (command.type()) {
            case HELP -> {
                printHelp();
                yield false;
            }
            case EXIT -> true;
            case LOGIN -> {
                currentUserId = login(command.argument());
                yield false;
            }
            case LOGOUT -> {
                currentUserId = logout();
                yield false;
            }
            case SHORTEN -> {
                shorten(command.argument());
                yield false;
            }
            case OPEN -> {
                openShort(command.argument());
                yield false;
            }
            case UNKNOWN -> {
                printUnknown();
                yield false;
            }
        };
    }

    public UUID getCurrentUserId() {
        return currentUserId;
    }

    // ====== Handlers ======

    private void printHelp() {
        System.out.println("Команды:");
        System.out.println("help               — показать справку");
        System.out.println("exit | quit        — выйти (данные сохранятся)");
        System.out.println("login <uuid>       — войти по UUID");
        System.out.println("logout             — выйти из текущей учётки");
        System.out.println("http...            — сократить ссылку");
        System.out.println("clck.ru/XXXXXXXX    — открыть короткую ссылку (только владельцу)");
    }

    private void printUnknown() {
        System.out.println("Команда не распознана. Введите help, чтобы посмотреть варианты.");
    }

    private UUID registerUser() {
        UUID newId = UUID.randomUUID();
        System.out.println("Для работы нужен пользователь. Создал новый UUID:");
        System.out.println(newId);
        System.out.println("Сохраните его, чтобы потом входить через login.");
        return newId;
    }

    private UUID login(String uuidText) {
        if (uuidText == null || uuidText.isBlank()) {
            System.out.println("Формат команды: login <uuid>");
            return null;
        }
        if (!UUID_PATTERN.matcher(uuidText).matches()) {
            System.out.println("UUID выглядит неверно. Проверьте символы и дефисы.");
            return null;
        }
        System.out.println("Вы вошли в систему.");
        return UUID.fromString(uuidText);
    }

    private UUID logout() {
        if (currentUserId == null) {
            System.out.println("Сейчас никто не вошёл.");
            return null;
        }
        System.out.println("Вы вышли из учётной записи.");
        return null;
    }

    private void shorten(String longUrl) {
        if (longUrl == null || longUrl.isBlank()) {
            System.out.println("Пустая ссылка — сокращать нечего.");
            return;
        }
        if (!LONG_URL_PATTERN.matcher(longUrl).matches()) {
            System.out.println("Не похоже на валидный URL. Нужен http:// или https://");
            return;
        }

        if (currentUserId == null) {
            currentUserId = registerUser();
        }

        String code = ShortCodeGenerator.getShort(longUrl, currentUserId);

        StoredLink record = new StoredLink(
                currentUserId,
                longUrl,
                System.currentTimeMillis() + StorageConfig.EXPIRATION_INTERVAL_MS,
                StorageConfig.MAX_CLICKS
        );

        storage.put(code, record);

        System.out.println("Ссылка готова и сохранена:");
        System.out.println(SHORT_PREFIX + code);
    }

    private void openShort(String shortUrl) {
        if (currentUserId == null) {
            System.out.println("Сначала войдите: login <uuid>. Подсказка: help.");
            return;
        }
        if (shortUrl == null || shortUrl.isBlank()) {
            System.out.println("Пустая короткая ссылка.");
            return;
        }
        if (!SHORT_URL_PATTERN.matcher(shortUrl).matches()) {
            System.out.println("Формат короткой ссылки неверный. Ожидаю clck.ru/XXXXXXXX");
            return;
        }

        String code = shortUrl.substring(SHORT_PREFIX.length());

        storage.get(code).ifPresentOrElse(record -> {
            long now = System.currentTimeMillis();

            if (record.isClickLimitReached()) {
                storage.remove(code);
                System.out.println("Ссылка отключена: лимит открытий исчерпан.");
                return;
            }
            if (record.isExpired(now)) {
                storage.remove(code);
                System.out.println("Ссылка отключена: срок действия истёк.");
                return;
            }

            String original = record.getOriginalUrlFor(currentUserId);
            if (original == null) {
                System.out.println("Доступ запрещён: ссылка принадлежит другому пользователю.");
                return;
            }

            record.addClick();
            if (openInBrowser(original)) {
                System.out.println("Открыл в браузере.");
            } else {
                System.out.println("Не удалось открыть браузер. Вот исходная ссылка:");
                System.out.println(original);
            }
        }, () -> System.out.println("В хранилище нет такой короткой ссылки."));
    }

    private boolean openInBrowser(String url) {
        if (!Desktop.isDesktopSupported()) return false;
        try {
            Desktop.getDesktop().browse(new URI(url));
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }
}