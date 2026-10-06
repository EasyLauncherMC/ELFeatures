package org.easylauncher.mods.elfeatures.mixin.quickplay;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code WorldOpenFlows.loadLevel(Screen, String)}, the way into a world from 1.19 on.
 *
 * <p>The build version 1.17.1 has no such class, so it is a stub, and the renaming to SRG has no name for its method:
 * it is invoked by the SRG name outright.
 */
@Mixin(WorldOpenFlows.class)
public interface MixinWorldOpenFlows {

    @Invoker(value = "m_233133_", remap = false)
    void elfeatures$loadLevel(Screen parent, String levelName);

}
