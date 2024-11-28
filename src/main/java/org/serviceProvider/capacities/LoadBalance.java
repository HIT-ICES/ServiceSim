package org.serviceProvider.capacities;

public interface LoadBalance {

    int findInstanceId(ServiceDiscovery serviceDiscovery, int serviceId, int deviceId);

    @SuppressWarnings("unused")
    int findService0InstanceId(ServiceDiscovery serviceDiscovery, int serviceChainId, int deviceId);

}
