package org.easylauncher.mods.elfeatures.texture.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProfileTexture.Type;

import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class LegacyTexturesPayload {

    private UUID profileId;
    private String profileName;
    private Map<Type, LegacyProfileTexture> textures;

}
