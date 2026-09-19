package org.easylauncher.mods.elfeatures.activity;

import lombok.CustomLog;
import org.easylauncher.mods.elfeatures.loader.ELFeaturesMixinBootstrap;
import org.easylauncher.mods.elfeatures.loader.naming.MixinRemapper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;

/**
 * Quick play and the activity journal on the vanilla game from 1.0 to the 1.14 snapshots, called from the mixins in
 * {@code mixin.activity} and {@code mixin.quickplay}.
 *
 * <p>This range has nothing to gate mixins by version with, {@code version.json} only comes in 18w47b, so every call into
 * the game goes by reflection instead: a member some version lacks fails here and lands in the log, rather than as a
 * {@code NoSuchMethodError} in the middle of a tick. The members are named in calamus intermediary, which keeps one id
 * for every one of them over the whole range, and put into the running game's names by the loader's remapper.
 */
@CustomLog
public final class LegacyActivityHooks {

    private static final String TITLE_SCREEN = "net/minecraft/unmapped/C_95462098";

    // what the world list up to 1.2.5 sets the client up with before it opens a world, by the world's mode
    private static final String SURVIVAL_INTERACTION_MANAGER = "net/minecraft/unmapped/C_54765678";
    private static final String CREATIVE_INTERACTION_MANAGER = "net/minecraft/unmapped/C_83928382";

    // the client, kept from its first tick for the joins that come after it
    private static Object minecraft;
    private static boolean titleScreenSeen;

    // the world field, looked up on the first tick, and whether that failed
    private static Field worldField;
    private static boolean worldCheckFailed;

    // up to 1.2.5 an old world is converted and then opened by a nested call, and both calls end in onStartGame
    private static WeakReference<Object> journaledWorld = new WeakReference<>(null);

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
            Method overlay = find(client.getClass(), "m_45915379");
            if (overlay != null && overlay.invoke(client) != null)
                return;

            titleScreenSeen = true;

