package me.basmathy.shortlinks.config;

import java.nio.file.Path;

public final class StorageConfig {

    private StorageConfig() {}

    public static final Path STORAGE_FILE = Path.of("data/links.bin");

    /** Сколько живёт ссылка после создания (мс). */
    public static final long EXPIRATION_INTERVAL_MS = 86_400_000; // 24 часа

    /** Максимум открытий по одной короткой ссылке. */
    public static final int MAX_CLICKS = 20;
}
