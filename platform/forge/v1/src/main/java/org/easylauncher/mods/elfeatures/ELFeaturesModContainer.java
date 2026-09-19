package org.easylauncher.mods.elfeatures;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.common.ModMetadata;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/** The mod's entry in FML's mod list, described by its mcmod.info as its {@code @Mod} was. */
public final class ELFeaturesModContainer extends DummyModContainer {

    private static final String MOD_ID = "elfeatures";

    public ELFeaturesModContainer() throws IOException {
        super(readMetadata());
    }

    // the mod list takes the logo out of this pack
    @Override
    public Class<?> getCustomResourcePackClass() {
        return FMLFileResourcePack.class;
    }

    @Override
    public File getSource() {
        return ELFeaturesFMLPlugin.jar;
    }

    // FML disables a container that takes no events
    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    // out of the jar itself: the first mcmod.info on the class path is some other mod's
    private static ModMetadata readMetadata() throws IOException {
        try (ZipFile jar = new ZipFile(ELFeaturesFMLPlugin.jar)) {
            MetadataCollection metadata = MetadataCollection.from(jar.getInputStream(jar.getEntry("mcmod.info")), MOD_ID);
            return metadata.getMetadataForId(MOD_ID, null);
        }
    }

}
