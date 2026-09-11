package org.easylauncher.mods.elfeatures.texture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import org.easylauncher.mods.elfeatures.ELFeaturesMod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * The game's session service with EasyX textures mixed into what {@code getTextures} answers.
 *
 * <p>A proxy rather than a subclass: the interface grew methods across authlib releases, and a proxy takes whichever
 * set the running one has.
 */
public final class EasyxSessionService implements InvocationHandler {

    private final MinecraftSessionService delegate;

    private EasyxSessionService(MinecraftSessionService delegate) {
        this.delegate = delegate;
    }

    public static MinecraftSessionService wrap(MinecraftSessionService delegate) {
        return (MinecraftSessionService) Proxy.newProxyInstance(
                MinecraftSessionService.class.getClassLoader(),
                new Class<?>[] { MinecraftSessionService.class },
                new EasyxSessionService(delegate)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;

        try {
            result = method.invoke(delegate, args);
        } catch (InvocationTargetException cause) {
            throw cause.getCause();
        }

        if (!"getTextures".equals(method.getName()) || args == null || args.length != 2)
            return result;

        return ELFeaturesMod.authlibEasyxTexturesProvider().loadTexturesMap(
                (GameProfile) args[0],
                (Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>) result
        );
    }

}
