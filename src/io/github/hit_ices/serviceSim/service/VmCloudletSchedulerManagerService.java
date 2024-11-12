package io.github.hit_ices.serviceSim.service;

import io.github.hit_ices.serviceSim.prototype.MapBasedManagerService;
import org.infrastructureProvider.entities.Vm;
import org.infrastructureProvider.policies.CloudletScheduler;

import java.util.ArrayList;
import java.util.List;

// New Service Interface Implementation
public class VmCloudletSchedulerManagerService extends MapBasedManagerService<Vm, CloudletScheduler> {

    /**
     * Updates the processing of cloudlets running on this VM.
     *
     * @param currentTime current simulation time
     * @param mipsShare   array with MIPS share of each Pe available to the scheduler
     * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is no
     * next events
     * @pre currentTime >= 0
     * @post $none
     */

    public double updateVmProcessing(Vm vm, double currentTime, List<Double> mipsShare) {
        if (mipsShare != null) {
            return getManager(vm).updateVmProcessing(currentTime, mipsShare);
        }
        return 0.0;
    }

    public List<Double> getCurrentRequestedMips(Vm vm) {
        List<Double> currentRequestedMips = getManager(vm).getCurrentRequestedMips();
        if (vm.isBeingInstantiated()) {
            currentRequestedMips = new ArrayList<>();
            for (int i = 0; i < vm.getNumberOfPes(); i++) {
                currentRequestedMips.add(vm.getMips());
            }
        }
        return currentRequestedMips;
    }

    public double getTotalUtilizationOfCpu(Vm vm, double time) {
        return getManager(vm).getTotalUtilizationOfCpu(time);
    }

    public double getTotalUtilizationOfCpuMips(Vm vm, double time) {
        return getTotalUtilizationOfCpu(vm, time) * vm.getMips();
    }

    public long getCurrentRequestedBw(Vm vm) {
        if (vm.isBeingInstantiated()) {
            return vm.getBw();
        }
        return (long) (getManager(vm).getCurrentRequestedUtilizationOfBw() * vm.getBw());
    }

    public int getCurrentRequestedRam(Vm vm) {
        if (vm.isBeingInstantiated()) {
            return vm.getRam();
        }
        return (int) (getManager(vm).getCurrentRequestedUtilizationOfRam() * vm.getRam());
    }



    /**
     * Gets the current requested total mips.
     *
     * @return the current requested total mips
     */
    public double getCurrentRequestedTotalMips(Vm vm) {
        double totalRequestedMips = 0;
        for (double mips : getCurrentRequestedMips(vm)) {
            totalRequestedMips += mips;
        }
        return totalRequestedMips;
    }

    /**
     * Gets the current requested max mips among all virtual PEs.
     *
     * @return the current requested max mips
     */
    public double getCurrentRequestedMaxMips(Vm vm) {
        double maxMips = 0;
        for (double mips : getCurrentRequestedMips(vm)) {
            if (mips > maxMips) {
                maxMips = mips;
            }
        }
        return maxMips;
    }
}
