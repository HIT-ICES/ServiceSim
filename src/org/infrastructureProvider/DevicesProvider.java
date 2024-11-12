package org.infrastructureProvider;

import io.github.hit_ices.serviceSim.service.HostManager;
import io.github.hit_ices.serviceSim.service.VmCloudletSchedulerManagerService;
import org.infrastructureProvider.entities.NetworkDevice;

import java.util.List;
import java.util.Map;

public abstract class DevicesProvider {

    private List<? extends NetworkDevice> devices;

    private Map<Integer, Map<Integer, Integer>> routingTable; // now deviceId -> destination deviceId, next deviceId

    // manager service
    HostManager hostManager;

    public VmCloudletSchedulerManagerService getVmCloudletSchedulerManagerService() {
        return hostManager.getVmCloudletSchedulerManager();
    }

    public DevicesProvider() {

    }


    public abstract void createDevices();
        // create NetworkDevices

        // create routingTable

        // add routngTable to the devices

    @SuppressWarnings("unchecked")
    public <T extends NetworkDevice> List<T> getDevices() {
        return (List<T>) devices;
    }

    protected <T extends NetworkDevice> void setDevices(List<T> devices) {
        this.devices = devices;
    }


    public Map<Integer, Map<Integer, Integer>> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(Map<Integer, Map<Integer, Integer>> routingTable) {
        this.routingTable = routingTable;
    }




}
