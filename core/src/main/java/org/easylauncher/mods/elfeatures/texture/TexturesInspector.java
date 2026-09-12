package org.easylauncher.mods.elfeatures.texture;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;

public final class TexturesInspector {

    private static final String EASYX_DOMAINS_SUFFIX = ".easyxcdn.net";
    private static final int[] ALLOWED_SCALE_FACTORS = new int[] { 1, 2, 4, 8, 16 };

    public static BufferedImage passValidTextureImage(BufferedImage image) {
        return image != null && computeTextureScale(image) != 0 ? image : null;
    }

    public static BufferedImage toLegacyLayout(BufferedImage image) {
        if (image == null || image.getWidth() != image.getHeight() || computeTextureScale(image) == 0)
            return image;

        int width = image.getWidth();
        int height = image.getHeight() / 2;

        BufferedImage legacy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = legacy.getGraphics();
        graphics.drawImage(image, 0, 0, width, height, 0, 0, width, height, null);
        graphics.dispose();
        return legacy;
    }

    public static int computeTextureScale(BufferedImage image) {
        return computeTextureScale(image.getWidth(), image.getHeight());
    }

    public static int computeTextureScale(int width, int height) {
        if (width < 64 || width > 1024 || width % 64 != 0)
            return 0;

        if (height < 32 || height > 1024 || height % 32 != 0)
            return 0;

        if (!hasCorrectRatio(width, height))
            return 0;

        int scaleFactor = width / 64;
        if (scaleFactor < 1 || scaleFactor > 16)
            return 0;

        for (int allowedScaleFactor : ALLOWED_SCALE_FACTORS)
            if (scaleFactor == allowedScaleFactor)
                return scaleFactor;

        return 0;
    }

    public static boolean hasEasyxDomain(String rawUrl) {
        if (rawUrl != null && !rawUrl.isEmpty()) {
            try {
                URL parsedUrl = new URL(rawUrl);
                String host = parsedUrl.getHost();
                return host.endsWith(EASYX_DOMAINS_SUFFIX);
            } catch (Throwable ignored) {
            }
        }

        return false;
    }

    private static boolean hasCorrectRatio(int width, int height) {
        return (width == height * 2) || (width == height);
    }

}
