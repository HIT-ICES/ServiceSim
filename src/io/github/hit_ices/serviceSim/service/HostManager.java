package io.github.hit_ices.serviceSim.service;

import org.cloudbus.cloudsim.Log;
import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Vm;
import org.infrastructureProvider.policies.VmScheduler;
import org.infrastructureProvider.policies.VmResourceProvisioner;

import java.util.List;

// HostManager class with methods moved from Host
public class HostManager {
    protected final VmScheduler vmScheduler;
    protected final VmResourceProvisioner<Host, Integer> ramProvisioner;
    protected final VmResourceProvisioner<Host, Long> bwProvisioner;

    protected final VmCloudletSchedulerManagerService vmCloudletSchedulerManagerService;
    public VmScheduler getVmScheduler() {return vmScheduler;}
    public HostManager(VmCloudletSchedulerManagerService vmCloudletSchedulerManagerService,
                       VmResourceProvisioner<Host, Integer> ramProvisioner,
                       VmResourceProvisioner<Host, Long> bwProvisioner,
                       VmScheduler vmScheduler) {
        this.vmScheduler = vmScheduler;
        this.ramProvisioner = ramProvisioner;
        this.bwProvisioner = bwProvisioner;
        this.vmCloudletSchedulerManagerService=vmCloudletSchedulerManagerService;
    }

    public double updateVmsProcessing(Host host, double currentTime) {
        double smallerTime = Double.MAX_VALUE;

        for (Vm vm : host.getVmList()) {
            double time = vmCloudletSchedulerManagerService.updateVmProcessing(vm, currentTime, vmScheduler.getAllocatedMipsForVm(host,vm));
            if (time > 0.0 && time < smallerTime) {
                smallerTime = time;
            }
        }
        return smallerTime;
    }

    public void addMigratingInVm(Host host, Vm vm, List<Vm> vmsMigratingIn, long storage) {
        vm.setInMigration(true);

        if (!vmsMigratingIn.contains(vm)) {
            if (storage < vm.getSize()) {
                Log.printLine("[VmSchedulerBase.addMigratingInVm] Allocation of VM #" + vm.getId() + " failed by storage");
                System.exit(0);
            }

            if (!ramProvisioner.allocateForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedRam(vm))) {
                Log.printLine("[VmSchedulerBase.addMigratingInVm] Allocation of VM #" + vm.getId() + " failed by RAM");
                System.exit(0);
            }

            if (!bwProvisioner.allocateForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedBw(vm))) {
                Log.printLine("[VmSchedulerBase.addMigratingInVm] Allocation of VM #" + vm.getId() + " failed by BW");
                System.exit(0);
            }

            vmScheduler.getVmsMigratingIn(host).add(vm.getUid());
            if (!vmScheduler.allocatePesForVm(host,vm, vmCloudletSchedulerManagerService.getCurrentRequestedMips(vm))) {
                Log.printLine("[VmSchedulerBase.addMigratingInVm] Allocation of VM #" + vm.getId() + " failed by MIPS");
                System.exit(0);
            }

