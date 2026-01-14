package me.basmathy.shortlinks.service;

import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.util.UUID;

public final class ShortCodeGenerator {

    private static final char[] BASE62_ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private ShortCodeGenerator() {}

    private static String toBase62(byte[] bytes) {
        BigInteger value = new BigInteger(1, bytes);
        if (value.equals(BigInteger.ZERO)) return "0";

        BigInteger base = BigInteger.valueOf(62);
        StringBuilder sb = new StringBuilder();

        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divRem = value.divideAndRemainder(base);
            sb.append(BASE62_ALPHABET[divRem[1].intValue()]);
            value = divRem[0];
        }

        return sb.reverse().toString();
    }

    public static String getShort(String sourceUrl, UUID userId) {
        String input = sourceUrl + "|" + userId.toString().toLowerCase();
        byte[] sha = DigestUtils.sha256(input);

        String base62 = toBase62(sha);

        if (base62.length() >= 8) return base62.substring(0, 8);
        return "0".repeat(8 - base62.length()) + base62;
    }
}