package org.easylauncher.mods.elfeatures.activity;

public final class QuickPlayWorldHolder {

    private static final String QUICK_PLAY_SINGLEPLAYER_ARGUMENT = "--quickPlaySingleplayer";
    private static final String ENVIRONMENT_WORLD = System.getenv("ELFEATURES_QUICKPLAY_WORLD");

    private static String world = ENVIRONMENT_WORLD != null && !ENVIRONMENT_WORLD.isEmpty()
            ? ENVIRONMENT_WORLD
            : System.getProperty("elfeatures.quickplay.world");

    public static String[] fixWorldArgument(String[] args) {
        if (ENVIRONMENT_WORLD == null || ENVIRONMENT_WORLD.isEmpty())
            return args;

        for (int i = 0; i < args.length - 1; i++) {
            if (QUICK_PLAY_SINGLEPLAYER_ARGUMENT.equals(args[i])) {
                String[] fixed = args.clone();
                fixed[i + 1] = ENVIRONMENT_WORLD;
                return fixed;
            }
        }

        return args;
    }

    // handed out once: a world that fails to load drops the game
    // back to the main menu, which must not try it again
    public static String takeWorld() {
        String taken = world;
        world = null;

        return taken != null && !taken.isEmpty() ? taken : null;
    }

}
