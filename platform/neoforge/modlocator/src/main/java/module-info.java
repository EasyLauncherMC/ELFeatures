import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IModLocator;
import org.easylauncher.mods.elfeatures.locator.NeoForgeModFileCandidateLocator;
import org.easylauncher.mods.elfeatures.locator.NeoForgeModLocator;

module easylauncher.modlocator.neoforge {

    requires static lombok;
    requires static fml_loader;
    requires static fml_spi;
    requires static net.neoforged.neoforgespi;
    requires org.apache.logging.log4j;

    provides IModLocator with NeoForgeModLocator;
    provides IModFileCandidateLocator with NeoForgeModFileCandidateLocator;

}