package org.infrastructureProvider.policies.vm;

import java.util.List;
import java.util.Map;

import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.entities.Vm;

public interface VmInterface {
    public String init(List<? extends Pe> peList);

    public boolean allocatePesForVm(Vm vm, List<Double> mipsShare);

    public void deallocatePesForVm(Vm vm);

    public void deallocatePesForAllVms();

    public List<Pe> getPesAllocatedForVM(Vm vm);

    public List<Double> getAllocatedMipsForVm(Vm vm);

    public double getTotalAllocatedMipsForVm(Vm vm);

    public double getMaxAvailableMips();

    public double getPeCapacity();

    public <T extends Pe> List<T> getPeList();

    public <T extends Pe> void setPeList(List<T> peList);

    public Map<String, List<Double>> getMipsMap();

    public void setMipsMap(Map<String, List<Double>> mipsMap);

    public double getAvailableMips();

    public void setAvailableMips(double availableMips);

    public List<String> getVmsMigratingOut();

    public void setVmsMigratingOut(List<String> vmsMigratingOut);

    public List<String> getVmsMigratingIn();

    public void setVmsMigratingIn(List<String> vmsMigratingIn);

    public Map<String, List<Pe>> getPeMap();

    public void setPeMap(Map<String, List<Pe>> peMap);
}
