package me.basmathy.shortlinks.data;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Storage<K extends Serializable, V extends Serializable> {

    private final Path filePath;
    private final Map<K, V> data = new HashMap<>();

    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    public void put(K key, V value) {
        data.put(key, value);
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(data.get(key));
    }

    public void remove(K key) {
        data.remove(key);
    }

    public boolean containsKey(K key) {
        return data.containsKey(key);
    }

    public int size() {
        return data.size();
    }

    public void save() {
        try {
            Path parent = filePath.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);

            Path tmp = filePath.resolveSibling(filePath.getFileName().toString() + ".tmp");

            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tmp))) {
                out.writeObject(data);
            }

            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить хранилище в файл: " + filePath, e);
        }
    }

    public void load() {
        if (!Files.exists(filePath)) return;

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(filePath))) {
            Object obj = in.readObject();

            if (obj instanceof Map<?, ?> loaded) {
                data.clear();
                @SuppressWarnings("unchecked")
                Map<K, V> casted = (Map<K, V>) loaded;
                data.putAll(casted);
            }
        } catch (Exception e) {
            // Мягкая деградация: если файл битый/несовместимый — начинаем с пустого хранилища.
            data.clear();
        }
    }
}