package org.easylauncher.mods.elfeatures.activity;

import org.easylauncher.mods.elfeatures.ELFeaturesMod;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.easylauncher.mods.elfeatures.loader.naming.MixinRemapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Quick play and the activity journal on the vanilla game from 1.6 to the 1.14 snapshots, called from the mixins in
 * {@code mixin.activity} and {@code mixin.quickplay}.
 *
 * <p>This range has nothing to gate mixins by version with, {@code version.json} only comes in 18w47b, so every call into
 * the game goes by reflection instead: a member some version lacks fails here and lands in the log, rather than as a
 * {@code NoSuchMethodError} in the middle of a tick. The members are named in calamus intermediary, which keeps one id
 * for every one of them over the whole range, and put into the running game's names by the loader's remapper.
 */
public final class LegacyActivityHooks {

    private static final String TITLE_SCREEN = "net/minecraft/unmapped/C_95462098";

    // the client, kept from its first tick for the joins that come after it
    private static Object minecraft;
    private static boolean titleScreenSeen;

    // the world field, looked up on the first tick, and whether that failed
    private static Field worldField;
    private static boolean worldCheckFailed;

    /**
     * At the head of every client tick: the idle entry once the world is gone, and quick play.
     *
     * <p>The world is dropped whichever way the player leaves it, the pause menu, a kick or the death screen, so the
     * first tick without one closes the join written last. The writer drops the entry while no join is open, so the
     * ticks in the menus write nothing.
     *
     * <p>The world from {@code elfeatures.quickplay.world} is opened on the first tick with the title screen open and no
     * overlay on top. Up to 1.13 the title screen is opened only once resources are loaded; from 19w08a on it is opened
     * under the reload overlay and ticked there, hence the check. The world is handed out once, so a world that fails to
     * load drops the player back into the main menu rather than into a loop.
     */
    public static void onClientTick(Object client) {
        minecraft = client;
        checkIdle(client);

        if (titleScreenSeen)
            return;

        try {
            // screen
            if (!isTitleScreen(get(client, "f_70816363")))
                return;

            // getOverlay, 19w08a and newer
            Method overlay = find(client.getClass(), "m_45915379", 0);
            if (overlay != null && overlay.invoke(client) != null)
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
     * Once the login packet is handled, which happens on the client thread: the journal entry for the world or server
     * joined. A running integrated server tells a singleplayer world from a server; a server is named by its list
     * entry, or, with none, by the address the connecting screen kept.
     */
    public static void onJoinGame() {
        Object client = minecraft;
        if (client == null)
            return;

        try {
            // interactionManager.gameMode.getKey()
            Object gameMode = get(get(client, "f_17639899"), "f_14768439");
            String gamemode = gameMode != null ? (String) call(gameMode, "m_51006294") : null;

            // getServer
            Object server = call(client, "m_37046522");
            if (server != null) {
                // getWorldSaveName is the directory, getWorldName the name the world was opened with
                ActivityJournalWriter.singleplayer((String) call(server, "m_70179823"), (String) call(server, "m_65157856"), gamemode);
                return;
            }

            // currentServerEntry: ip as typed, name
            Object entry = get(client, "f_32571834");
            String address = entry != null ? (String) get(entry, "f_01631523") : null;
            String serverName = entry != null ? (String) get(entry, "f_20279990") : null;
            ActivityJournalWriter.multiplayer(address, serverName, gamemode);
        } catch (Throwable cause) {
            ELFeaturesMod.mod().log("Activity not recorded: %s", cause);
        }
    }

    private static void checkIdle(Object client) {
        if (worldCheckFailed)
            return;

        try {
            // world
            if (worldField == null)
                worldField = field(client, "f_26407825");

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
        // getWorldStorageSource().getAll(): the world list itself. Reading one world's data alone has no single way
        // over the range, the storage source turns from an interface into a class in the 1.14 snapshots
        Object summary = null;
        for (Object candidate : (List<?>) call(call(client, "m_98129977"), "m_45201496"))
            // getName is the directory
            if (world.equals(call(candidate, "m_07607595")))
                summary = candidate;

        if (summary == null) {
            ELFeaturesMod.mod().log("Quick play world '%s' not found, staying in the main menu", world);
            return;
        }

        // startGame(String, String, WorldSettings) with no settings, as the world list calls it;
        // the name is written into level.dat over the one there, so it has to be that one, getDisplayName
        call(client, "m_16362034", world, call(summary, "m_59791697"), null);
    }

    private static boolean isTitleScreen(Object screen) {
        String titleScreen = className(TITLE_SCREEN);
        for (Class<?> type = screen != null ? screen.getClass() : null; type != null; type = type.getSuperclass())
            if (titleScreen.equals(type.getName()))
                return true;

        return false;
    }

    private static Object get(Object target, String intermediary) throws ReflectiveOperationException {
        return field(target, intermediary).get(target);
    }

    private static Field field(Object target, String intermediary) throws NoSuchFieldException {
        String name = memberName(intermediary);

        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }

        throw new NoSuchFieldException(target.getClass().getName() + '.' + intermediary);
    }

    private static Object call(Object target, String intermediary, Object... arguments) throws ReflectiveOperationException {
        Method method = find(target.getClass(), intermediary, arguments.length);
        if (method == null)
            throw new NoSuchMethodException(target.getClass().getName() + '.' + intermediary);

        return method.invoke(target, arguments);
    }

    // obfuscated method names repeat within a class, told apart here by how many parameters they take
    private static Method find(Class<?> owner, String intermediary, int parameterCount) {
        String name = memberName(intermediary);

        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            Method[] methods;
            try {
                methods = type.getDeclaredMethods();
            } catch (Throwable ignored) {
                // a mixin method whose signature names a class this version does not have
                continue;
            }

            for (Method method : methods) {
                if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

    private static String memberName(String intermediary) {
        MixinRemapper remapper = ELFeaturesMixinBootstrap.getMixinRemapper();
        return remapper != null ? remapper.mapMethodName(null, intermediary, null) : intermediary;
    }

    private static String className(String intermediary) {
        MixinRemapper remapper = ELFeaturesMixinBootstrap.getMixinRemapper();
        return (remapper != null ? remapper.map(intermediary) : intermediary).replace('/', '.');
    }

}
