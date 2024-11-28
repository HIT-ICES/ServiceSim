package org.serviceProvider.capacities;

import org.cloudbus.cloudsim.Log;
import org.enduser.networkPacket.NetworkPacket;
import org.infrastructureProvider.entities.NetworkDevice;

import java.util.*;

public class RequestDispatchingSimple extends RequestDispatchingRule {

    public RequestDispatchingSimple(ArrayList<NetworkDevice> networkDevices) {
        super(networkDevices);
    }

    /* 1. check self; 2. check same level devices; 3. check child devices; 4. check parent devices */

    /* return deviceId */
    @Override
    public int findDeviceId(NetworkPacket request, ServiceDiscovery serviceDiscovery,
                            int deviceId, ArrayList<Integer> childDeviceIds, ArrayList<Integer> parentDeviceIds, ArrayList<Integer> sameLevelDeviceIds) {

        int requestServiceId = request.getDestinationServiceId();
        Log.printLine("find service " + requestServiceId);

        if (!serviceDiscovery.getServiceIdToInstanceList().containsKey(requestServiceId)) {
            System.out.println("Service Discovery Information Missing or there is no service instance");
            return -1;
        } else {
            // 1. check self
            if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).containsKey(deviceId)) {

                if (!serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).get(deviceId).isEmpty()) {
                    return deviceId;
                }
            }
            // 2. check same level devices
            ArrayList<Integer> devices = new ArrayList<>();
            for (Integer sameLevelDeviceId : sameLevelDeviceIds) {
                if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).containsKey(sameLevelDeviceId)) {

                    if (!serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).get(sameLevelDeviceId).isEmpty()) {
                        devices.add(sameLevelDeviceId);
                    }
                }
            }
            if (!devices.isEmpty()) {
                Random r = new Random();
                int index = r.nextInt(devices.size());
                Log.printLine("service has find in" + devices.get(index));
                return devices.get(index);
            }

            // 3. check child devices
            ArrayList<Integer> devices1 = new ArrayList<>();
            for (Integer childDeviceId : childDeviceIds) {
                if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).containsKey(childDeviceId)) {

                    if (!serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).get(childDeviceId).isEmpty()) {
                        devices1.add(childDeviceId);
                    }
                }
            }
            if (!devices1.isEmpty()) {
                Random r = new Random();
                int index = r.nextInt(devices1.size());
                return devices1.get(index);
            }

            // 4. check parent devices
            ArrayList<Integer> devices2 = new ArrayList<>();
            for (Integer parentDeviceId : parentDeviceIds) {
                if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).containsKey(parentDeviceId)) {

                    if (!serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).get(parentDeviceId).isEmpty()) {
                        devices2.add(parentDeviceId);
                    }
                }
            }
            if (!devices2.isEmpty()) {
                Random r = new Random();
                int index = r.nextInt(devices2.size());
                return devices2.get(index);
            }

            // 5. check parents' children
            Set<Integer> hashSet = new HashSet<>();
            for (Integer parentDeviceId : parentDeviceIds) {
                ArrayList<Integer> parentChildIds = findDeviceAccDeviceId(getNetworkDevices(), parentDeviceId).getChildDeviceIds();
                for (Integer parentChildId : parentChildIds) {
                    if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).containsKey(parentChildId)) {

                        if (!serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).get(parentChildId).isEmpty()) {
                            hashSet.add(parentChildId);
                        }
                    }
                }

            }
            ArrayList<Integer> devices3 = new ArrayList<>(hashSet);
            if (!devices3.isEmpty()) {
                Random r = new Random();
                int index = r.nextInt(devices3.size());
                return devices3.get(index);
            }

            // 6. check others
            int id = findCloud(getNetworkDevices());
//            ArrayList<Integer> devices4 = new ArrayList<>();
//
//            for (int i = 0 ; i < getNetworkDevices().size(); i ++){
//                if (getNetworkDevices().get(i).getId() != id){
//                    if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).containsKey(getNetworkDevices().get(i).getId())){
//
//                        if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).get(getNetworkDevices().get(i).getId()).size() > 0){
//                            devices4.add(getNetworkDevices().get(i).getId());
//                        }
//                    }
//                }
//            }
//            if (devices4.size() > 0){
//                Random r = new Random();
//                int index = r.nextInt(devices4.size());
//                return devices4.get(index);
//            }

            // 7. check cloud
            if (serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).containsKey(id)) {
                if (!serviceDiscovery.getServiceIdToInstanceList().get(requestServiceId).get(id).isEmpty()) {
                    return id;
                }
            }

        }
        System.out.println("Service Discovery Information Missing or there is no service instance");
        return -1;
    }

    public NetworkDevice findDeviceAccDeviceId(ArrayList<NetworkDevice> networkDevices, int deviceId) {
        for (NetworkDevice device : networkDevices) {
            if (device.getId() == deviceId) {
                return device;
            }
        }
        return null;
    }

    public int findCloud(ArrayList<NetworkDevice> networkDevices) {
        for (NetworkDevice device : networkDevices) {
            if (Objects.equals(device.getIdentify(), "cloud")) {
                return device.getId();
            }
        }
        return -1;
    }


}
