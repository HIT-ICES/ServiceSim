package org.serviceProvider;

import javafx.util.Pair;
import org.cloudbus.cloudsim.CloudSimTags;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.lists.VmList;
import org.enduser.networkPacket.NetworkCloudlet;
import org.enduser.networkPacket.NetworkPacket;
import org.enduser.networkPacket.TaskStage;
import org.infrastructureProvider.DevicesProvider;
import org.infrastructureProvider.entities.Vm;
import org.infrastructureProvider.policies.CloudletScheduler;
import org.infrastructureProvider.policies.NetworkCloudletTimeSharedScheduler;
import org.infrastructureProvider.policies.NetworkCloudletTimeSharedSchedulerWithLimit;
import org.infrastructureProvider.policies.NetworkCloudletTimeSharedSchedulerWithShare;
import org.serviceProvider.capacities.LoadAdmission;
import org.serviceProvider.capacities.LoadBalance;
import org.serviceProvider.capacities.RequestDispatchingRule;
import org.serviceProvider.services.MicroserviceInstance;
import org.serviceProvider.services.Servicechain;
import org.utils.Location;
import org.utils.PolicyConstants;
import org.utils.ServiceSimEvents;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServiceProvider extends DatacenterBroker {

    private List<Servicechain> serviceChain;

    private DevicesProvider devicesProvider;

    private Map<Integer, Map<Integer, Map<Integer,Integer>>> initInstance;

    private String appCloudletResultFile;
    private String cloudletExeDetailFile;
    private String cloudletStageDetailFile;

    // failed created vms

    /** The vms list. */
    protected List<? extends Vm> vmsCreateFailedList;
    protected List<? extends Vm> vmsDestroyFailedList;
    protected List<? extends Vm> vmsDestroyedList;

    // record

    private Map<Integer,ArrayList<NetworkPacket>> endUserRequest;



    /**
     * Created a new DatacenterBroker object.
     *
     * @param name name to be associated with this entity (as required by Sim_entity class from
     *             simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public ServiceProvider(String name,
                           List<Servicechain> serviceChain,
                           DevicesProvider devicesProvider,
                           Map<Integer, LoadAdmission> initLoadAdmission,
                           Map<Integer, LoadBalance> initLoadBalance,
                           Map<Integer, RequestDispatchingRule> initRequestDispatching,
                           Map<Integer, Map<Integer, Map<Integer,Integer>>> initInstance,
                           String[] cloudletResultFile) throws Exception {
        super(name);
        this.serviceChain = serviceChain;
        this.devicesProvider = devicesProvider;

        setVmsCreateFailedList(new ArrayList<Vm>());
        setVmsDestroyFailedList(new ArrayList<Vm>());
        setVmsDestroyedList(new ArrayList<Vm>());

        endUserRequest = new HashMap<>();

        initExecutorDeployment(initLoadAdmission, initLoadBalance, initRequestDispatching);
        this.initInstance = initInstance;

        this.appCloudletResultFile = cloudletResultFile[0];
        this.cloudletExeDetailFile = cloudletResultFile[1];
        this.cloudletStageDetailFile = cloudletResultFile[2];

    }

    @Override
    public void startEntity() {
        Log.printLine(getName() + " is starting...");
        // Map<Integer, Map<Integer, Map<Integer,Integer>>> instances; <device id -> <service id -> <type id -> num>>>
        createInstancesInDevices(initInstance);
    }

    @Override
    public void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case ServiceSimEvents.LoadAdmission_ALTER:
                Map<Integer, LoadAdmission> deviceId2Executor = (Map<Integer, LoadAdmission>) ev.getData();
                processLoadAdmissionAlter(deviceId2Executor);
                break;
            case ServiceSimEvents.LoadBalance_ALTER:
                Map<Integer, LoadBalance> deviceId2Executor1 = (Map<Integer, LoadBalance>) ev.getData();
                processLoadBalanceAlter(deviceId2Executor1);
                break;
            case ServiceSimEvents.RequestDispatching_ALTER:
                Map<Integer, RequestDispatchingRule> deviceId2Executor2 = (Map<Integer, RequestDispatchingRule>) ev.getData();
                processRequestDispatchingAlter(deviceId2Executor2);
                break;
            case ServiceSimEvents.Instance_GREATE:
                Map<Integer, Map<Integer, Map<Integer,Integer>>> instancesToCreate = (Map<Integer, Map<Integer, Map<Integer,Integer>>>) ev.getData();
                createInstancesInDevices(instancesToCreate);
                break;
            case ServiceSimEvents.Instance_DESTROY:
                ArrayList<Vm> instancesToDestroy = (ArrayList<Vm>) ev.getData();
                destroyInstances(instancesToDestroy);
                break;
            case CloudSimTags.VM_DESTROY_ACK:
                processVmDestroy(ev);
                break;
            case ServiceSimEvents.EndUserRequest_ARRIVAL:
                processEndUserRequestArrival(ev);
                break;
            case ServiceSimEvents.EndUserRequest_SEND:
                sendEndUserRequest();
                break;
            default:
                super.processOtherEvent(ev);
                break;
        }
    }

    @Override
    public void shutdownEntity() {

    }

    public void initExecutorDeployment(Map<Integer, LoadAdmission> initLoadAdmission,
                                       Map<Integer, LoadBalance> initLoadBalance,
                                       Map<Integer, RequestDispatchingRule> RequestDispatching){
        processLoadAdmissionAlter(initLoadAdmission);
        processLoadBalanceAlter(initLoadBalance);
        processRequestDispatchingAlter(RequestDispatching);
    }


    public void processLoadAdmissionAlter(Map<Integer, LoadAdmission> deviceId2Executor){

        for (Integer key : deviceId2Executor.keySet()){
            int index = findDevices(key);
            if (index >= 0){
                devicesProvider.getDevices().get(index).setLoadAdmission(deviceId2Executor.get(key));
            }
        }
    }

    public void processLoadBalanceAlter(Map<Integer, LoadBalance> deviceId2Executor){
        for (Integer key : deviceId2Executor.keySet()){
            int index = findDevices(key);
            if (index >= 0){
                devicesProvider.getDevices().get(index).setLoadBalance(deviceId2Executor.get(key));
            }
        }
    }

    public void processRequestDispatchingAlter(Map<Integer, RequestDispatchingRule> deviceId2Executor){

        for (Integer key : deviceId2Executor.keySet()){
            int index = findDevices(key);
            if (index >= 0){
                devicesProvider.getDevices().get(index).setRequestDispatchingRule(deviceId2Executor.get(key));
            }
        }
    }

    /* vms created */
    public void createInstancesInDevices(Map<Integer, Map<Integer, Map<Integer,Integer>>> instances){
        Map<Integer, List<MicroserviceInstance>> microserviceInstances = new HashMap<>();
        for (int deviceId : instances.keySet()){
            microserviceInstances.put(deviceId, new ArrayList<>());
            for (int serviceId : instances.get(deviceId).keySet()){
                for (int type : instances.get(deviceId).get(serviceId).keySet()){
                    for (int i = 0;i<instances.get(deviceId).get(serviceId).get(type);i++){
                        if (serviceId == 0){
                            microserviceInstances.get(deviceId).add(instancePreparation(type,serviceId));// type represent service chain id
                        }else{
                            microserviceInstances.get(deviceId).add(instancePreparation(type,serviceId));
                        }
                    }
                }
            }
        }

        for (int key : microserviceInstances.keySet()){

            for (int i = 0;i<microserviceInstances.get(key).size();i++){
                getVmList().add(microserviceInstances.get(key).get(i));
                sendNow(key, CloudSimTags.VM_CREATE_ACK, microserviceInstances.get(key).get(i));
            }
        }
    }

    public MicroserviceInstance instancePreparation(int configurationType, int serviceId){
        //VM configuration
//        int serviceChainId = configurationType;
//        if (serviceId == 0){
//            configurationType = 0;
//        }
        double mips = PolicyConstants.VM_MIPS[configurationType];
        int pesNumber = PolicyConstants.VM_PES[configurationType];
        int ram = PolicyConstants.VM_RAM[configurationType];
        double delayInStartUp = PolicyConstants.VM_DELAY[configurationType];
        long bw = PolicyConstants.VM_BW;
        long size = PolicyConstants.VM_SIZE;
        String vmm = "Xen";
        double requestTime = CloudSim.clock();

        // init vms deployment
        if (requestTime == 0.0){
            requestTime = requestTime - delayInStartUp;
            delayInStartUp = 0;
        }

        CloudletScheduler cloudletScheduler = new NetworkCloudletTimeSharedScheduler();
        if (PolicyConstants.VM_CldScheduler == PolicyConstants.TimeShared){
            cloudletScheduler = new NetworkCloudletTimeSharedScheduler();
        }else if(PolicyConstants.VM_CldScheduler == PolicyConstants.TimeSharedWithLimit){
            cloudletScheduler = new NetworkCloudletTimeSharedSchedulerWithLimit(20);
        }else if (PolicyConstants.VM_CldScheduler == PolicyConstants.TimeSharedWithFixShare){
            cloudletScheduler = new NetworkCloudletTimeSharedSchedulerWithShare(10);

        }
        if (serviceId == 0){
            cloudletScheduler = new NetworkCloudletTimeSharedScheduler();
        }
        // purchase type is not used.
        MicroserviceInstance instance = new MicroserviceInstance(PolicyConstants.vmIdNum,getId(),mips,pesNumber,ram,bw,size,vmm,cloudletScheduler,requestTime,configurationType,0,delayInStartUp,serviceId);
//        if (serviceId == 0){
//            instance.serviceChainId = serviceChainId;
//        }
        PolicyConstants.vmIdNum++;
        return instance;
    }

    @Override
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
            Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
                    + " has been created in Datacenter #" + datacenterId + ", Host #"
                    + VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
        } else {
            getVmsCreateFailedList().add(VmList.getById(getVmList(), vmId));
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
                    + " failed in Datacenter #" + datacenterId);
        }

    }

    /* vms destroy */
    public void destroyInstances(ArrayList<Vm> instancesToDestroy){

        for (Vm vm : instancesToDestroy){
            int deviceId = getVmsToDatacentersMap().get(vm.getId());
            sendNow(deviceId,CloudSimTags.VM_DESTROY_ACK,vm);
        }

    }

    public void processVmDestroy(SimEvent ev){
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];
        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().remove(vmId);
            getVmsDestroyedList().add(VmList.getById(getVmList(), vmId));
            Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
                    + " has been destroyed in Datacenter #" + datacenterId + ", Host #"
                    + VmList.getById(getVmsDestroyedList(), vmId).getHost().getId());
        } else {
            getVmsDestroyFailedList().add(VmList.getById(getVmList(), vmId));
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
                    + " failed in Datacenter #" + datacenterId);
        }
    }

    public void processEndUserRequestArrival(SimEvent ev){
        Pair<Location, NetworkPacket> data = (Pair<Location, NetworkPacket>) ev.getData();
        Location location = data.getKey();
        NetworkPacket networkPacket = data.getValue();
        int deviceId = findDispatchAccessPoint(location);
        if (deviceId<0){
            Log.printLine("Error: can not find an access point for endUser request!");
        }
        networkPacket.setDestination(deviceId);
        int index = findServiceChain(networkPacket.getServiceChainInfo().getServiceChainId());
        networkPacket.setServiceChainInfo(serviceChain.get(index));
        networkPacket.setDestinationServiceId(0); // gateway service
        networkPacket.setSourceServiceId(-1);
        if (!endUserRequest.containsKey(deviceId)){
            endUserRequest.put(deviceId,new ArrayList<>());
        }
        endUserRequest.get(deviceId).add(networkPacket);
        CloudSim.cancelAll(getId(), new PredicateType(ServiceSimEvents.EndUserRequest_SEND));
        sendNow(getId(), ServiceSimEvents.EndUserRequest_SEND);
    }

    public void sendEndUserRequest(){
        for (int key : endUserRequest.keySet()){
            for (NetworkPacket networkPacket : endUserRequest.get(key)){
                Log.printLine(CloudSim.clock() + ": " + getName() + ": endUser request #" + networkPacket.getAppId()
                        + " has arrivalled");
            }
            sendNow(key,ServiceSimEvents.Packet_ARRIVAL,endUserRequest.get(key));
        }
        endUserRequest.clear();
    }

    public int findDispatchAccessPoint(Location location){
        if (location.getLatitude() == -1 || location.getLongitude() == -1){
            // get device id according to marked block num
            for (int i = 0; i < devicesProvider.getDevices().size(); i++){
                if (devicesProvider.getDevices().get(i).getLocation().getBlock() == location.getBlock()){
                    return devicesProvider.getDevices().get(i).getId();
                }
            }
        }
        // else : get device id according to location distance
        // @todo
        return -1;
    }

    // return index
    public int findDevices(int deviceId){
        for (int i = 0;i < devicesProvider.getDevices().size(); i++){
            if (devicesProvider.getDevices().get(i).getId() == deviceId){
                return i;
            }
        }
        return -1;
    }

    public int findServiceChain(int serviceChainId){
        for (int i = 0;i < serviceChain.size(); i++){
            if (serviceChain.get(i).getServiceChainId() == serviceChainId){
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        Pair<NetworkCloudlet, NetworkPacket> data= (Pair<NetworkCloudlet, NetworkPacket>) ev.getData();

        NetworkPacket networkPacket = data.getValue();
        NetworkCloudlet cloudlet = (NetworkCloudlet) data.getKey();
        MicroserviceInstance vm = VmList.getById(getVmsCreatedList(),cloudlet.getVmId());
        writeNetworkCloudletResult(cloudletExeDetailFile, cloudlet, vm,networkPacket);
        writeNetworkCloudletStages(cloudletStageDetailFile, cloudlet,vm,networkPacket);

        if (vm.getServiceId() == 0){// gateway

            writeAppCloudletResult(appCloudletResultFile, cloudlet,vm,networkPacket);
        }

    }

    protected void writeNetworkCloudletResult(String fileName, NetworkCloudlet cloudlet, MicroserviceInstance vm,NetworkPacket networkPacket){
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(fileName,true);
            //fileWriter.append('\n');
            fileWriter.append(String.valueOf(cloudlet.appId));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(cloudlet.getCloudletId()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(networkPacket.getDestination()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(vm.getHost().getId()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(vm.getId()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(vm.getServiceId()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(cloudlet.getExecStartTime()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(CloudSim.clock()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(CloudSim.clock() - cloudlet.getExecStartTime()));
            fileWriter.append('\n');

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeNetworkCloudletStages(String fileName, NetworkCloudlet cloudlet, MicroserviceInstance vm,NetworkPacket networkPacket){
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(fileName,true);
            //fileWriter.append('\n');
            for (TaskStage taskStage : cloudlet.stages){
                fileWriter.append(String.valueOf(cloudlet.appId));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(cloudlet.getCloudletId()));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(networkPacket.getDestination()));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(vm.getHost().getId()));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(vm.getId()));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(vm.getServiceId()));
                fileWriter.append(',');
                String typeName = "un-known";
                int type = taskStage.getType();
                if (type == 0){
                    typeName = "EXECUTION";
                }else if(type == 1){
                    typeName ="WAIT_SEND";
                }else if (type == 2){
                    typeName ="WAIT_RECV";
                }
                fileWriter.append(typeName);
                fileWriter.append(',');
                fileWriter.append(String.valueOf(taskStage.getProcessStartTime()));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(taskStage.getTime()));
                fileWriter.append('\n');

            }

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // mai: write Result to the file
    protected void writeAppCloudletResult(String fileName, NetworkCloudlet cloudlet, MicroserviceInstance vm,NetworkPacket networkPacket){
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(fileName,true);
            //fileWriter.append('\n');
            fileWriter.append(String.valueOf(cloudlet.getAppId()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(networkPacket.getServiceChainInfo().getServiceChainId()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(networkPacket.getEndUserInfo().getEnduserId()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(networkPacket.getEndUserInfo().getUserLevel()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(networkPacket.getDestination()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(cloudlet.getExecStartTime()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(CloudSim.clock()));
            fileWriter.append(',');
            fileWriter.append(String.valueOf(CloudSim.clock() - cloudlet.getExecStartTime()));
            fileWriter.append('\n');

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Servicechain> getServiceChain() {
        return serviceChain;
    }

    public void setServiceChain(List<Servicechain> serviceChain) {
        this.serviceChain = serviceChain;
    }

    public DevicesProvider getDevicesProvider() {
        return devicesProvider;
    }

    public void setDevicesProvider(DevicesProvider devicesProvider) {
        this.devicesProvider = devicesProvider;
    }

    @SuppressWarnings("unchecked")
    public <T extends Vm> List<T> getVmsCreateFailedList() {
        return (List<T>) vmsCreateFailedList;
    }

    /**
     * Sets the vm list.
     *
     * @param <T> the generic type
     * @param vmsCreateFailedList the vms failed list
     */
    protected <T extends Vm> void setVmsCreateFailedList(List<T> vmsCreateFailedList) {
        this.vmsCreateFailedList = vmsCreateFailedList;
    }

    @SuppressWarnings("unchecked")
    public <T extends Vm> List<T> getVmsDestroyFailedList() {
        return (List<T>) vmsDestroyFailedList;
    }

    /**
     * Sets the vm list.
     *
     * @param <T> the generic type
     * @param vmsDestroyFailedList the vms failed list
     */
    protected <T extends Vm> void setVmsDestroyFailedList(List<T> vmsDestroyFailedList) {
        this.vmsDestroyFailedList = vmsDestroyFailedList;
    }

    @SuppressWarnings("unchecked")
    public <T extends Vm> List<T> getVmsDestroyedList() {
        return (List<T>) vmsDestroyedList;
    }

    /**
     * Sets the vm list.
     *
     * @param <T> the generic type
     * @param vmsDestroyedList the vms failed list
     */
    protected <T extends Vm> void setVmsDestroyedList(List<T> vmsDestroyedList) {
        this.vmsDestroyedList = vmsDestroyedList;
    }

}
