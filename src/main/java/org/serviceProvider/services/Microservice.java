package org.serviceProvider.services;

import org.enduser.networkPacket.NetworkCloudlet;

import java.util.ArrayList;

public class Microservice {
    public int serviceId;

    /* a microservice may have many networkCloudlet instances */
    public ArrayList<NetworkCloudlet> netCloudletList;

    /* a microservice may have a set of vms to deployment */
    public ArrayList<MicroserviceInstance> serVmList;

    public Microservice(int serviceId) {
        this.serviceId = serviceId;
        netCloudletList = new ArrayList<>();
        serVmList = new ArrayList<>();
    }

}
