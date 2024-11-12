package org.infrastructureProvider.policies;

import org.infrastructureProvider.entities.Vm;

public interface VmResourceProvisioner<THost, TResource> {
    /**
     * Allocates RAM for a given VM on the specified host.
     *
     * @param host the host on which the RAM is being allocated
     * @param vm   the virtual machine for which the RAM is being allocated
     * @param res  the amount of RAM to allocate
     * @return true if the RAM could be allocated; false otherwise
     */
    boolean allocateForVm(THost host, Vm vm, TResource res);

    /**
     * Gets the allocated RAM for a VM on the specified host.
     *
     * @param host the host
     * @param vm   the VM
     * @return the allocated RAM for the VM
     */
    TResource getAllocatedForVm(THost host, Vm vm);

    /**
     * Releases RAM used by a VM on the specified host.
     *
     * @param host the host
     * @param vm   the VM
     */
    void deallocateForVm(THost host, Vm vm);

    void deallocateForAllVms(THost host);

    /**
     * Checks if the host has sufficient RAM for a given VM allocation.
     *
     * @param host the host
     * @param vm   the VM
     * @param res  the amount of resource to check
     * @return true if the host has sufficient RAM; false otherwise
     */
    boolean isSuitableForVm(THost host, Vm vm, TResource res);
}
