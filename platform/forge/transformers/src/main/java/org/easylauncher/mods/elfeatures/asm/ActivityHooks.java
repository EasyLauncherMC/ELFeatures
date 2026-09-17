package org.easylauncher.mods.elfeatures.asm;

import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.activity.ActivityJournalWriter;
import org.easylauncher.mods.elfeatures.activity.QuickPlayWorldHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Quick play and the activity journal on Forge 1.7.10 – 1.16.5, called from the bytecode the transformers put into the
 * game.
 *
 * <p>The patches hand over nothing but plain objects, so the patched bytecode names no game member whatever state the
 * class is in when it is patched. The game is reached from here by reflection on SRG names instead: the production
 * client keeps them from 1.7.10 to 1.16.5, and every one used below has the same id over the whole range. One class
 * serves both ways the patches get in, the launch wrapper's Java transformers and the ModLauncher script.
 */
public final class ActivityHooks {

    private static final String[] TITLE_SCREENS = {
            "net.minecraft.client.gui.GuiMainMenu",             // up to 1.13.2
            "net.minecraft.client.gui.screen.MainMenuScreen",   // 1.14 and newer
    };

    // the client, kept from its first tick for the joins that come after it
    private static Object minecraft;
    private static boolean titleScreenSeen;

    // the world field, looked up on the first tick, and whether that failed
    private static Field worldField;
    private static boolean worldCheckFailed;

