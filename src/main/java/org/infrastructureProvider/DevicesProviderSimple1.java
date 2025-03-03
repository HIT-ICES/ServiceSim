package org.infrastructureProvider;

import com.alibaba.fastjson.JSONObject;
import org.cloudbus.cloudsim.Storage;
import org.customBuilder.factory.ProfileFactory;
import org.infrastructureProvider.entities.*;
import org.infrastructureProvider.policies.PacketSchedulerTimeShared;
import org.infrastructureProvider.policies.ShortestPathRoutingGenerator;
import org.infrastructureProvider.policies.VmAllocationPolicySimple;
import org.infrastructureProvider.policies.vm.VmSchedulerSpaceShared;
import org.infrastructureProvider.policies.provisioners.BwProvisionerSimple;
import org.infrastructureProvider.policies.provisioners.PeProvisionerSimple;
import org.infrastructureProvider.policies.provisioners.RamProvisionerSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utils.GeoCoverage;
import org.utils.Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* small base station(small data center / edge server) num: 32
 * medium base station(medium data center / edge server) num: 4
 * router num: 1
 * cloud(big data center / cloud server) : 1
 *  */
public class DevicesProviderSimple1 extends DevicesProvider {
    private static final Logger logger = LoggerFactory.getLogger(DevicesProviderSimple1.class);

    int smallBS;
    int meBS;
    int router;

    public DevicesProviderSimple1(int smallBS, int meBS, int router) {

        super();
        this.smallBS = smallBS;
        this.meBS = meBS;
        this.router = router;
        createDevices();

    }

    public DevicesProviderSimple1(JSONObject config) {
        super(config);
        smallBS = config.getInteger("smallBS");
        meBS = config.getInteger("meBS");
        router = config.getInteger("router");
        createDevices();
    }

