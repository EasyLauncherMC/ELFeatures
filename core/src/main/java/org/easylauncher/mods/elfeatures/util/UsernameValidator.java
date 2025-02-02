package org.easylauncher.mods.elfeatures.util;

import java.util.regex.Pattern;

public final class UsernameValidator {

    private static final Pattern VALID_USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty())
            return false;

        return VALID_USERNAME_REGEX.matcher(username).matches();
    }

}