            storage -= vm.getSize();
            vmsMigratingIn.add(vm);
        }
    }

    public void removeMigratingInVm(Host host, Vm vm, List<Vm> vmsMigratingIn, List<? extends Vm> vmList) {
        vmDeallocate(host, vm);
        vmsMigratingIn.remove(vm);
        vmList.remove(vm);
        vmScheduler.getVmsMigratingIn(host).remove(vm.getUid());
        vm.setInMigration(false);
    }

    public void reallocateMigratingInVms(Host host, List<Vm> vmsMigratingIn) {
        var vmList = host.getVmList();
        for (Vm vm : vmsMigratingIn) {
            if (!vmList.contains(vm)) {
                vmList.add(vm);
            }
            if (!vmScheduler.getVmsMigratingIn(host).contains(vm.getUid())) {
                vmScheduler.getVmsMigratingIn(host).add(vm.getUid());
            }
            ramProvisioner.allocateForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedRam(vm));
            bwProvisioner.allocateForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedBw(vm));
            vmScheduler.allocatePesForVm(host,vm, vmCloudletSchedulerManagerService.getCurrentRequestedMips(vm));
        }
    }

    public boolean isSuitableForVm(Host host, Vm vm) {
        return (vmScheduler.getPeCapacity(host) >= vmCloudletSchedulerManagerService.getCurrentRequestedMaxMips(vm)
                && vmScheduler.getAvailableMips(host) >= vmCloudletSchedulerManagerService.getCurrentRequestedTotalMips(vm)
                && ramProvisioner.isSuitableForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedRam(vm))
                && bwProvisioner.isSuitableForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedBw(vm)));
    }

    public boolean vmCreate(Host host, Vm vm) {
        if (host.getStorage() < vm.getSize()) {
            Log.printLine("[VmSchedulerBase.vmCreate] Allocation of VM #" + vm.getId() + " failed by storage");
            return false;
        }

        if (!ramProvisioner.allocateForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedRam(vm))) {
            Log.printLine("[VmSchedulerBase.vmCreate] Allocation of VM #" + vm.getId() + " failed by RAM");
            return false;
        }

        if (!bwProvisioner.allocateForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedBw(vm))) {
            Log.printLine("[VmSchedulerBase.vmCreate] Allocation of VM #" + vm.getId() + " failed by BW");
            ramProvisioner.deallocateForVm(host, vm);
            return false;
        }

        if (!vmScheduler.allocatePesForVm(host,vm, vmCloudletSchedulerManagerService.getCurrentRequestedMips(vm))) {
            Log.printLine("[VmSchedulerBase.vmCreate] Allocation of VM #" + vm.getId() + " failed by MIPS");
            ramProvisioner.deallocateForVm(host, vm);
            bwProvisioner.deallocateForVm(host, vm);
            return false;
        }

        host.setStorage(host.getStorage() - vm.getSize());
        host.getVmList().add(vm);
        vm.setHost(host);
        return true;
    }

    public void vmDestroy(Host host, Vm vm) {
        if (vm != null) {
            vmDeallocate(host, vm);
            host.getVmList().remove(vm);
            vm.setHost(null);
        }
    }

    public void vmDestroyAll(Host host, List<? extends Vm> vmList, long storage) {
        vmDeallocateAll(host);
        for (Vm vm : vmList) {
            vm.setHost(null);
            storage += vm.getSize();
        }
        vmList.clear();
    }

    protected void vmDeallocate(Host host, Vm vm) {
        ramProvisioner.deallocateForVm(host, vm);
        bwProvisioner.deallocateForVm(host, vm);
        vmScheduler.deallocatePesForVm(host,vm);
    }

    protected void vmDeallocateAll(Host host) {
        ramProvisioner.deallocateForAllVms(host);
        bwProvisioner.deallocateForAllVms(host);
        vmScheduler.deallocatePesForAllVms(host);
    }

    /**
     * Allocates PEs for a VM.
     *
     * @param vm        the vm
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean allocatePesForVm(Host host, Vm vm, List<Double> mipsShare) {
        return vmScheduler.allocatePesForVm(host,vm, mipsShare);
    }

    /**
     * Releases PEs allocated to a VM.
     *
     * @param vm the vm
     * @pre $none
     * @post $none
     */
    public void deallocatePesForVm(Host host, Vm vm) {
        vmScheduler.deallocatePesForVm(host,vm);
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given VM.
     *
     * @param vm the vm
     * @return an array containing the amount of MIPS of each pe that is available to the VM
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForVm(Host host, Vm vm) {
        return vmScheduler.getAllocatedMipsForVm(host,vm);
    }

    /**
     * Gets the total allocated MIPS for a VM over all the PEs.
     *
     * @param vm the vm
     * @return the allocated mips for vm
     */
    public double getTotalAllocatedMipsForVm(Host host, Vm vm) {
        return vmScheduler.getTotalAllocatedMipsForVm(host,vm);
    }

    /**
     * Returns maximum available MIPS among all the PEs.
     *
     * @return max mips
     */
    public double getMaxAvailableMips(Host host) {
        return vmScheduler.getMaxAvailableMips(host);
    }

    public VmCloudletSchedulerManagerService getVmCloudletSchedulerManager() {
        return vmCloudletSchedulerManagerService;
    }

    public void removeMigratingInVms(Host host, Vm vm) {

        vmDeallocate(host, vm);
        host.getVmsMigratingIn().remove(vm);
        host.getVmList().remove(vm);
        vmScheduler.getVmsMigratingIn(host).remove(vm.getUid());
        vm.setInMigration(false);


    }
}
