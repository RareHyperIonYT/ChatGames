package dev.rarehyperion.chatgames.versioning;

import dev.rarehyperion.chatgames.platform.Platform;
import dev.rarehyperion.chatgames.platform.PlatformLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionChecker {

    private static final String PROJECT_SLUG = "chat-games";
    private static final String API_URL = "https://api.modrinth.com/v2/project/%s/version";
    private static final String PROJECT_URL = "https://modrinth.com/plugin/%s";

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "\\{[^}]*?\"version_number\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"version_type\"\\s*:\\s*\"([^\"]+)\"[^}]*?}"
    );

    public static void check(final Platform platform) {
        final PlatformLogger logger = platform.getLogger();
        logger.info("Checking for updates...");

        try {
            checkInternal(logger, platform);
        } catch (final IOException exception) {
            logger.error("Failed to check version.");
        }
    }

    private static void checkInternal(final PlatformLogger logger, final Platform platform) throws IOException {
        final URL url = new URL(String.format(API_URL, PROJECT_SLUG));
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "ChatGames-VersionCheck/1.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int status = connection.getResponseCode();
        if (status != 200) {
            throw new IOException("Modrinth API returned status '" + status + "'");
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final StringBuilder json = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        reader.close();
        connection.disconnect();

        final Matcher matcher = VERSION_PATTERN.matcher(json.toString());
        String latestVersion = null;

        while (matcher.find()) {
            final String versionNumber = matcher.group(1);
            final String versionType = matcher.group(2);

            if (!"release".equalsIgnoreCase(versionType)) continue;

            if (latestVersion == null || compareSemVer(versionNumber, latestVersion) > 0) {
                latestVersion = versionNumber;
            }
        }

        if (latestVersion == null) {
            logger.warn("No stable releases found on Modrinth.");
            return;
        }

        final String currentVersion = platform.pluginMeta().getVersion();

        if (compareSemVer(latestVersion, currentVersion) > 0) {
            logger.warn("Update available: " + currentVersion + " -> " + latestVersion);
            logger.warn("Download: " + String.format(PROJECT_URL, PROJECT_SLUG));
        } else {
            logger.info("You are running the latest version.");
        }
    }

    private static int compareSemVer(String a, String b) {
        String[] aParts = a.split("-", 2);
        String[] bParts = b.split("-", 2);

        String[] aNums = aParts[0].split("\\.");
        String[] bNums = bParts[0].split("\\.");

        int max = Math.max(aNums.length, bNums.length);
        for (int i = 0; i < max; i++) {
            int ai = i < aNums.length ? parseIntSafe(aNums[i]) : 0;
            int bi = i < bNums.length ? parseIntSafe(bNums[i]) : 0;
            if (ai != bi) return Integer.compare(ai, bi);
        }

        boolean aPre = aParts.length > 1;
        boolean bPre = bParts.length > 1;

        if (aPre && !bPre) return -1;
        if (!aPre && bPre) return 1;
        if (!aPre) return 0;

        return aParts[1].compareToIgnoreCase(bParts[1]);
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
