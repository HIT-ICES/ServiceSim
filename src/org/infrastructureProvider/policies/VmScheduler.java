package org.infrastructureProvider.policies;

import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.entities.Vm;

import java.util.List;
import java.util.Map;

public interface VmScheduler {
    void manage(Host host);

    /**
     * Allocates PEs for a VM.
     *
     * @param vm        the vm
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    boolean allocatePesForVm(Host host, Vm vm, List<Double> mipsShare);

    /**
     * Releases PEs allocated to a VM.
     *
     * @param vm the vm
     * @pre $none
     * @post $none
     */
    void deallocatePesForVm(Host host, Vm vm);

    void deallocatePesForAllVms(Host host);

    List<Pe> getPesAllocatedForVM(Host host, Vm vm);

    List<Double> getAllocatedMipsForVm(Host host, Vm vm);

    double getTotalAllocatedMipsForVm(Host host, Vm vm);

    double getMaxAvailableMips(Host host);

    double getPeCapacity(Host host);

    @SuppressWarnings("unchecked")
    <T extends Pe> List<T> getPeList(Host host);

    double getAvailableMips(Host host);

    List<String> getVmsMigratingOut(Host host);

    List<String> getVmsMigratingIn(Host host);

    Map<String, List<Pe>> getPeMap(Host host);
}
