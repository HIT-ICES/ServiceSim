package org.infrastructureProvider.entities;

import java.util.ArrayList;
import java.util.List;

public class HostEntity {

    private int id;
    private long storage;
    private List<? extends Pe> peList;
    private final List<? extends VmEntity> vmList = new ArrayList<>();
    private final List<VmEntity> vmsMigratingIn = new ArrayList<>();
    private boolean failed;

    public HostEntity(int id, long storage, List<? extends Pe> peList) {
        this.id = id;
        this.storage = storage;
        this.peList = peList;
        this.failed = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getStorage() { return storage; }
    public void setStorage(long storage) { this.storage = storage; }

    public List<? extends Pe> getPeList() { return peList; }
    public void setPeList(List<? extends Pe> peList) { this.peList = peList; }

    public List<? extends VmEntity> getVmList() { return vmList; }
    public List<VmEntity> getVmsMigratingIn() { return vmsMigratingIn; }

    public boolean isFailed() { return failed; }
    public void setFailed(boolean failed) { this.failed = failed; }
}