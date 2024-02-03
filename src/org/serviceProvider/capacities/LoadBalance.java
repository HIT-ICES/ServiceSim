package org.serviceProvider.capacities;

import java.util.ArrayList;
import java.util.Map;

public interface LoadBalance {

   int findInstanceId(ServiceDiscovery serviceDiscovery, int serviceId, int deviceId);

   int findService0InstanceId(ServiceDiscovery serviceDiscovery,int serviceChainId, int deviceId);

}
