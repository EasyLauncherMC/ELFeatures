import net.minecraftforge.forgespi.locating.IModLocator;
import org.easylauncher.mods.elfeatures.locator.ForgeModLocator;

module easylauncher.forge.modlocator {

    requires static lombok;
    requires static net.minecraftforge.forgespi;
    requires org.apache.logging.log4j;

    provides IModLocator with ForgeModLocator;

}