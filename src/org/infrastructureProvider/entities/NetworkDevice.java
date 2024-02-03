package org.infrastructureProvider.entities;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.lists.VmList;
import org.enduser.networkPacket.*;
import org.infrastructureProvider.policies.CloudletScheduler;
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

import java.util.*;

public class NetworkDevice extends Datacenter {

    private Location location;

    private GeoCoverage geoCoverage;

    private String identify; // "cloud" or "edge"

    private int level; // 1,2,3,...

    /* Associated Devices */
    private ArrayList<Integer> childDeviceIds = new ArrayList<>();

    private ArrayList<Integer> parentDeviceIds = new ArrayList<>();

    private ArrayList<Integer> sameLevelDeviceIds = new ArrayList<>();


    /* Network model */
    private Map<Integer, Channel> sameLevelDevicesToChannel = new HashMap<>();

    private Map<Integer, Channel> parentDevicesToChannel = new HashMap<>();

    private Map<Integer, Channel> childDevicesToChannel = new HashMap<>();

    /* NetworkPackets */
    private ArrayList<NetworkPacket> receivedResponsePackets = new ArrayList<>();

    private Map<Integer, ArrayList<NetworkPacket>> sendToSameLevelDevicesPktList = new HashMap<>();

    private Map<Integer, ArrayList<NetworkPacket>> sendToChildDevicesPktList = new HashMap<>();

    private Map<Integer, ArrayList<NetworkPacket>> sendToParentDevicesPktList = new HashMap<>();

//    private List<? extends NetworkPacket> receivedResponsePackets = new ArrayList<>();
//
//    private Map<Integer, List<? extends NetworkPacket>> sendToSameLevelDevicesPktList = new HashMap<>();
//
//    private Map<Integer, List<? extends NetworkPacket>> sendToChildDevicesPktList = new HashMap<>();
//
//    private Map<Integer, List<? extends NetworkPacket>> sendToParentDevicesPktList = new HashMap<>();


    private Map<Integer, NetworkPacket> cloudletIdToPacket = new HashMap<>(); // for cloudlets' life cycle

    /* NetworkCloudlet */

    private Map<Integer, ArrayList<NetworkCloudlet>> instanceIdToCloudlet = new HashMap<>(); // for cloudlets submit

    /* routing */
    Map<Integer, Map<Integer, Integer>> routingTable;

    /* service related */

    private ServiceDiscovery serviceDiscovery; // local service discovery

    private LoadBalance loadBalance; // local load balance

    private RequestDispatchingRule requestDispatchingRule; // request dispatching

    private LoadAdmission loadAdmission; // load admission for gateway


