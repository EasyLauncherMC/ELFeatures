package org.easylauncher.mods.elfeatures.activity;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class ActivityJournalWriter {

    private static final String JOURNAL_PATH = System.getProperty("elfeatures.activity.journal");

    // nulls are left out as Gson does by default: an entry carries only what the game knew
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    // a connection with no server list entry behind it, as --server makes, kept for the join it leads to
    private static String connectingHost;
    private static Integer connectingPort;

    // a join written and not closed by an idle entry yet; every hook runs on the client thread
    private static boolean joined;

    public static void connecting(String host, int port) {
        connectingHost = host;
        connectingPort = port;
    }

    // the address as typed in the server list entry, or null when the join had no entry
    public static void multiplayer(String address, String serverName, String gamemode) {
        String host = connectingHost;
        Integer port = connectingPort;
        connectingHost = null;
        connectingPort = null;

        if (address != null) {
            // split as the game does it, before any SRV lookup:
            // "[v6]:port", "host:port", a bare v6 has no port
            String portText = null;
            int bracket = address.indexOf(']');
            int colon = address.indexOf(':');

            if (address.startsWith("[") && bracket > 0) {
                host = address.substring(1, bracket);
                portText = address.startsWith(":", bracket + 1) ? address.substring(bracket + 2) : null;
            } else if (colon >= 0 && colon == address.lastIndexOf(':')) {
                host = address.substring(0, colon);
                portText = address.substring(colon + 1);
            } else {
                host = address;
            }

            port = parsePort(portText);
        }

        // a join nothing announced, as Realms makes
        if (host == null) return;

        joined = true;
        write(new Entry("multiplayer", null, null, host, port, serverName, gamemode, nowMillis()));
    }

    public static void singleplayer(String directoryName, String levelName, String gamemode) {
        joined = true;
        write(new Entry("singleplayer", directoryName, levelName, null, null, null, gamemode, nowMillis()));
    }

    // the player is in no world: the join written last is over
    public static void idle() {
        if (!joined) return;

        joined = false;
        write(new Entry("idle", null, null, null, null, null, null, nowMillis()));
    }

    private static void write(Entry entry) {
        if (JOURNAL_PATH == null)
            return;

        // one entry per line: the launcher tails the file and takes only the lines a newline has closed
        byte[] line = (GSON.toJson(entry) + '\n').getBytes(StandardCharsets.UTF_8);

        try {
            Path path = Paths.get(JOURNAL_PATH);
            if (path.getParent() != null)
                Files.createDirectories(path.getParent());

            // opened and closed per entry, so the launcher is free to delete the file once the session is over
            Files.write(path, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception cause) {
            ELFeaturesMod.mod().log("Activity not written to '%s': %s", JOURNAL_PATH, cause);
        }
    }

    private static Integer parsePort(String text) {
        try {
            return text != null ? Integer.valueOf(text.trim()) : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static long nowMillis() {
        return System.currentTimeMillis();
    }

    @AllArgsConstructor
    private static final class Entry {

        private final String type;
        private final String directoryName;
        private final String levelName;
        private final String host;
        private final Integer port;
        private final String serverName;
        private final String gamemode;
        private final long timestamp;

    }

}
