package me.basmathy.shortlinks.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public final class StoredLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID ownerId;
    private final String originalUrl;
    private final long expiresAt;
    private final int maxClicks;

    private int clicks;

    public StoredLink(UUID ownerId, String originalUrl, long expiresAt, int maxClicks) {
        this.ownerId = ownerId;
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
        this.maxClicks = maxClicks;
        this.clicks = 0;
    }

    public boolean isExpired(long now) {
        return now >= expiresAt;
    }

    public boolean isClickLimitReached() {
        return clicks >= maxClicks;
    }

    /** Возвращает оригинальный URL только владельцу, иначе null. */
    public String getOriginalUrlFor(UUID requesterId) {
        if (requesterId == null) return null;
        return ownerId.equals(requesterId) ? originalUrl : null;
    }

    public void addClick() {
        clicks++;
    }
}