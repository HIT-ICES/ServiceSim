package org.serviceProvider.capacities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoundRobin implements LoadBalance {

    private Map<Integer, Integer> position = new HashMap<>(); // serviceId, instanceId

    private Map<Integer, Integer> service0Position = new HashMap<>(); // serviceChainId, instanceId

    /* return instanceId */
    @Override
    public int findInstanceId(ServiceDiscovery serviceDiscovery, int serviceId, int deviceId) {
        if (position.containsKey(serviceId) && serviceDiscovery.getServiceIdToInstanceList().containsKey(serviceId)) {
            if (serviceDiscovery.getServiceIdToInstanceList().get(serviceId).containsKey(deviceId))
            {
                if (serviceDiscovery.getServiceIdToInstanceList().get(serviceId).get(deviceId).size() == 0){
                    System.out.println("Service Discovery Information Missing");
                    return -1;
                }
                int pos = position.get(serviceId);
                if (pos + 1 > serviceDiscovery.getServiceIdToInstanceList().get(serviceId).get(deviceId).size() - 1)
                    pos = 0;
                else
                    pos = pos + 1;
                position.put(serviceId, pos);
                return serviceDiscovery.getServiceIdToInstanceList().get(serviceId).get(deviceId).get(pos);
            }else{
                System.out.println("Service Discovery Information Missing");
                return -1;
            }

        } else {
            if(serviceDiscovery.getServiceIdToInstanceList().containsKey(serviceId)) {

                if (serviceDiscovery.getServiceIdToInstanceList().get(serviceId).containsKey(deviceId)){

                    if (serviceDiscovery.getServiceIdToInstanceList().get(serviceId).get(deviceId).size() == 0){
                        System.out.println("Service Discovery Information Missing");
                        return -1;
                    }
                    position.put(serviceId, 0);
                    int instanceId = serviceDiscovery.getServiceIdToInstanceList().get(serviceId).get(deviceId).get(0);
                    return instanceId;

                }else{
                    System.out.println("Service Discovery Information Missing");
                    return -1;
                }
            }
            System.out.println("Service Discovery Information Missing");
            return -1;
        }
    }

    public int findService0InstanceId(ServiceDiscovery serviceDiscovery,int serviceChainId, int deviceId){
        if (service0Position.containsKey(serviceChainId) && serviceDiscovery.getService0ToInstanceList().containsKey(serviceChainId)) {
            if (serviceDiscovery.getService0ToInstanceList().get(serviceChainId).containsKey(deviceId))
            {
                if (serviceDiscovery.getService0ToInstanceList().get(serviceChainId).get(deviceId).size() == 0){
                    System.out.println("Service Discovery Information Missing");
                    return -1;
                }
                int pos = service0Position.get(serviceChainId);
                if (pos + 1 > serviceDiscovery.getService0ToInstanceList().get(serviceChainId).get(deviceId).size() - 1)
                    pos = 0;
                else
                    pos = pos + 1;
                service0Position.put(serviceChainId, pos);
                return serviceDiscovery.getService0ToInstanceList().get(serviceChainId).get(deviceId).get(pos);
            }else{
                System.out.println("Service Discovery Information Missing");
                return -1;
            }

        } else {
            if(serviceDiscovery.getService0ToInstanceList().containsKey(serviceChainId)) {

                if (serviceDiscovery.getService0ToInstanceList().get(serviceChainId).containsKey(deviceId)){

                    if (serviceDiscovery.getService0ToInstanceList().get(serviceChainId).get(deviceId).size() == 0){
                        System.out.println("Service Discovery Information Missing");
                        return -1;
                    }
                    service0Position.put(serviceChainId, 0);
                    int instanceId = serviceDiscovery.getService0ToInstanceList().get(serviceChainId).get(deviceId).get(0);
                    return instanceId;

                }else{
                    System.out.println("Service Discovery Information Missing");
                    return -1;
                }
            }
            System.out.println("Service Discovery Information Missing");
            return -1;
        }
    }
}
