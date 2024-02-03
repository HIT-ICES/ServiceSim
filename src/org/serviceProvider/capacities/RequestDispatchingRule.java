package org.serviceProvider.capacities;

import org.enduser.networkPacket.NetworkPacket;
import org.infrastructureProvider.entities.NetworkDevice;

import java.util.ArrayList;

public abstract class RequestDispatchingRule {

     private ArrayList<NetworkDevice> networkDevices; // the dispatching range, which determines the service discovery update.

     public RequestDispatchingRule(ArrayList<NetworkDevice> networkDevices){
          this.networkDevices = networkDevices;
     }

     public abstract int findDeviceId(NetworkPacket request, ServiceDiscovery serviceDiscovery,
                      int deviceId, ArrayList<Integer> childDeviceIds, ArrayList<Integer> parentDeviceIds, ArrayList<Integer> sameLevelDeviceIds); // return networkDeviceId

     public ArrayList<NetworkDevice> getNetworkDevices() {
          return networkDevices;
     }

     public void setNetworkDevices(ArrayList<NetworkDevice> networkDevices) {
          this.networkDevices = networkDevices;
     }
}
