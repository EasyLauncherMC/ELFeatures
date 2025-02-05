package org.easylauncher.mods.elfeatures.texture.model;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public final class LegacyTexturesResponse {

    private UUID id;
    private String name;
    private List<LegacyProperty> properties;

}