            String world = QuickPlayWorldHolder.takeWorld();
            if (world != null) startWorld(client, world);
        } catch (Throwable cause) {
            titleScreenSeen = true;
            log.warn("Quick play failed", cause);
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
            String gamemode = gamemodeOf(client);

            // getServer, 1.3 and newer; up to 1.2.5 only a server sends the login packet
            Object server = find(client.getClass(), "m_37046522") != null ? call(client, "m_37046522") : null;
            if (server != null) {
                // getWorldSaveName is the directory, getWorldName the name the world was opened with
                ActivityJournalWriter.singleplayer((String) call(server, "m_70179823"), (String) call(server, "m_65157856"), gamemode);
                return;
            }

            // currentServerEntry, 1.3 and newer: ip as typed, name
            Object entry = null;
            try {
                entry = get(client, "f_32571834");
            } catch (NoSuchFieldException ignored) {
                // up to 1.2.5 the server is known only by the address the connecting screen kept
            }

            String address = entry != null ? (String) get(entry, "f_01631523") : null;
            String serverName = entry != null ? (String) get(entry, "f_20279990") : null;
            ActivityJournalWriter.multiplayer(address, serverName, gamemode);
        } catch (Throwable cause) {
            log.warn("Activity not recorded", cause);
        }
    }

    /**
     * Once a world of one's own is opened, up to 1.2.5: the journal entry for it. The world is loaded right in the
     * client there, and no login packet follows; from 1.3 on one does, from the integrated server.
     */
    public static void onStartGame(Object client, String directoryName) {
        try {
            // getServer, 1.3 and newer
            if (find(client.getClass(), "m_37046522") != null)
                return;

            // world, still empty when the open was handed over to the conversion of an old world
            Object world = get(client, "f_26407825");
            if (world == null || world == journaledWorld.get())
                return;

            journaledWorld = new WeakReference<>(world);

            // getData().getName(): the name the world was opened with, put into its data over the one there
            String levelName = (String) call(call(world, "m_67440400"), "m_90700428");
            ActivityJournalWriter.singleplayer(directoryName, levelName, gamemodeOf(client));
        } catch (Throwable cause) {
            log.warn("Activity not recorded", cause);
        }
    }

    private static String gamemodeOf(Object client) throws ReflectiveOperationException {
        // interactionManager
        Object interactionManager = get(client, "f_17639899");

        try {
            // gameMode.getKey(), 1.3 and newer
            Object gameMode = get(interactionManager, "f_14768439");
            return gameMode != null ? (String) call(gameMode, "m_51006294") : null;
        } catch (NoSuchFieldException ignored) {
            // world.getData().defaultGameMode up to 1.2.5: a number, and survival or creative, nothing else
            int gameMode = (Integer) get(call(get(client, "f_26407825"), "m_67440400"), "f_26217704");
            return gameMode == 1 ? "creative" : "survival";
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
            log.warn("Idle not recorded", cause);
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
            log.warn("Quick play world '{}' not found, staying in the main menu", world);
            return;
        }

        // getServer, 1.3 and newer; up to 1.2.5 the world is opened right in the client, which is readied for it first
        boolean integratedServer = find(client.getClass(), "m_37046522") != null;
        if (!integratedServer)
            setUpInteractionManager(client, summary);

        // startGame(String, String, WorldSettings) with no settings, as the world list calls it;
        // the name is written into level.dat over the one there, so it has to be that one, getDisplayName
        call(client, "m_16362034", world, call(summary, "m_59791697"), null);

        // openScreen(null), with which the world list closes itself up to 1.2.5, the mouse grabbed along the way;
        // a null fits every overload, so the one taking a screen is told by its parameter
        if (!integratedServer) {
            Class<?> screenType = field(client, "f_70816363").getType();
            Method openScreen = findMatching(client.getClass(), "m_52715402",
                    method -> method.getParameterCount() == 1 && method.getParameterTypes()[0] == screenType);

            if (openScreen == null)
                throw new NoSuchMethodException(client.getClass().getName() + ".m_52715402");

            openScreen.invoke(client, (Object) null);
        }
    }

    // interactionManager, survival or creative by getGameMode
    private static void setUpInteractionManager(Object client, Object summary) throws ReflectiveOperationException {
        String type = (Integer) call(summary, "m_12400919") == 0
                ? SURVIVAL_INTERACTION_MANAGER
                : CREATIVE_INTERACTION_MANAGER;

        // the client is a subclass the launcher made, and the managers take the Minecraft class itself
        Field interactionManager = field(client, "f_17639899");
        Class<?> managerType = Class.forName(className(type), true, client.getClass().getClassLoader());
        Object manager = managerType.getConstructor(interactionManager.getDeclaringClass()).newInstance(client);
        interactionManager.set(client, manager);
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
        Method method = find(target.getClass(), intermediary, arguments);
        if (method == null)
            throw new NoSuchMethodException(target.getClass().getName() + '.' + intermediary);

        return method.invoke(target, arguments);
    }

    // obfuscated method names repeat within a class, so a method is told apart by the arguments it takes
    private static Method find(Class<?> owner, String intermediary, Object... arguments) {
        return findMatching(owner, intermediary, method -> accepts(method, arguments));
    }

    /**
     * Statics are left out: up to 1.2.5 {@code startGame} called with no settings fits {@code startMainThread} too,
     * which starts a second client.
     */
    private static Method findMatching(Class<?> owner, String intermediary, Predicate<Method> fits) {
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
                if (method.getName().equals(name) && !Modifier.isStatic(method.getModifiers()) && fits.test(method)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

    // no argument here is ever a primitive, so a primitive parameter takes none
    private static boolean accepts(Method method, Object[] arguments) {
        Class<?>[] parameters = method.getParameterTypes();
        if (parameters.length != arguments.length)
            return false;

        for (int i = 0; i < parameters.length; i++)
            if (arguments[i] != null ? !parameters[i].isInstance(arguments[i]) : parameters[i].isPrimitive())
                return false;

        return true;
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
