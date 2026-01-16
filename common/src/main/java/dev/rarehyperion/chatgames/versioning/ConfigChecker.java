package dev.rarehyperion.chatgames.versioning;

import dev.rarehyperion.chatgames.platform.Platform;
import dev.rarehyperion.chatgames.platform.PlatformLogger;

import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ConfigChecker {

    private static final Map<String, Integer> EXPECTED_VERSIONS = new HashMap<>();

    public static void check(final Platform platform) {
        String raw = platform.getConfigValue("VERSION", String.class, "UPDVC").trim();
        String decoded = ConfigChecker.tryDecode(raw);
        final PlatformLogger logger = platform.getLogger();

        if(!looksLikeVersionString(decoded)) {
            if(raw.equals("UPDVC")) {
                setVersion(platform);
                raw = platform.getConfigValue("VERSION", String.class, "UPDVC").trim();
                decoded = ConfigChecker.tryDecode(raw);
            } else {
                logger.warn("Config versioning has been tampered with... ignoring.");
                return;
            }
        }

        final Map<String, Integer> retrievedVersions = ConfigChecker.parseVersionString(decoded);
        final Set<String> outdated = new HashSet<>();

        for(final Map.Entry<String, Integer> entry : EXPECTED_VERSIONS.entrySet()) {
            final String file = entry.getKey();
            final int expected = entry.getValue();
            final int found = retrievedVersions.getOrDefault(file, -1);

            if(found == 0) {
                logger.warn("Config versioning has been tampered with... ignoring.");
                break;
            }

            if(found < expected) {
                outdated.add(file);
            }
        }

        if(!outdated.isEmpty()) {
            logger.warn("The following configurations are out-of-date: " + String.join(", ", outdated));
            logger.warn("To ensure that they function as expected, migrate them to their new schema.");
            logger.warn(String.format(String.format(VersionChecker.PROJECT_URL, VersionChecker.PROJECT_SLUG)));
            setVersion(platform);
        }
    }

    private static void setVersion(final Platform platform) {
        final String canonical = ConfigChecker.getVersionString();
        final String encoded = ConfigChecker.encode(canonical);
        platform.setConfigValue("VERSION", encoded);
        platform.saveConfig();
    }

    public static Map<String, Integer> parseVersionString(final String str) {
        Map<String, Integer> map = new HashMap<>();
        if (str == null || str.isEmpty()) return map;

        String[] parts = str.split("[,;]");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            String[] kv;

            if (part.contains("=")) {
                kv = part.split("=", 2);
            } else if (part.contains(":")) {
                kv = part.split(":", 2);
            } else {
                continue;
            }

            String key = kv[0].trim();
            String val = kv[1].trim();

            if (key.isEmpty()) continue;

            try {
                int version = Integer.parseInt(val);
                if (version < 0) throw new NumberFormatException("negative");
                map.put(key, version);
            } catch (final NumberFormatException exception) { /* no-op */ }
        }
        return map;
    }

    private static String tryDecode(final String raw) {
        if(raw == null || raw.isEmpty()) return null;

        try {
            final byte[] bytes = Base64.getDecoder().decode(raw);
            final String string = new String(bytes, StandardCharsets.UTF_8).trim();
            return string.isEmpty() ? null : string;
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    private static String encode(final String str) {
        if (str == null) return "";
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean looksLikeVersionString(final String str) {
        if(str == null) return false;
        return str.contains("=") && (str.contains(",") || str.contains(";") || str.contains(":"));
    }


    private static String getVersionString() {
        final List<String> keys = new ArrayList<>(ConfigChecker.EXPECTED_VERSIONS.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String k : keys) {
            if (!first) sb.append(",");
            sb.append(k).append("=").append(ConfigChecker.EXPECTED_VERSIONS.get(k));
            first = false;
        }
        return sb.toString();
    }

    static {
        EXPECTED_VERSIONS.put("config.yml", 1);
        EXPECTED_VERSIONS.put("math.yml", 1);
        EXPECTED_VERSIONS.put("multiple-choice.yml", 1);
        EXPECTED_VERSIONS.put("reaction.yml", 1);
        EXPECTED_VERSIONS.put("trivia.yml", 1);
        EXPECTED_VERSIONS.put("unscramble.yml", 1);
    }

}
