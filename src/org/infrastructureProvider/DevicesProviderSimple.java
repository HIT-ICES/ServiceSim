package org.infrastructureProvider;

import org.cloudbus.cloudsim.Storage;
import org.infrastructureProvider.entities.*;
import org.infrastructureProvider.policies.*;
import org.infrastructureProvider.policies.provisioners.BwProvisionerSimple;
import org.infrastructureProvider.policies.provisioners.PeProvisionerSimple;
import org.infrastructureProvider.policies.provisioners.RamProvisionerSimple;
import org.utils.GeoCoverage;
import org.utils.Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DevicesProviderSimple extends DevicesProvider {


    public DevicesProviderSimple() {
        super();
        createDevices();
    }

    @Override
    public void createDevices() {
        // create NetworkDevices and their relations

        int[] pesList = new int[8]; // Pes List of each Host
        int[] ram = new int[8]; // Ram capacity of each Host
        int[] bw = new int[8]; // BW capacity of each Host
        long[] storage = new long[8] ; // storage capacity of each Host
        for (int i = 0; i < 8; i++){
            pesList[i] = 8;
            ram[i] = 4096;
            bw[i] = 10000;
            storage[i] = 1000000;
        }
        int mips = 40;
        Location location0 = new Location(-1,-1,-1);
        Location location = new Location(-1,-1,0);
        Location location1 = new Location(-1,-1,1);
        Location location2 = new Location(-1,-1,2);
        NetworkDevice cloudDataCenter = createNetworkDevice("Datacenter_0", "cloud", 3, location0, null, 8, pesList ,
                mips, ram, bw, storage);

        NetworkDevice edgeDataCenter1 = createNetworkDevice("Datacenter_1", "router", 2, location2, null, 1, pesList ,
                mips, ram, bw, storage);

        NetworkDevice edgeDataCenter2 = createNetworkDevice("Datacenter_2", "edge", 1, location, null, 2, pesList ,
                mips, ram, bw, storage);
        NetworkDevice edgeDataCenter3 = createNetworkDevice("Datacenter_3", "edge", 1, location1, null, 2, pesList ,
                mips, ram, bw, storage);

        Channel channel = new Channel(cloudDataCenter.getId(),edgeDataCenter1.getId(),100 * 1024 * 1024,0.15,new PacketSchedulerTimeShared());
        Channel channel1 = new Channel(edgeDataCenter1.getId(),cloudDataCenter.getId(),100 * 1024 * 1024,0.15,new PacketSchedulerTimeShared());

        Channel channel2 = new Channel(edgeDataCenter1.getId(),edgeDataCenter2.getId(),100 * 1024 * 1024,0.002,new PacketSchedulerTimeShared());
        Channel channel3 = new Channel(edgeDataCenter2.getId(),edgeDataCenter1.getId(),100 * 1024 * 1024,0.002,new PacketSchedulerTimeShared());

        Channel channel4 = new Channel(edgeDataCenter1.getId(),edgeDataCenter3.getId(),100 * 1024 * 1024,0.002,new PacketSchedulerTimeShared());
        Channel channel5 = new Channel(edgeDataCenter3.getId(),edgeDataCenter1.getId(),100 * 1024 * 1024,0.002,new PacketSchedulerTimeShared());

        cloudDataCenter.getChildDeviceIds().add(edgeDataCenter1.getId());
        cloudDataCenter.getChildDevicesToChannel().put(edgeDataCenter1.getId(),channel);

        edgeDataCenter1.getParentDeviceIds().add(cloudDataCenter.getId());
        edgeDataCenter1.getParentDevicesToChannel().put(cloudDataCenter.getId(),channel1);
        edgeDataCenter1.getChildDeviceIds().add(edgeDataCenter2.getId());
        edgeDataCenter1.getChildDeviceIds().add(edgeDataCenter3.getId());
        edgeDataCenter1.getChildDevicesToChannel().put(edgeDataCenter2.getId(),channel2);
        edgeDataCenter1.getChildDevicesToChannel().put(edgeDataCenter3.getId(),channel4);

        edgeDataCenter2.getParentDeviceIds().add(edgeDataCenter1.getId());
        edgeDataCenter2.getParentDevicesToChannel().put(edgeDataCenter1.getId(),channel3);

        edgeDataCenter3.getParentDeviceIds().add(edgeDataCenter1.getId());
        edgeDataCenter3.getParentDevicesToChannel().put(edgeDataCenter1.getId(),channel5);

        ArrayList<NetworkDevice> devices = new ArrayList<>();
        devices.add(cloudDataCenter);
        devices.add(edgeDataCenter1);
        devices.add(edgeDataCenter2);
        devices.add(edgeDataCenter3);

        // create routingTable and add routngTable to the devices
        Map<Integer, Map<Integer, Integer>> routingTable = ShortestPathRoutingGenerator.generateRoutingTable(devices);
        for (int i = 0;i<devices.size();i++){
            devices.get(i).setRoutingTable(routingTable);
        }
        setDevices(devices);
        setRoutingTable(routingTable);

    }

    public NetworkDevice createNetworkDevice(String name, String identify, int level, Location location, GeoCoverage geoCoverage, int hostcount, int[] pescount ,
                                             int mipslenght, int[] ram, int[] bw, long[] storage){
        List<Host> hostList = new ArrayList<Host>();

        for (int i=0; i< hostcount;i++){

            List<Pe> peListTmp = new ArrayList<Pe>();
            for(int j= 0; j < pescount[i]; j++)
                peListTmp.add(new Pe(j, new PeProvisionerSimple(mipslenght)));

            hostList.add(
                    new Host(
                            i,
                            new RamProvisionerSimple(ram[i]),
                            new BwProvisionerSimple(bw[i]),
                            storage[i],
                            peListTmp,
                            new VmSchedulerSpaceShared(peListTmp)
                    )
            );
        }

        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.1;	// the cost of using storage in this resource
        double costPerBw = 0.1;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        NetworkDevice datacenter = null;
        try {
            datacenter = new NetworkDevice(name, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 0,
            location, geoCoverage, identify, level);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
}
