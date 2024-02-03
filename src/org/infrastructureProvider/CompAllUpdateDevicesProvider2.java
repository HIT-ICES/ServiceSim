package org.infrastructureProvider;

/*
simulate k8s
Fully connected
 *  */

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.infrastructureProvider.entities.*;
import org.infrastructureProvider.policies.PacketSchedulerTimeShared;
import org.infrastructureProvider.policies.ShortestPathRoutingGenerator;
import org.infrastructureProvider.policies.VmAllocationPolicySimple;
import org.infrastructureProvider.policies.VmSchedulerSpaceShared;
import org.infrastructureProvider.policies.provisioners.BwProvisionerSimple;
import org.infrastructureProvider.policies.provisioners.PeProvisionerSimple;
import org.infrastructureProvider.policies.provisioners.RamProvisionerSimple;
import org.utils.GeoCoverage;
import org.utils.Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CompAllUpdateDevicesProvider2 extends DevicesProvider {

    int nodeNum;

    public CompAllUpdateDevicesProvider2(int nodeNum) {
        super();
        this.nodeNum = nodeNum;
        createDevices();
    }


    @Override
    public void createDevices(){
        // create NetworkDevices and their relations
        int hostnum = 1;
        int[] pesList = new int[hostnum]; // Pes List of each Host
        int[] ram = new int[hostnum]; // Ram capacity of each Host
        int[] bw = new int[hostnum]; // BW capacity of each Host
        long[] storage = new long[hostnum] ; // storage capacity of each Host
        for (int i = 0; i < hostnum; i++){
            pesList[i] = 20;//之前是16
            ram[i] = 4096;
            bw[i] = 100000;//之前是10000
            storage[i] = 1000000;
        }
        int mips = 110;

        ArrayList<NetworkDeviceAllVmUpdate> smallBS_devices = createNetworkDevices("EdgeLevel1_DataCenter_", "edge", 1, 0, null,nodeNum, hostnum, pesList ,
                mips, ram, bw, storage);
        ArrayList<NetworkDeviceAllVmUpdate> router = createNetworkDevices("Router_", "router", 2, nodeNum, null, 1, 1, pesList , mips, ram, bw, storage);

        for (int i = 0;i<smallBS_devices.size();i++){

            if (i==3){
                Channel channel = new Channel(smallBS_devices.get(i).getId(),router.get(0).getId(),20 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                smallBS_devices.get(i).getParentDeviceIds().add(router.get(0).getId());
                smallBS_devices.get(i).getParentDevicesToChannel().put(router.get(0).getId(),channel);

                Channel channel1 = new Channel(router.get(0).getId(),smallBS_devices.get(i).getId(),20 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                router.get(0).getChildDeviceIds().add(smallBS_devices.get(i).getId());
                router.get(0).getChildDevicesToChannel().put(smallBS_devices.get(i).getId(),channel1);
            }else if(i == 1){
                Channel channel = new Channel(smallBS_devices.get(i).getId(),router.get(0).getId(),20 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                smallBS_devices.get(i).getParentDeviceIds().add(router.get(0).getId());
                smallBS_devices.get(i).getParentDevicesToChannel().put(router.get(0).getId(),channel);

                Channel channel1 = new Channel(router.get(0).getId(),smallBS_devices.get(i).getId(),20 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                router.get(0).getChildDeviceIds().add(smallBS_devices.get(i).getId());
                router.get(0).getChildDevicesToChannel().put(smallBS_devices.get(i).getId(),channel1);
            }else if(i == 2){
                Channel channel = new Channel(smallBS_devices.get(i).getId(),router.get(0).getId(),50 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                smallBS_devices.get(i).getParentDeviceIds().add(router.get(0).getId());
                smallBS_devices.get(i).getParentDevicesToChannel().put(router.get(0).getId(),channel);

                Channel channel1 = new Channel(router.get(0).getId(),smallBS_devices.get(i).getId(),50 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                router.get(0).getChildDeviceIds().add(smallBS_devices.get(i).getId());
                router.get(0).getChildDevicesToChannel().put(smallBS_devices.get(i).getId(),channel1);
            }
            else{
                Channel channel = new Channel(smallBS_devices.get(i).getId(),router.get(0).getId(),50 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                smallBS_devices.get(i).getParentDeviceIds().add(router.get(0).getId());
                smallBS_devices.get(i).getParentDevicesToChannel().put(router.get(0).getId(),channel);

                Channel channel1 = new Channel(router.get(0).getId(),smallBS_devices.get(i).getId(),50 * 1024 * 1024,0.0001,new PacketSchedulerTimeShared());
                router.get(0).getChildDeviceIds().add(smallBS_devices.get(i).getId());
                router.get(0).getChildDevicesToChannel().put(smallBS_devices.get(i).getId(),channel1);
            }


        }
        ArrayList<NetworkDevice> devices= new ArrayList<>();
        devices.addAll(smallBS_devices);
        devices.addAll(router);

        // create routingTable and add routngTable to the devices
        Map<Integer, Map<Integer, Integer>> routingTable = ShortestPathRoutingGenerator.generateRoutingTable(devices);
        for (int i = 0;i<devices.size();i++){
            devices.get(i).setRoutingTable(routingTable);
        }
        Log.printLine("routing table "+ routingTable);
        setDevices(devices);
        setRoutingTable(routingTable);
    }

    public ArrayList<NetworkDeviceAllVmUpdate> createNetworkDevices(String name, String identify, int level, int blockNum, GeoCoverage geoCoverage, int dataCenterNum, int hostcount, int[] pescount ,
                                                         int mipslenght, int[] ram, int[] bw, long[] storage){
        ArrayList<NetworkDeviceAllVmUpdate> devices = new ArrayList<>();
        for (int i = 0; i< dataCenterNum; i++){
            String datacenterName = name + String.valueOf(i);
            Location location = new Location(-1,-1,blockNum+i);
            NetworkDeviceAllVmUpdate networkDevice = createNetworkDevice(datacenterName, identify, level, location, geoCoverage, hostcount, pescount ,
                    mipslenght, ram, bw, storage);
            devices.add(networkDevice);
        }
        return devices;

    }

    public NetworkDeviceAllVmUpdate createNetworkDevice(String name, String identify, int level, Location location, GeoCoverage geoCoverage, int hostcount, int[] pescount ,
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


        NetworkDeviceAllVmUpdate datacenter = null;
        try {
            datacenter = new NetworkDeviceAllVmUpdate(name, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 0,
                    location, geoCoverage, identify, level);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
}
