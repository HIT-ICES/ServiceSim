package io.github.hit_ices.serviceSim.service;

import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.entities.Vm;
import org.infrastructureProvider.policies.VmScheduler;
import org.infrastructureProvider.policies.VmResourceProvisioner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicLoadHostManager extends HostManager {

    protected Map<Host,List<HostStateHistoryEntry>>  hostStateHistory = new HashMap<Host,List<HostStateHistoryEntry>>();
    protected Map<Host,Double> hostUtilizationMips = new HashMap<Host,Double>();
    protected Map<Host,Double> hostPreviousUtilizationMips = new HashMap<Host,Double>();
    
    public DynamicLoadHostManager(VmCloudletSchedulerManagerService vmCloudletSchedulerManagerService,
                                  VmResourceProvisioner<Host,Integer> ramProvisioner,
                                  VmResourceProvisioner<Host,Long> bwProvisioner,
                                  VmScheduler vmScheduler) {
        super(vmCloudletSchedulerManagerService, ramProvisioner, bwProvisioner,  vmScheduler);
    }

    @Override
    public double updateVmsProcessing(Host host, double currentTime) {
        double smallerTime = super.updateVmsProcessing(host, currentTime);
        hostPreviousUtilizationMips.put(host,hostUtilizationMips.getOrDefault(host,0.0));
        hostUtilizationMips.put(host, 0.0);
        double hostTotalRequestedMips = 0;
        var vmList = host.getVmList();
        for (Vm vm : vmList) {
            vmScheduler.deallocatePesForVm(host, vm);
        }

        for (Vm vm : vmList) {
            vmScheduler.allocatePesForVm(host, vm, vmCloudletSchedulerManagerService.getCurrentRequestedMips(vm));
        }

        for (Vm vm : vmList) {
            double totalRequestedMips = vmCloudletSchedulerManagerService.getCurrentRequestedTotalMips(vm);
            double totalAllocatedMips = vmScheduler.getTotalAllocatedMipsForVm(host, vm);
            if (!Log.isDisabled()) {
                Log.formatLine(
                        "%.2f: [Host #" + host.getId() + "] Total allocated MIPS for VM #" + vm.getId()
                                + " (Host #" + vm.getHost().getId()
                                + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                        CloudSim.clock(),
                        totalAllocatedMips,
                        totalRequestedMips,
                        vm.getMips(),
                        totalRequestedMips / vm.getMips() * 100);

                List<Pe> pes = vmScheduler.getPesAllocatedForVM(host,vm);
                StringBuilder pesString = new StringBuilder();
                for (Pe pe : pes) {
                    pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.",
                            vmScheduler.getTotalAllocatedMipsForVm(host,vm)));
                }
                Log.formatLine(
                        "%.2f: [Host #" + host.getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
                                + host.getNumberOfPes() + " * " + vmScheduler.getPeCapacity(host) + ")."
                                + pesString,
                        CloudSim.clock());
            }

            if (host.getVmsMigratingIn().contains(vm)) {
                Log.formatLine("%.2f: [Host #" + host.getId() + "] VM #" + vm.getId()
                        + " is being migrated to Host #" + host.getId(), CloudSim.clock());
            } else {
                if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                    Log.formatLine("%.2f: [Host #" + host.getId() + "] Under allocated MIPS for VM #" + vm.getId()
                            + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                }

                vm.addStateHistoryEntry(
                        currentTime,
                        totalAllocatedMips,
                        totalRequestedMips,
                        (vm.isInMigration() && !host.getVmsMigratingIn().contains(vm)));

                if (vm.isInMigration()) {
                    Log.formatLine(
                            "%.2f: [Host #" + host.getId() + "] VM #" + vm.getId() + " is in migration",
                            CloudSim.clock());
                    totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                }
            }
            hostUtilizationMips.put(host,hostUtilizationMips.getOrDefault(host,0.0)+ totalAllocatedMips);

            hostTotalRequestedMips += totalRequestedMips;
        }
        var utilizationMips=hostUtilizationMips.getOrDefault(host,0.0);
        addStateHistoryEntry(host, currentTime, utilizationMips, hostTotalRequestedMips, hostUtilizationMips.getOrDefault(host,0.0) > 0);

        return smallerTime;
    }

    public void addStateHistoryEntry(Host host, double time, double allocatedMips, double requestedMips, boolean isActive) {
        HostStateHistoryEntry newState = new HostStateHistoryEntry(time, allocatedMips, requestedMips, isActive);
        List<HostStateHistoryEntry> stateHistory = hostStateHistory.getOrDefault(host, new ArrayList<>());
        if (!stateHistory.isEmpty() && stateHistory.get(stateHistory.size() - 1).getTime() == time) {
            stateHistory.set(stateHistory.size() - 1, newState);
        } else {
            stateHistory.add(newState);
        }
        hostStateHistory.put(host, stateHistory);
    }

    public List<Vm> getCompletedVms(Host host) {
        List<Vm> vmsToRemove = new ArrayList<>();
        for (Vm vm : host.getVmList()) {
            if (!vm.isInMigration() && vmCloudletSchedulerManagerService.getCurrentRequestedTotalMips(vm) == 0) {
                vmsToRemove.add(vm);
            }
        }
        return vmsToRemove;
    }

    public double getUtilizationOfCpu(Host host) {
        double utilization = hostUtilizationMips.getOrDefault(host,0.0) / vmScheduler.getMaxAvailableMips(host);
        return Math.min(utilization, 1);
    }

    public double getPreviousUtilizationOfCpu(Host host) {
        double utilization = hostPreviousUtilizationMips.getOrDefault(host,0.0) / vmScheduler.getMaxAvailableMips(host);
        return Math.min(utilization, 1);
    }
}