    /* record */
    // for vm destroy
    private Map<Integer, Boolean> vmsNeedToDestroy = new HashMap<>();

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
    public NetworkDevice(String name, DatacenterCharacteristics characteristics,
                         VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval,
                         Location location, GeoCoverage geoCoverage, String identify, int level) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.location = location;
        this.geoCoverage = geoCoverage;
        this.identify = identify;
        this.level = level;
        serviceDiscovery = new ServiceDiscovery();

    }

    @Override
    protected void processOtherEvent(SimEvent ev){
        switch (ev.getTag()){
            case ServiceSimEvents.Packet_ARRIVAL:
                processNetworkPacketArrival(ev);
                break;
            case ServiceSimEvents.Packet_SEND:
                networkPacketSend();
                break;
            case ServiceSimEvents.Packet_SEND_CHECK:
                packetSendChecking(ev);
                break;
            case ServiceSimEvents.Cloudlet_SUBMIT:
                cloudletSubmit();
                break;
            case ServiceSimEvents.Cloudlet_PROCESS_UPDATE:
                Vm vm = (Vm) ev.getData();
                cloudletProcessUpdate(vm);
                cloudletProcessChecking(vm);
                break;
            case ServiceSimEvents.Cloudlet_UPDATE_FOR_RESPONSE:
                processCloudletUpdateForResponse();
                break;
            case ServiceSimEvents.Service_DISCOVERY_ADD:
                serviceRegistration(ev);
                break;
            case ServiceSimEvents.Service_DISCOVERY_DEL:
                serviceCancellation(ev);
                break;
            default:
                super.processOtherEvent(ev);
                break;
        }
    }


    protected void processNetworkPacketArrival(SimEvent ev){

        List<NetworkPacket> networkPackets = (List<NetworkPacket>) ev.getData();

        // Judge whether it is the destination
        for (int i = 0; i < networkPackets.size(); i++){

            NetworkPacket networkPacket = networkPackets.get(i);
            networkPacket.setRemainSize(networkPacket.getSize());

            int destination = networkPacket.destination;

            if (destination != getId()){ // transmit
                // Get the next hop address according to the routing table
                int nexthop = routingTable.get(getId()).get(destination);

                CloudSim.cancelAll(getId(), new PredicateType(ServiceSimEvents.Packet_SEND));
                send(getId(), PolicyConstants.PacketArrivalProcess,ServiceSimEvents.Packet_SEND);

                addPacketToSendMap(nexthop, networkPacket);

            }else{
                networkPacket.setRecvTime(CloudSim.clock());
                int type = networkPacket.getType();
                if (type == NetworkConstants.REQUEST){
                    // 1. load admission
                    if (!loadAdmission.isAdmission(networkPacket)){
                        break;
                        // to do: request failed
                    }

                    // 2. load balance
                    int instanceId;
//                    if (networkPacket.getDestinationServiceId() == 0){
//                        instanceId = loadBalance.findService0InstanceId(serviceDiscovery,networkPacket.getServiceChainInfo().getServiceChainId(),getId());
//                    }else{
//                        instanceId = loadBalance.findInstanceId(serviceDiscovery, networkPacket.getDestinationServiceId(),getId());
//
//                    }
                    instanceId = loadBalance.findInstanceId(serviceDiscovery, networkPacket.getDestinationServiceId(),getId());
                    if (instanceId < 0){
                        Log.printLine(CloudSim.clock() + ": " + getName() + ": can not find instance!" );
                        return;
                        // to do: request failed
                    }
                    networkPacket.setDestinationVm(instanceId);
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": request packet #" + networkPackets.get(i).getAppId()
                            + " has arrivalled in Datacenter #" + getId() + " and was routed to vm #" + instanceId);

                    // 3. generate cloudlets
                    NetworkCloudlet networkCloudlet = generateCloudlet(networkPacket);
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": " + networkCloudlet.getCloudletId());
                    cloudletIdToPacket.put(networkCloudlet.getCloudletId(),networkPacket);
                    if (instanceIdToCloudlet.containsKey(instanceId)){
                        instanceIdToCloudlet.get(instanceId).add(networkCloudlet);
                    }else {
                        ArrayList<NetworkCloudlet> cloudlets = new ArrayList<>();
                        cloudlets.add(networkCloudlet);
                        instanceIdToCloudlet.put(instanceId,cloudlets);
                    }

                    CloudSim.cancelAll(getId(), new PredicateType(ServiceSimEvents.Cloudlet_SUBMIT));
                    send(getId(), PolicyConstants.RequestArrivalProcess,ServiceSimEvents.Cloudlet_SUBMIT);

                }else if (type == NetworkConstants.RESPONSE){
                    getReceivedResponsePackets().add(networkPacket);

                    CloudSim.cancelAll(getId(), new PredicateType(ServiceSimEvents.Cloudlet_UPDATE_FOR_RESPONSE));
                    send(getId(), PolicyConstants.ResponseDataArrivalProcess,ServiceSimEvents.Cloudlet_UPDATE_FOR_RESPONSE);

                }else{
                    System.out.println("NETWORKDEVICE ERROR: Unknown Packet.");
                }
            }

        }

    }

    /* generate cloudlet */
    public NetworkCloudlet generateCloudlet(NetworkPacket networkPacket){
        int serviceId = networkPacket.getDestinationServiceId();
        int preServiceId = networkPacket.getSourceServiceId();
        NetworkCloudlet cl = new NetworkCloudlet(NetworkConstants.currentCloudletId++,
                networkPacket.getAppId(),
                0, // Here, cloudletLength is set to 0.
                networkPacket.getServiceChainInfo().getPesNumberList().get(serviceId),
                NetworkConstants.FILE_SIZE,
                NetworkConstants.OUTPUT_SIZE,
                networkPacket.getServiceChainInfo().getMemList().get(serviceId),
                new UtilizationModelFull(),
                new UtilizationModelFull(),
                new UtilizationModelFull()); // solve the serviceId
        cl.setVmId(networkPacket.getDestinationVm());
        cl.setUserId(networkPacket.getUserId());
        // task stages
        ArrayList<ServiceStage> serviceStages =  networkPacket.getServiceChainInfo().getServiceStagesMap().get(serviceId).get(preServiceId);
        for (int i = 0; i < serviceStages.size(); i++){
            TaskStage taskStage = new TaskStage(serviceStages.get(i).getType(), serviceStages.get(i).getStageCloudletLength(), serviceStages.get(i).getData(), serviceStages.get(i).getStageid(), serviceStages.get(i).getPeer());
            cl.stages.add(taskStage);
        }
        return cl;
    }


    public void networkPacketSend(){
        // 1. same level nodes
        for (int key : sendToSameLevelDevicesPktList.keySet()){

            Channel channel = sameLevelDevicesToChannel.get(key);

            Double nextCheckInterval = channel.packetSubmit(getPackets(sendToSameLevelDevicesPktList,key));
            Log.printLine("packet sending time: "+nextCheckInterval);

            if (nextCheckInterval < 0){
                Log.printLine("NETWORKDEVICE ERROR: NextCheckInterval error.");
            }

            send(getId(),nextCheckInterval,ServiceSimEvents.Packet_SEND_CHECK,key);

        }
        sendToSameLevelDevicesPktList.clear();

        // 2. child level nodes
        for (int key : sendToChildDevicesPktList.keySet()){

            Channel channel = childDevicesToChannel.get(key);

            Double nextCheckInterval = channel.packetSubmit(getPackets(sendToChildDevicesPktList,key));

            if (nextCheckInterval < 0){
                System.out.println("NETWORKDEVICE ERROR: NextCheckInterval error.");
            }

            send(getId(),nextCheckInterval,ServiceSimEvents.Packet_SEND_CHECK,key);

        }
        sendToChildDevicesPktList.clear();

        // 3. parent level nodes
        for (int key : sendToParentDevicesPktList.keySet()){

            Channel channel = parentDevicesToChannel.get(key);

            Log.printLine("iiiiiiiii" + channel);

            Double nextCheckInterval = channel.packetSubmit(getPackets(sendToParentDevicesPktList,key));

            if (nextCheckInterval < 0){
                System.out.println("NETWORKDEVICE ERROR: NextCheckInterval error.");
            }

            send(getId(),nextCheckInterval,ServiceSimEvents.Packet_SEND_CHECK,key);

        }
        sendToParentDevicesPktList.clear();
    }

    public void packetSendChecking(SimEvent ev){
        Log.printLine("packet sending check");
        int to = (int) ev.getData();
        Channel channel;
        if (sameLevelDevicesToChannel.containsKey(to)){
            channel = sameLevelDevicesToChannel.get(to);
        }else if (childDevicesToChannel.containsKey(to)){
            channel = childDevicesToChannel.get(to);
        }else{
            channel = parentDevicesToChannel.get(to);
        }

        Double nextCheckInterval = channel.updatePacketSending();

        List<NetworkPacket> networkPackets = channel.getPacketScheduler().getPacketFinishedList();

        if (networkPackets.size() > 0){
            Log.printLine("packet sending has checked");
            send(to, channel.getLatency(), ServiceSimEvents.Packet_ARRIVAL, networkPackets);
            channel.getPacketScheduler().setPacketFinishedList(new ArrayList<>());
        }

        if (nextCheckInterval < 0){
            return;
        }

        send(getId(),nextCheckInterval,ServiceSimEvents.Packet_SEND_CHECK,to);

    }

    public void cloudletSubmit(){
        Log.printLine("ssssss");
        for (int key : instanceIdToCloudlet.keySet()){

            Host host = getVmAllocationPolicy().getHost(key,instanceIdToCloudlet.get(key).get(0).getUserId());
            Vm vm = host.getVm(key,instanceIdToCloudlet.get(key).get(0).getUserId());
            NetworkCloudletScheduler scheduler = (NetworkCloudletScheduler) vm.getCloudletScheduler();

            Log.printLine(CloudSim.clock() + ": " + getName() + ": some cloudlets"
                    + "has arrivalled in Datacenter #" + getId() + " and submitted to vm #" + vm.getId());

            double nextCheckInterval = scheduler.cloudletSubmit(instanceIdToCloudlet.get(key));
            cloudletProcessChecking(vm);

            if (nextCheckInterval > 0 && nextCheckInterval!=Double.MAX_VALUE){
                send(getId(), nextCheckInterval, ServiceSimEvents.Cloudlet_PROCESS_UPDATE,vm);
            }
        }
        instanceIdToCloudlet.clear();
    }

    public void cloudletProcessUpdate(Vm vm){
        double nextCheckInterval = vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
                .getAllocatedMipsForVm(vm));
        if (nextCheckInterval > 0 && nextCheckInterval!=Double.MAX_VALUE){
            send(getId(), nextCheckInterval, ServiceSimEvents.Cloudlet_PROCESS_UPDATE,vm);
        }
    }

    public void cloudletProcessChecking(Vm vm){

        NetworkCloudletScheduler networkCloudletScheduler = (NetworkCloudletScheduler) vm.getCloudletScheduler();
        // check send packets
        ArrayList<NetworkPacket> sendToSelf = new ArrayList<>();
        for (int key : networkCloudletScheduler.getPkttosend().keySet()){
            NetworkPacket cld2Packet = cloudletIdToPacket.get(key);
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
                    int destination = getRequestDispatchingRule().findDeviceId(networkPacket,serviceDiscovery,getId(),childDeviceIds,parentDeviceIds,sameLevelDeviceIds);
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
            NetworkPacket networkPacket = cloudletIdToPacket.get(id);
            cloudletIdToPacket.remove(id);
            Pair<Cloudlet, NetworkPacket> data= new Pair<>(networkCloudletScheduler.getCloudletFinishedList().get(i).getCloudlet(),networkPacket);
            sendNow(networkCloudletScheduler.getCloudletFinishedList().get(i).getUserId(), CloudSimTags.CLOUDLET_RETURN, data);
        }
        networkCloudletScheduler.getCloudletFinishedList().clear();

        // check executed cloudlets
        if (getVmsNeedToDestroy().keySet().contains(vm.getId())){
            if (networkCloudletScheduler.getCloudletExecList().size() == 0){
                doVmDestroy(vm, getVmsNeedToDestroy().get(vm.getId()));
                getVmsNeedToDestroy().remove(vm.getId());
            }
        }

    }

    public void processCloudletUpdateForResponse(){

        Map<Integer,List<NetworkPacket>> networkPacketMap = new HashMap<>();
        for (NetworkPacket networkPacket : getReceivedResponsePackets()) {
            networkPacket.setRecvTime(CloudSim.clock());
            if (networkPacketMap.get(networkPacket.getDestinationVm()) == null) {
                networkPacketMap.put(networkPacket.getDestinationVm(), new ArrayList<>());
            }
            networkPacketMap.get(networkPacket.getDestinationVm()).add(networkPacket);
        }
        getReceivedResponsePackets().clear();

        for (int instanceId : networkPacketMap.keySet()){
            int userId = networkPacketMap.get(instanceId).get(0).getUserId();
            Host host = getVmAllocationPolicy().getHost(instanceId, userId);
            Vm vm = host.getVm(instanceId, userId);
            NetworkCloudletScheduler scheduler = (NetworkCloudletScheduler) vm.getCloudletScheduler();
            for (NetworkPacket networkPacket : networkPacketMap.get(instanceId)) {
                if (scheduler.getPktrecv().get(networkPacket.getDestinationCloudlet()) == null) {
                    scheduler.getPktrecv().put(networkPacket.getDestinationCloudlet(), new ArrayList<>());
                }
                scheduler.getPktrecv().get(networkPacket.getDestinationCloudlet()).add(networkPacket);
            }
            cloudletProcessUpdate(vm);
            cloudletProcessChecking(vm);
        }


    }

    public void addPacketToSendMap(int nexthop, NetworkPacket networkPacket){

        if (sameLevelDeviceIds.contains(nexthop)){

            if (!sendToSameLevelDevicesPktList.containsKey(nexthop)){
                sendToSameLevelDevicesPktList.put(nexthop, new ArrayList<>());
            }
            getPackets(sendToSameLevelDevicesPktList, nexthop).add(networkPacket);

        }else if (childDeviceIds.contains(nexthop)){

            if (!sendToChildDevicesPktList.containsKey(nexthop)){
                sendToChildDevicesPktList.put(nexthop, new ArrayList<>());
            }
            getPackets(sendToChildDevicesPktList, nexthop).add(networkPacket);

        }else{

            if (!sendToParentDevicesPktList.containsKey(nexthop)){
                sendToParentDevicesPktList.put(nexthop, new ArrayList<>());
            }
            getPackets(sendToParentDevicesPktList, nexthop).add(networkPacket);

        }
    }

    /* vms created */
    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
        MicroserviceInstance instance = (MicroserviceInstance) ev.getData();
        double delayInStartUp = instance.getDelayInStartUp();

        boolean result = getVmAllocationPolicy().allocateHostForVm(instance);

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = instance.getId();

            if (result) {
                data[2] = CloudSimTags.TRUE;
                send(instance.getUserId(), delayInStartUp, CloudSimTags.VM_CREATE_ACK, data);
            } else {
                data[2] = CloudSimTags.FALSE;
                sendNow(instance.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
            }
        }

        if (result) {
            getVmList().add(instance);

            if (instance.isBeingInstantiated()) {
                instance.setBeingInstantiated(false);
            }
            instance.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(instance).getVmScheduler()
                    .getAllocatedMipsForVm(instance));
            Pair<Integer,MicroserviceInstance> data = new Pair<>(getId(),instance);
            send(getId(),delayInStartUp,ServiceSimEvents.Service_DISCOVERY_ADD,data);
        }
    }

    /* vms destroyed */
    @Override
    protected void processVmDestroy(SimEvent ev, boolean ack) {
        MicroserviceInstance instance = (MicroserviceInstance) ev.getData();
        Pair<Integer,MicroserviceInstance> data = new Pair<>(getId(),instance);
        sendNow(getId(),ServiceSimEvents.Service_DISCOVERY_DEL,data);

        if (ack) {
            getVmsNeedToDestroy().put(instance.getId(),true);
        }else{
            getVmsNeedToDestroy().put(instance.getId(),false);
        }
    }

    public void doVmDestroy(Vm vm, boolean ack){

        getVmAllocationPolicy().deallocateHostForVm(vm);

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = vm.getId();
            data[2] = CloudSimTags.TRUE;

            sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, data);
        }
        getVmList().remove(vm);
    }

    /* service discovery information update */

    public void serviceRegistration(SimEvent ev){
        Pair<Integer,MicroserviceInstance> data = (Pair<Integer,MicroserviceInstance>) ev.getData();
        int from = data.getKey();
        MicroserviceInstance instance = (MicroserviceInstance) data.getValue();
        getServiceDiscovery().addServiceDiscoveryInfo(instance);
        // cascade
        if (from == getId()){
            for (int i = 0; i < getRequestDispatchingRule().getNetworkDevices().size();i++){
                if (getRequestDispatchingRule().getNetworkDevices().get(i).getId()!=getId()){
                    Pair<Integer,MicroserviceInstance> data1 = new Pair<>(getId(),instance);
                    sendNow(getRequestDispatchingRule().getNetworkDevices().get(i).getId(),ServiceSimEvents.Service_DISCOVERY_ADD,data1);
                }
            }
        }

    }

    public void serviceCancellation(SimEvent ev){
        Pair<Integer,MicroserviceInstance> data = (Pair<Integer,MicroserviceInstance>) ev.getData();
        int from = data.getKey();
        MicroserviceInstance instance = (MicroserviceInstance) data.getValue();
        getServiceDiscovery().removeServiceDiscoveryInfo(instance);
        // cascade
        if (from == getId()){
            for (int i = 0; i < getRequestDispatchingRule().getNetworkDevices().size();i++){
                if (getRequestDispatchingRule().getNetworkDevices().get(i).getId()!=getId()){
                    Pair<Integer,MicroserviceInstance> data1 = new Pair<>(getId(),instance);
                    sendNow(getRequestDispatchingRule().getNetworkDevices().get(i).getId(),ServiceSimEvents.Service_DISCOVERY_DEL,data1);
                }
            }
        }

    }


