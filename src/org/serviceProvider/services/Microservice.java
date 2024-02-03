package org.serviceProvider.services;

import org.enduser.networkPacket.NetworkCloudlet;

import java.util.ArrayList;

public class Microservice {
   public int serviceId;

   /* a microservice may have many networkCloudlet instances */
   public ArrayList<NetworkCloudlet> netClist;

   /* a microservice may have a set of vms to deployment */
   public ArrayList<MicroserviceInstance> serVmList;

   public Microservice(int serviceId){
      this.serviceId = serviceId;
      netClist = new ArrayList<NetworkCloudlet>();
      serVmList = new ArrayList<MicroserviceInstance> ();
   }

}
