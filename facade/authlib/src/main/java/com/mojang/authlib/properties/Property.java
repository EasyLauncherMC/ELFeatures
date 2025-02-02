package com.mojang.authlib.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.security.PublicKey;

@Getter @Accessors(fluent = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class Property {

    private final String name;
    private final String value;
    private String signature;

    public boolean hasSignature() {
        return signature != null;
    }

    public boolean isSignatureValid(PublicKey publicKey) {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

}