    /**
     * At the head of {@code Minecraft.runTick}: the idle entry once the world is gone, and quick play.
     *
     * <p>The world is dropped whichever way the player leaves it, the pause menu, a kick or the death screen, so the
     * first tick without one closes the join written last. The writer drops the entry while no join is open, so the
     * ticks in the menus write nothing.
     *
     * <p>The world from {@code elfeatures.quickplay.world} is opened on the first tick with the title screen open and no
     * overlay on top. From 1.14 on the title screen is opened under the resource reload overlay and ticked there, hence
     * the check; before that it is opened only once resources are loaded. The world is handed out once, so a world that
     * fails to load drops the player back into the main menu rather than into a loop.
     */
    public static void onClientTick(Object client) {
        minecraft = client;
        checkIdle(client);

        if (titleScreenSeen)
            return;

        try {
            // currentScreen
            if (!isTitleScreen(get(client, "field_71462_r")))
                return;

            // getLoadingGui, 1.14 and newer
            Method loadingGui = find(client.getClass(), "func_213250_au");
            if (loadingGui != null && loadingGui.invoke(client) != null)
                return;

            titleScreenSeen = true;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world != null) startWorld(client, world);
        } catch (Throwable cause) {
            titleScreenSeen = true;
            ELFeaturesMod.mod().log("Quick play failed: %s", cause);
        }
    }

    /**
     * At the end of {@code handleJoinGame}, which runs on the client thread: the journal entry for the world or server
     * joined. A running integrated server tells a singleplayer world from a server; a server is named by its list
     * entry, or, with none, by the address the connecting screen kept.
     */
    public static void onJoinGame() {
        Object client = minecraft;
        if (client == null) client = clientSingleton();
        if (client == null) return;

        String gamemode = gamemodeOf(client);

        try {
            // getIntegratedServer
            Object server = call(client, "func_71401_C");
            if (server != null) {
                ActivityJournalWriter.singleplayer(directoryName(server), levelName(server), gamemode);
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            Object entry = currentServerEntry(client);
            String address = entry != null ? (String) get(entry, "field_78845_b") : null;
            String serverName = entry != null ? (String) get(entry, "field_78847_a") : null;
            ActivityJournalWriter.multiplayer(address, serverName, gamemode);
        } catch (Throwable cause) {
            ELFeaturesMod.mod().log("Activity not recorded: %s", cause);
        }
    }

    /**
     * The address of a server-list join, kept for the journal when {@code getCurrentServerData} is still empty
     * at {@code handleJoinGame}. The connecting screen's {@code ServerData} constructor is the one that path uses.
     */
    public static void connectingFromServerData(Object entry) {
        try {
            String ip = (String) get(entry, "field_78845_b");
            if (ip == null)
                return;

            int port = 25565;
            int colon = ip.lastIndexOf(':');
            if (colon > 0 && ip.indexOf(':') == colon) {
                try {
                    port = Integer.parseInt(ip.substring(colon + 1).trim());
                    ip = ip.substring(0, colon);
                } catch (NumberFormatException ignored) {
                }
            }

            ActivityJournalWriter.connecting(ip, port);
        } catch (Throwable ignored) {
        }
    }

    private static void checkIdle(Object client) {
        if (worldCheckFailed)
            return;

        try {
            // world
            if (worldField == null)
                worldField = field(client, "field_71441_e");

            if (worldField.get(client) == null) {
                ActivityJournalWriter.idle();
            }
        } catch (Throwable cause) {
            // a field that is not there stays missing, so it is not looked for again on every tick
            worldCheckFailed = true;
            ELFeaturesMod.mod().log("Idle not recorded: %s", cause);
        }
    }

    private static void startWorld(Object client, String world) throws ReflectiveOperationException {
        // getSaveLoader
        Object saveLoader = call(client, "func_71359_d");

        // loadWorld(String), 1.16: the directory alone, everything else is read from the world
        Method loadWorld = find(client.getClass(), "func_238191_a_");
        if (loadWorld != null) {
            // canLoadWorld
            if (!(Boolean) call(saveLoader, "func_90033_f", world)) {
                worldNotFound(world);
                return;
            }

            loadWorld.invoke(client, world);
            return;
        }

        // getWorldInfo: no level.dat, no world
        Object worldInfo = call(saveLoader, "func_75803_c", world);
        if (worldInfo == null) {
            worldNotFound(world);
            return;
        }

        // launchIntegratedServer(String, String, WorldSettings) with no settings, as the world list calls it;
        // the name is written into level.dat over the one there, so it has to be that one, getWorldName
        call(client, "func_71371_a", world, call(worldInfo, "func_76065_j"), null);
    }

    private static String directoryName(Object server) throws ReflectiveOperationException {
        // getFolderName, up to 1.15.2
        if (find(server.getClass(), "func_71270_I") != null)
            return (String) call(server, "func_71270_I");

        // anvilConverterForAnvilFile.getSaveName(), 1.16
        return (String) call(get(server, "field_71310_m"), "func_237282_a_");
    }

    private static String levelName(Object server) throws ReflectiveOperationException {
        // getWorldName, up to 1.15.2: the name the world was opened with, the one put into level.dat
        if (find(server.getClass(), "func_71221_J") != null)
            return (String) call(server, "func_71221_J");

        // getServerConfiguration().getWorldName(), 1.16
        return (String) call(call(server, "func_240793_aU_"), "func_76065_j");
    }

    private static void worldNotFound(String world) {
        ELFeaturesMod.mod().log("Quick play world '%s' not found, staying in the main menu", world);
    }

    // getMinecraft: a join can land before the first tick has stored the client
    private static Object clientSingleton() {
        try {
            Method method = find(Class.forName("net.minecraft.client.Minecraft"), "func_71410_x");
            return method != null ? method.invoke(null) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String gamemodeOf(Object client) {
        try {
            // playerController.currentGameType.getName()
            Object gameType = get(get(client, "field_71442_b"), "field_78779_k");
            return gameType != null ? (String) call(gameType, "func_77149_b") : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object currentServerEntry(Object client) throws ReflectiveOperationException {
        Method getter = find(client.getClass(), "func_147104_D");
        if (getter != null)
            return getter.invoke(client);

        try {
            return get(client, "field_71422_O");
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static boolean isTitleScreen(Object screen) {
        for (Class<?> type = screen != null ? screen.getClass() : null; type != null; type = type.getSuperclass())
            for (String titleScreen : TITLE_SCREENS)
                if (titleScreen.equals(type.getName()))
                    return true;

        return false;
    }

    private static Object get(Object target, String name) throws ReflectiveOperationException {
        return field(target, name).get(target);
    }

    private static Field field(Object target, String name) throws NoSuchFieldException {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }

        throw new NoSuchFieldException(target.getClass().getName() + '.' + name);
    }

    private static Object call(Object target, String name, Object... arguments) throws ReflectiveOperationException {
        Method method = find(target.getClass(), name);
        if (method == null)
            throw new NoSuchMethodException(target.getClass().getName() + '.' + name);

        return method.invoke(target, arguments);
    }

    // an SRG name is one method, so the name alone is enough to tell
    private static Method find(Class<?> owner, String name) {
        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().equals(name)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

}
