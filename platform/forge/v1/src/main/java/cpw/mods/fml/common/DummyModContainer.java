package cpw.mods.fml.common;

import com.google.common.eventbus.EventBus;

import java.io.File;

public class DummyModContainer {

    public DummyModContainer(ModMetadata md) {
    }

    public File getSource() {
        return null;
    }

    public boolean registerBus(EventBus bus, LoadController controller) {
        return false;
    }

    public Class<?> getCustomResourcePackClass() {
        return null;
    }

}
