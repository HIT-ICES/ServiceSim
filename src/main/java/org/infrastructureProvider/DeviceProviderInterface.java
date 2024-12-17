package org.infrastructureProvider;

import java.util.List;
import java.util.Map;

import org.infrastructureProvider.entities.NetworkDevice;

public interface DeviceProviderInterface {
    void init();

    void createDevices();

    <T extends NetworkDevice> List<T> getDevices();

    <T extends NetworkDevice> void setDevices(List<T> devices);

    Map<Integer, Map<Integer, Integer>> getRoutingTable();
    
    void setRoutingTable(Map<Integer, Map<Integer, Integer>> routingTable);

}
