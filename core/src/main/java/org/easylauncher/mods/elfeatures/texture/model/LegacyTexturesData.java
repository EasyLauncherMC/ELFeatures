package org.easylauncher.mods.elfeatures.texture.model;

import lombok.Data;
import org.easylauncher.mods.elfeatures.texture.model.LegacyProfileTexture.Type;

import java.util.Collections;
import java.util.Map;

@Data
public final class LegacyTexturesData implements TexturesData {

    public static final LegacyTexturesData EMPTY = new LegacyTexturesData(null, Collections.emptyMap());

    private final String propertyValue;
    private final Map<Type, LegacyProfileTexture> texturesMap;

}
