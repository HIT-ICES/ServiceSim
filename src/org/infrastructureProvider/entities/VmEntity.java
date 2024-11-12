package org.infrastructureProvider.entities;

import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.infrastructureProvider.policies.CloudletScheduler;

import java.util.LinkedList;
import java.util.List;

public class VmEntity {

    private int id;
    private int userId;
    private String uid;
    private long size;
    private double mips;
    private int numberOfPes;
    private int ram;
    private long bw;
    private String vmm;
    private HostEntity host;
    private boolean inMigration;
    private long currentAllocatedSize;
    private int currentAllocatedRam;
    private long currentAllocatedBw;
    private List<Double> currentAllocatedMips;
    private boolean beingInstantiated;
    private final List<VmStateHistoryEntry> stateHistory = new LinkedList<>();

    public VmEntity(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm) {
        this.id = id;
        this.userId = userId;
        this.uid = userId + "-" + id;
        this.mips = mips;
        this.numberOfPes = numberOfPes;
        this.ram = ram;
        this.bw = bw;
        this.size = size;
        this.vmm = vmm;
        this.inMigration = false;
        this.beingInstantiated = true;
        this.currentAllocatedBw = 0;
        this.currentAllocatedMips = null;
        this.currentAllocatedRam = 0;
        this.currentAllocatedSize = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public double getMips() { return mips; }
    public void setMips(double mips) { this.mips = mips; }

    public int getNumberOfPes() { return numberOfPes; }
    public void setNumberOfPes(int numberOfPes) { this.numberOfPes = numberOfPes; }

    public int getRam() { return ram; }
    public void setRam(int ram) { this.ram = ram; }

    public long getBw() { return bw; }
    public void setBw(long bw) { this.bw = bw; }

    public String getVmm() { return vmm; }
    public void setVmm(String vmm) { this.vmm = vmm; }

    public HostEntity getHost() { return host; }
    public void setHost(HostEntity host) { this.host = host; }

    public boolean isInMigration() { return inMigration; }
    public void setInMigration(boolean inMigration) { this.inMigration = inMigration; }

    public long getCurrentAllocatedSize() { return currentAllocatedSize; }
    public void setCurrentAllocatedSize(long currentAllocatedSize) { this.currentAllocatedSize = currentAllocatedSize; }

    public int getCurrentAllocatedRam() { return currentAllocatedRam; }
    public void setCurrentAllocatedRam(int currentAllocatedRam) { this.currentAllocatedRam = currentAllocatedRam; }

    public long getCurrentAllocatedBw() { return currentAllocatedBw; }
    public void setCurrentAllocatedBw(long currentAllocatedBw) { this.currentAllocatedBw = currentAllocatedBw; }

    public List<Double> getCurrentAllocatedMips() { return currentAllocatedMips; }
    public void setCurrentAllocatedMips(List<Double> currentAllocatedMips) { this.currentAllocatedMips = currentAllocatedMips; }

    public boolean isBeingInstantiated() { return beingInstantiated; }
    public void setBeingInstantiated(boolean beingInstantiated) { this.beingInstantiated = beingInstantiated; }

    public List<VmStateHistoryEntry> getStateHistory() { return stateHistory; }
    public void addStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isInMigration) {
        VmStateHistoryEntry newState = new VmStateHistoryEntry(time, allocatedMips, requestedMips, isInMigration);
        if (!stateHistory.isEmpty() && stateHistory.get(stateHistory.size() - 1).getTime() == time) {
            stateHistory.set(stateHistory.size() - 1, newState);
            return;
        }
        stateHistory.add(newState);
    }
}