    @Override
    public void createDevices() {
        // create NetworkDevices and their relations
        int hostNum = 8;
        int[] pesList = new int[hostNum]; // Pes List of each Host
        int[] ram = new int[hostNum]; // Ram capacity of each Host
        int[] bw = new int[hostNum]; // BW capacity of each Host
        long[] storage = new long[hostNum]; // storage capacity of each Host
        for (int i = 0; i < hostNum; i++) {
            pesList[i] = 8;
            ram[i] = 4096;
            bw[i] = 10000;
            storage[i] = 1000000;
        }
        int mips = 40;

        ArrayList<NetworkDevice> devices = new ArrayList<>();
        ArrayList<NetworkDevice> smallBS_devices;
        ArrayList<NetworkDevice> meBS_devices;
        ArrayList<NetworkDevice> router_devices;
        ArrayList<NetworkDevice> cloud_devices;

        smallBS_devices = createNetworkDevices("EdgeLevel1_DataCenter_", "edge", 1, 0, null, smallBS, hostNum, pesList,
                mips, ram, bw, storage);
        meBS_devices = createNetworkDevices("EdgeLevel2_DataCenter_", "edge", 2, smallBS, null, meBS, hostNum, pesList,
                mips, ram, bw, storage);
        router_devices = createNetworkDevices("Router_", "router", 3, smallBS + meBS, null, router, 1, pesList,
                mips, ram, bw, storage);
        cloud_devices = createNetworkDevices("Cloud_", "cloud", 4, -1, null, 1, hostNum, pesList,
                mips, ram, bw, storage);

        int smallBSNumPerMeBS = smallBS / meBS;

        for (int i = 0; i < smallBS_devices.size(); i++) {
            Channel channel = new Channel(smallBS_devices.get(i).getId(), meBS_devices.get(i / smallBSNumPerMeBS).getId(), 100 * 1024 * 1024, 0.0005, new PacketSchedulerTimeShared());
            smallBS_devices.get(i).getParentDeviceIds().add(meBS_devices.get(i / smallBSNumPerMeBS).getId());
            smallBS_devices.get(i).getParentDevicesToChannel().put(meBS_devices.get(i / smallBSNumPerMeBS).getId(), channel);

            Channel channel1 = new Channel(meBS_devices.get(i / smallBSNumPerMeBS).getId(), smallBS_devices.get(i).getId(), 100 * 1024 * 1024, 0.0005, new PacketSchedulerTimeShared());
            meBS_devices.get(i / smallBSNumPerMeBS).getChildDeviceIds().add(smallBS_devices.get(i).getId());
            meBS_devices.get(i / smallBSNumPerMeBS).getChildDevicesToChannel().put(smallBS_devices.get(i).getId(), channel1);
        }

        int meBSNumPerRouter = meBS / router;

        for (int i = 0; i < meBS_devices.size(); i++) {
            Channel channel = new Channel(meBS_devices.get(i).getId(), router_devices.get(i / meBSNumPerRouter).getId(), 100 * 1024 * 1024, 0.002, new PacketSchedulerTimeShared());
            meBS_devices.get(i).getParentDeviceIds().add(router_devices.get(i / meBSNumPerRouter).getId());
            meBS_devices.get(i).getParentDevicesToChannel().put(router_devices.get(i / meBSNumPerRouter).getId(), channel);

            Channel channel1 = new Channel(router_devices.get(i / meBSNumPerRouter).getId(), meBS_devices.get(i).getId(), 100 * 1024 * 1024, 0.002, new PacketSchedulerTimeShared());
            router_devices.get(i / meBSNumPerRouter).getChildDeviceIds().add(meBS_devices.get(i).getId());
            router_devices.get(i / meBSNumPerRouter).getChildDevicesToChannel().put(meBS_devices.get(i).getId(), channel1);
        }

        for (NetworkDevice routerDevice : router_devices) {
            Channel channel = new Channel(routerDevice.getId(), cloud_devices.get(0).getId(), 100 * 1024 * 1024, 0.06, new PacketSchedulerTimeShared());
            routerDevice.getParentDeviceIds().add(cloud_devices.get(0).getId());
            routerDevice.getParentDevicesToChannel().put(cloud_devices.get(0).getId(), channel);

            Channel channel1 = new Channel(cloud_devices.get(0).getId(), routerDevice.getId(), 100 * 1024 * 1024, 0.06, new PacketSchedulerTimeShared());
            cloud_devices.get(0).getChildDeviceIds().add(routerDevice.getId());
            cloud_devices.get(0).getChildDevicesToChannel().put(routerDevice.getId(), channel1);
        }

        devices.addAll(smallBS_devices);
        devices.addAll(meBS_devices);
        devices.addAll(router_devices);
        devices.addAll(cloud_devices);
        // create routingTable and add routingTable to the devices
        Map<Integer, Map<Integer, Integer>> routingTable = ShortestPathRoutingGenerator.generateRoutingTable(devices);
        for (NetworkDevice device : devices) {
            device.setRoutingTable(routingTable);
        }
        setDevices(devices);
        setRoutingTable(routingTable);

    }

    public ArrayList<NetworkDevice> createNetworkDevices(String name, String identify, int level, int blockNum, GeoCoverage geoCoverage, int dataCenterNum, int hostCount, int[] pesCount,
                                                         int mipsLength, int[] ram, int[] bw, long[] storage) {
        ArrayList<NetworkDevice> devices = new ArrayList<>();
        for (int i = 0; i < dataCenterNum; i++) {
            String datacenterName = name + i;
            Location location = new Location(-1, -1, blockNum + i);
            NetworkDevice networkDevice = createNetworkDevice(datacenterName, identify, level, location, geoCoverage, hostCount, pesCount,
                    mipsLength, ram, bw, storage);
            devices.add(networkDevice);
        }
        return devices;

    }

    public NetworkDevice createNetworkDevice(String name, String identify, int level, Location location, GeoCoverage geoCoverage, int hostCount, int[] pesCount,
                                             int mipsLength, int[] ram, int[] bw, long[] storage) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < hostCount; i++) {

            List<Pe> peListTmp = new ArrayList<>();
            for (int j = 0; j < pesCount[i]; j++)
                peListTmp.add(new Pe(j, new PeProvisionerSimple(mipsLength)));

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
        double costPerMem = 0.05;        // the cost of using memory in this resource
        double costPerStorage = 0.1;    // the cost of using storage in this resource
        double costPerBw = 0.1;            // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();    //we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        NetworkDevice datacenter = null;
        try {
            datacenter = new NetworkDevice(name, characteristics,
                    new VmAllocationPolicySimple(hostList), storageList, 0,
                    location, geoCoverage, identify, level);
        } catch (Exception e) {
            logger.error("Error in creating Datacenter", e);
        }

        return datacenter;
    }
}
