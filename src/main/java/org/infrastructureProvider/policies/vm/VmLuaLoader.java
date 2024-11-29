package org.infrastructureProvider.policies.vm;

import org.infrastructureProvider.InfraLuaLoader;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.entities.Vm;
import org.infrastructureProvider.policies.provisioners.PeProvisionerSimple;
import party.iroiro.luajava.value.LuaValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VmLuaLoader extends InfraLuaLoader {
    private static final String base = "main";
    private String code;
    private String impl;
    private String obj;

    private void init(List<? extends Pe> peList) {
        exec(getLuaCode(base));
        if (code != null) {
            exec(getLuaCode(code));
        }
        obj = executeWithReturn(impl + ":new", null, peList).getKey();
    }

    public VmLuaLoader(List<? extends Pe> peList) {
        super("vm");
        this.code = null;
        this.impl = "VmScheduler";
        init(peList);
    }

    public VmLuaLoader(String code, List<? extends Pe> peList) {
        super("vm");
        this.code = code;
        this.impl = code;
        init(peList);
    }

    public VmLuaLoader(String code, String impl, List<? extends Pe> peList) {
        super("vm");
        this.code = code;
        this.impl = impl;
        init(peList);
    }

    public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
        return executeWithReturn(obj + ":allocatePesForVm", vm, mipsShare).getValue().toBoolean();
    }

    public void deallocatePesForVm(Vm vm) {
        execute(obj + ":deallocatePesForVm", vm);
    }

    public void deallocatePesForAllVms() {
        execute(obj + ":deallocatePesForAllVms");
    }

    public List<Pe> getPesAllocatedForVM(Vm vm) {
        return (List<Pe>) executeWithReturn(obj + ":getPesAllocatedForVM", vm).getValue().toJavaObject();
    }

    public List<Double> getAllocatedMipsForVm(Vm vm) {
        return (List<Double>) executeWithReturn(obj + ":getAllocatedMipsForVm", vm).getValue().toJavaObject();
    }

    public double getTotalAllocatedMipsForVm(Vm vm) {
        return executeWithReturn(obj + ":getTotalAllocatedMipsForVm", vm).getValue().toNumber();
    }

    public double getMaxAvailableMips() {
        return executeWithReturn(obj + ":getMaxAvailableMips").getValue().toNumber();
    }

    public double getPeCapacity() {
        return executeWithReturn(obj + ":getPeCapacity").getValue().toNumber();
    }

    public <T extends Pe> List<T> getPeList() {
        return (List<T>) executeWithReturn(obj + ":getPeList").getValue().toJavaObject();
    }

    public <T extends Pe> void setPeList(List<T> peList) {
        execute(obj + ":setPeList", peList);
    }

    public Map<String, List<Double>> getMipsMap() {
        return (Map<String, List<Double>>) executeWithReturn(obj + ":getMipsMap").getValue().toJavaObject();
    }

    public void setMipsMap(Map<String, List<Double>> mipsMap) {
        execute(obj + ":setMipsMap", mipsMap);
    }

    public double getAvailableMips() {
        return executeWithReturn(obj + ":getAvailableMips").getValue().toNumber();
    }

    public void setAvailableMips(double availableMips) {
        execute(obj + ":setAvailableMips", availableMips);
    }

    public List<String> getVmsMigratingOut() {
        return (List<String>) executeWithReturn(obj + ":getVmsMigratingOut").getValue().toJavaObject();
    }

    protected void setVmsMigratingOut(List<String> vmsMigratingOut) {
        execute(obj + ":setVmsMigratingOut", vmsMigratingOut);
    }

    public List<String> getVmsMigratingIn() {
        return (List<String>) executeWithReturn(obj + ":getVmsMigratingIn").getValue().toJavaObject();
    }

    protected void setVmsMigratingIn(List<String> vmsMigratingIn) {
        execute(obj + ":setVmsMigratingIn", vmsMigratingIn);
    }

    public Map<String, List<Pe>> getPeMap() {
        return (Map<String, List<Pe>>) executeWithReturn(obj + ":getPeMap").getValue().toJavaObject();
    }

    protected void setPeMap(Map<String, List<Pe>> peMap) {
        execute(obj + ":setPeMap", peMap);
    }

    public static void main(String[] args) {
        List<Pe> pes = new ArrayList<>();
        pes.add(new Pe(0, new PeProvisionerSimple(2142)));
        pes.add(new Pe(1, new PeProvisionerSimple(6480)));
        try (VmLuaLoader loader = new VmLuaLoader(pes)) {
            double res = loader.getMaxAvailableMips();
            System.out.println(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
