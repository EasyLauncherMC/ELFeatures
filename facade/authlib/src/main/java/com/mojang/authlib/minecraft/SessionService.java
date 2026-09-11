package com.mojang.authlib.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**
 * What {@code MinecraftSessionService} was renamed to in authlib 10, which arrived with 26.3 Snapshot 2.
 *
 * <p>Only the two members the skin mixins call: the facade exists so both generations compile against one
 * build, and the name a library goes under at runtime is never rewritten.
 */
public interface SessionService {

    Property getPackedTextures(GameProfile profile);

    MinecraftProfileTextures unpackTextures(Property property);

}
