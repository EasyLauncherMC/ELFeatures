package org.easylauncher.mods.elfeatures.activity;

public final class QuickPlayWorldHolder {

    private static String world = System.getProperty("elfeatures.quickplay.world");

    // handed out once: a world that fails to load drops the game
    // back to the main menu, which must not try it again
    public static String takeWorld() {
        String taken = world;
        world = null;

        return taken != null && !taken.isEmpty() ? taken : null;
    }

}