//    public <T extends NetworkPacket> List<T> getPackets(Map<Integer, List<? extends NetworkPacket>> devicesToPackets, int key){
//        return (List<T>) devicesToPackets.get(key);
//    }

    public ArrayList<NetworkPacket> getPackets(Map<Integer, ArrayList<NetworkPacket>> devicesToPackets, int key){
        return devicesToPackets.get(key);
    }




    /* getter and setter */
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getIdentify() {
        return identify;
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public ArrayList<Integer> getChildDeviceIds() {
        return childDeviceIds;
    }

    public void setChildDeviceIds(ArrayList<Integer> childDeviceIds) {
        this.childDeviceIds = childDeviceIds;
    }

    public ArrayList<Integer> getParentDeviceIds() {
        return parentDeviceIds;
    }

    public void setParentDeviceIds(ArrayList<Integer> parentDeviceIds) {
        this.parentDeviceIds = parentDeviceIds;
    }

    public ArrayList<Integer> getSameLevelDeviceIds() {
        return sameLevelDeviceIds;
    }

    public void setSameLevelDeviceIds(ArrayList<Integer> sameLevelDeviceIds) {
        this.sameLevelDeviceIds = sameLevelDeviceIds;
    }

    public GeoCoverage getGeoCoverage() {
        return geoCoverage;
    }

    public void setGeoCoverage(GeoCoverage geoCoverage) {
        this.geoCoverage = geoCoverage;
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public RequestDispatchingRule getRequestDispatchingRule() {
        return requestDispatchingRule;
    }

    public void setRequestDispatchingRule(RequestDispatchingRule requestDispatchingRule) {
        this.requestDispatchingRule = requestDispatchingRule;
    }

    public LoadAdmission getLoadAdmission() {
        return loadAdmission;
    }

    public void setLoadAdmission(LoadAdmission loadAdmission) {
        this.loadAdmission = loadAdmission;
    }

//    public <T extends NetworkPacket> List<T> getReceiveResponsePackets(){
//        return (List<T>) receivedResponsePackets;
//    }
//    protected <T extends NetworkPacket> void setReceiveResponsePackets(List<T> receivedResponsePackets) {
//        this.receivedResponsePackets = receivedResponsePackets;
//    }
//
//    public Map<Integer, List<? extends NetworkPacket>> getSendToSameLevelDevicesPktList() {
//        return sendToSameLevelDevicesPktList;
//    }
//
//    public void setSendToSameLevelDevicesPktList(Map<Integer, List<? extends NetworkPacket>> sendToSameLevelDevicesPktList) {
//        this.sendToSameLevelDevicesPktList = sendToSameLevelDevicesPktList;
//    }
//
//    public Map<Integer, List<? extends NetworkPacket>> getSendToChildDevicesPktList() {
//        return sendToChildDevicesPktList;
//    }
//
//    public void setSendToChildDevicesPktList(Map<Integer, List<? extends NetworkPacket>> sendToChildDevicesPktList) {
//        this.sendToChildDevicesPktList = sendToChildDevicesPktList;
//    }
//
//    public Map<Integer, List<? extends NetworkPacket>> getSendToParentDevicesPktList() {
//        return sendToParentDevicesPktList;
//    }
//
//    public void setSendToParentDevicesPktList(Map<Integer, List<? extends NetworkPacket>> sendToParentDevicesPktList) {
//        this.sendToParentDevicesPktList = sendToParentDevicesPktList;
//    }

    public Map<Integer, Channel> getSameLevelDevicesToChannel() {
        return sameLevelDevicesToChannel;
    }

    public void setSameLevelDevicesToChannel(Map<Integer, Channel> sameLevelDevicesToChannel) {
        this.sameLevelDevicesToChannel = sameLevelDevicesToChannel;
    }

    public Map<Integer, Channel> getParentDevicesToChannel() {
        return parentDevicesToChannel;
    }

    public void setParentDevicesToChannel(Map<Integer, Channel> parentDevicesToChannel) {
        this.parentDevicesToChannel = parentDevicesToChannel;
    }

    public Map<Integer, Channel> getChildDevicesToChannel() {
        return childDevicesToChannel;
    }

    public void setChildDevicesToChannel(Map<Integer, Channel> childDevicesToChannel) {
        this.childDevicesToChannel = childDevicesToChannel;
    }

    public Map<Integer, Boolean> getVmsNeedToDestroy() {
        return vmsNeedToDestroy;
    }

    public void setVmsNeedToDestroy(Map<Integer, Boolean> vmsNeedToDestroy) {
        this.vmsNeedToDestroy = vmsNeedToDestroy;
    }

    public Map<Integer, Map<Integer, Integer>> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(Map<Integer, Map<Integer, Integer>> routingTable) {
        this.routingTable = routingTable;
    }

    public ArrayList<NetworkPacket> getReceivedResponsePackets() {
        return receivedResponsePackets;
    }

    public void setReceivedResponsePackets(ArrayList<NetworkPacket> receivedResponsePackets) {
        this.receivedResponsePackets = receivedResponsePackets;
    }

    public Map<Integer, ArrayList<NetworkPacket>> getSendToSameLevelDevicesPktList() {
        return sendToSameLevelDevicesPktList;
    }

    public void setSendToSameLevelDevicesPktList(Map<Integer, ArrayList<NetworkPacket>> sendToSameLevelDevicesPktList) {
        this.sendToSameLevelDevicesPktList = sendToSameLevelDevicesPktList;
    }

    public Map<Integer, ArrayList<NetworkPacket>> getSendToChildDevicesPktList() {
        return sendToChildDevicesPktList;
    }

    public void setSendToChildDevicesPktList(Map<Integer, ArrayList<NetworkPacket>> sendToChildDevicesPktList) {
        this.sendToChildDevicesPktList = sendToChildDevicesPktList;
    }

    public Map<Integer, ArrayList<NetworkPacket>> getSendToParentDevicesPktList() {
        return sendToParentDevicesPktList;
    }

    public void setSendToParentDevicesPktList(Map<Integer, ArrayList<NetworkPacket>> sendToParentDevicesPktList) {
        this.sendToParentDevicesPktList = sendToParentDevicesPktList;
    }

    public Map<Integer, ArrayList<NetworkCloudlet>> getInstanceIdToCloudlet() {
        return instanceIdToCloudlet;
    }

    public void setInstanceIdToCloudlet(Map<Integer, ArrayList<NetworkCloudlet>> instanceIdToCloudlet) {
        this.instanceIdToCloudlet = instanceIdToCloudlet;
    }

    public Map<Integer, NetworkPacket> getCloudletIdToPacket() {
        return cloudletIdToPacket;
    }

    public void setCloudletIdToPacket(Map<Integer, NetworkPacket> cloudletIdToPacket) {
        this.cloudletIdToPacket = cloudletIdToPacket;
    }


}
