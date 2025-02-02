package net.neoforged.neoforgespi.locating;

import net.neoforged.neoforgespi.ILaunchContext;

public interface IModFileCandidateLocator extends IOrderedProvider {

    String name();

    void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline);

}
