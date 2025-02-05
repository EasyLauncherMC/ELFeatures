package org.easylauncher.mods.elfeatures.texture.model;

import lombok.Data;

import java.util.Map;

@Data
public final class LegacyProfileTexture {

    private String url;
    private Map<String, String> metadata;

    public enum Type {
        SKIN, CAPE
    }

}
