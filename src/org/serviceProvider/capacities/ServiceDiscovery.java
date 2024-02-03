package org.serviceProvider.capacities;

import org.cloudbus.cloudsim.Log;
import org.infrastructureProvider.entities.NetworkDevice;
import org.serviceProvider.services.MicroserviceInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServiceDiscovery {


    private Map<Integer, Map<Integer, ArrayList<Integer> > > serviceIdToInstanceList; // service discovery info

    private Map<Integer, Map<Integer, ArrayList<Integer> > > service0ToInstanceList; // seervicechainID ->device id -> instance id


    public ServiceDiscovery() {
        serviceIdToInstanceList = new HashMap<>();
        service0ToInstanceList = new HashMap<>();
    }

    public Map<Integer, Map<Integer, ArrayList<Integer>>> getServiceIdToInstanceList() {
        return serviceIdToInstanceList;
    }

    public void setServiceIdToInstanceList(Map<Integer, Map<Integer, ArrayList<Integer>>> serviceIdToInstanceList) {
        this.serviceIdToInstanceList = serviceIdToInstanceList;
    }

    public Map<Integer, Map<Integer, ArrayList<Integer>>> getService0ToInstanceList() {
        return service0ToInstanceList;
    }

    public void setService0ToInstanceList(Map<Integer, Map<Integer, ArrayList<Integer>>> service0ToInstanceList) {
        this.service0ToInstanceList = service0ToInstanceList;
    }


    // add service discovery info
    public void addServiceDiscoveryInfo(MicroserviceInstance microserviceInstance){
        int serviceId = microserviceInstance.getServiceId();
        int deviceId = microserviceInstance.getHost().getDatacenter().getId();
        if (serviceId < 0 || deviceId < 0){
            Log.print("ServiceDiscovery: addServiceDiscoveryInfo error in serviceId or deviceId");
        }
        if (serviceIdToInstanceList.containsKey(serviceId)){
            if (serviceIdToInstanceList.get(serviceId).containsKey(deviceId)){
                ArrayList<Integer> instanceList = serviceIdToInstanceList.get(serviceId).get(deviceId);
                if (instanceList.contains(microserviceInstance.getId())){
                    return;
                }
                instanceList.add(microserviceInstance.getId());
                serviceIdToInstanceList.get(serviceId).put(deviceId, instanceList);
            }else{
                Map<Integer, ArrayList<Integer>> deviceIdToInstanceList = serviceIdToInstanceList.get(serviceId);
                ArrayList<Integer> instanceList = new ArrayList<>();
                instanceList.add(microserviceInstance.getId());
                deviceIdToInstanceList.put(deviceId,instanceList);
                serviceIdToInstanceList.put(serviceId, deviceIdToInstanceList);
            }

        }else{
            Map<Integer, ArrayList<Integer>> deviceIdToInstanceList = new HashMap<>();
            ArrayList<Integer> instanceList = new ArrayList<>();
            instanceList.add(microserviceInstance.getId());
            deviceIdToInstanceList.put(deviceId,instanceList);
            serviceIdToInstanceList.put(serviceId, deviceIdToInstanceList);
        }

        // for service0
        if (serviceId == 0){
            int serviceChainId = microserviceInstance.serviceChainId;
            if (service0ToInstanceList.containsKey(serviceChainId)){
                if (service0ToInstanceList.get(serviceChainId).containsKey(deviceId)){
                    ArrayList<Integer> instanceList = service0ToInstanceList.get(serviceChainId).get(deviceId);
                    if (instanceList.contains(microserviceInstance.getId())){
                        return;
                    }
                    instanceList.add(microserviceInstance.getId());
                    service0ToInstanceList.get(serviceChainId).put(deviceId, instanceList);
                }else{
                    Map<Integer, ArrayList<Integer>> deviceIdToInstanceList = service0ToInstanceList.get(serviceChainId);
                    ArrayList<Integer> instanceList = new ArrayList<>();
                    instanceList.add(microserviceInstance.getId());
                    deviceIdToInstanceList.put(deviceId,instanceList);
                    service0ToInstanceList.put(serviceChainId, deviceIdToInstanceList);
                }
            }else{
                Map<Integer, ArrayList<Integer>> deviceIdToInstanceList = new HashMap<>();
                ArrayList<Integer> instanceList = new ArrayList<>();
                instanceList.add(microserviceInstance.getId());
                deviceIdToInstanceList.put(deviceId,instanceList);
                service0ToInstanceList.put(serviceChainId, deviceIdToInstanceList);
            }
        }

    }

    // remove service discovery info
    public void removeServiceDiscoveryInfo(MicroserviceInstance microserviceInstance){
        int serviceId = microserviceInstance.getServiceId();
        int deviceId = microserviceInstance.getHost().getDatacenter().getId();
        if (serviceIdToInstanceList.containsKey(serviceId)){
            if (serviceIdToInstanceList.get(serviceId).containsKey(deviceId)){
                if(serviceIdToInstanceList.get(serviceId).get(deviceId).contains(microserviceInstance.getId())){
                    ArrayList<Integer> instanceList = serviceIdToInstanceList.get(serviceId).get(deviceId);
                    instanceList.remove(microserviceInstance.getId());
                    serviceIdToInstanceList.get(serviceId).put(deviceId,instanceList);
                }else{
                    return;
                }
            }else{
                return;
            }
        }else{
            return;
        }
        // for service0
        if (serviceId == 0){
            int serviceChainId = microserviceInstance.serviceChainId;
            if (service0ToInstanceList.containsKey(serviceChainId)){
                if (service0ToInstanceList.get(serviceChainId).containsKey(deviceId)){
                    if(service0ToInstanceList.get(serviceChainId).get(deviceId).contains(microserviceInstance.getId())){
                        ArrayList<Integer> instanceList = service0ToInstanceList.get(serviceChainId).get(deviceId);
                        instanceList.remove(microserviceInstance.getId());
                        service0ToInstanceList.get(serviceChainId).put(deviceId,instanceList);
                    }else{
                        return;
                    }
                }else{
                    return;
                }
            }else{
                return;
            }
        }

    }

}
