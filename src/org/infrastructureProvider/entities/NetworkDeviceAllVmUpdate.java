package org.infrastructureProvider.entities;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.enduser.networkPacket.*;
import org.infrastructureProvider.policies.NetworkCloudletScheduler;
import org.infrastructureProvider.policies.VmAllocationPolicy;
import org.serviceProvider.capacities.LoadAdmission;
import org.serviceProvider.capacities.LoadBalance;
import org.serviceProvider.capacities.RequestDispatchingRule;
import org.serviceProvider.capacities.ServiceDiscovery;
import org.serviceProvider.services.MicroserviceInstance;
import org.serviceProvider.services.ServiceStage;
import org.utils.GeoCoverage;
import org.utils.Location;
import org.utils.PolicyConstants;
import org.utils.ServiceSimEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkDeviceAllVmUpdate extends NetworkDevice {

    /**
     * Allocates a new PowerDatacenter object.
     *
     * @param name               the name to be associated with this entity (as required by Sim_entity class from
     *                           simjava package)
     * @param characteristics    an object of DatacenterCharacteristics
     * @param vmAllocationPolicy the vmAllocationPolicy
     * @param storageList        a LinkedList of storage elements, for data simulation
     * @param schedulingInterval
     * @throws Exception This happens when one of the following scenarios occur:
     *                   <ul>
     *                   <li>creating this entity before initializing CloudSim package
     *                   <li>this entity name is <tt>null</tt> or empty
     *                   <li>this entity has <tt>zero</tt> number of PEs (Processing Elements). <br>
     *                   No PEs mean the Cloudlets can't be processed. A CloudResource must contain one or
     *                   more Machines. A Machine must contain one or more PEs.
     *                   </ul>
     * @pre name != null
     * @pre resource != null
     * @post $none
     */
    public NetworkDeviceAllVmUpdate(String name, DatacenterCharacteristics characteristics,
                                    VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval,
                                    Location location, GeoCoverage geoCoverage, String identify, int level) throws Exception {
        super(name, characteristics,
                vmAllocationPolicy, storageList, schedulingInterval,
         location, geoCoverage, identify, level);

    }


    @Override
    public void cloudletProcessUpdate(Vm vm){
        // all vm update
        double minTime = Double.MAX_VALUE;
        for (Vm vm1 : getVmList()){
            double nextCheckInterval = vm1.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
                    .getAllocatedMipsForVm(vm));
            if (nextCheckInterval<minTime && nextCheckInterval>0){
                minTime = nextCheckInterval;
            }
        }
        if (minTime > 0 && minTime!=Double.MAX_VALUE){
            send(getId(), minTime, ServiceSimEvents.Cloudlet_PROCESS_UPDATE,vm);
        }
    }

    @Override
    public void cloudletProcessChecking(Vm vm){

        for (Vm vm1 : getVmList()){
            NetworkCloudletScheduler networkCloudletScheduler = (NetworkCloudletScheduler) vm1.getCloudletScheduler();
            // check send packets
            ArrayList<NetworkPacket> sendToSelf = new ArrayList<>();
            for (int key : networkCloudletScheduler.getPkttosend().keySet()){
                NetworkPacket cld2Packet = getCloudletIdToPacket().get(key);
                for (NetworkPacket networkPacket : networkCloudletScheduler.getPkttosend().get(key)){
                    networkPacket.setSize(networkPacket.getData());
                    networkPacket.setRemainSize(networkPacket.getData());
                    networkPacket.setSource(getId());
                    networkPacket.setSourceVm(cld2Packet.getDestinationVm());
                    networkPacket.setSourceServiceId(cld2Packet.getDestinationServiceId());
                    networkPacket.setServiceChainInfo(cld2Packet.getServiceChainInfo());
                    networkPacket.setEndUserInfo(cld2Packet.getEndUserInfo());
                    if (cld2Packet.getSourceServiceId() == networkPacket.getDestinationServiceId()){
                        networkPacket.setType(NetworkConstants.RESPONSE);
                        networkPacket.setDestination(cld2Packet.getSource());
                        networkPacket.setDestinationVm(cld2Packet.getSourceVm());
                        networkPacket.setDestinationCloudlet(cld2Packet.getSourceCloudlet());
                    }else{
                        networkPacket.setType(NetworkConstants.REQUEST);
                        int destination = getRequestDispatchingRule().findDeviceId(networkPacket,getServiceDiscovery(),getId(),getChildDeviceIds(),getParentDeviceIds(),getSameLevelDeviceIds());
                        networkPacket.setDestination(destination);
                    }
                    networkPacket.setSendTime(CloudSim.clock());
                    if (networkPacket.getDestination() == getId()){
                        sendToSelf.add(networkPacket);
                    }else{
                        int nexthop = routingTable.get(getId()).get(networkPacket.getDestination());
                        CloudSim.cancelAll(getId(), new PredicateType(ServiceSimEvents.Packet_SEND));
                        send(getId(), PolicyConstants.PacketArrivalProcess,ServiceSimEvents.Packet_SEND);
                        Log.printLine("packet generated and send to: "+nexthop);
                        addPacketToSendMap(nexthop, networkPacket);
                    }
                }
            }
            if (sendToSelf.size()>0){
                send(getId(), PolicyConstants.PacketArrivalProcess,ServiceSimEvents.Packet_ARRIVAL,sendToSelf);
            }
            networkCloudletScheduler.getPkttosend().clear();

            // check finished cloudlets
            // @todo
            for (int i = 0; i < networkCloudletScheduler.getCloudletFinishedList().size();i++){
                int id = networkCloudletScheduler.getCloudletFinishedList().get(i).getCloudletId();
                NetworkPacket networkPacket = getCloudletIdToPacket().get(id);
                getCloudletIdToPacket().remove(id);
                Pair<Cloudlet, NetworkPacket> data= new Pair<>(networkCloudletScheduler.getCloudletFinishedList().get(i).getCloudlet(),networkPacket);
                sendNow(networkCloudletScheduler.getCloudletFinishedList().get(i).getUserId(), CloudSimTags.CLOUDLET_RETURN, data);
            }
            networkCloudletScheduler.getCloudletFinishedList().clear();

            // check executed cloudlets
            if (getVmsNeedToDestroy().keySet().contains(vm1.getId())){
                if (networkCloudletScheduler.getCloudletExecList().size() == 0){
                    doVmDestroy(vm1, getVmsNeedToDestroy().get(vm1.getId()));
                    getVmsNeedToDestroy().remove(vm1.getId());
                }
            }
        }


    }


}
