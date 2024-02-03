package org.test;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.EndUser;
import org.enduser.networkPacket.NetworkConstants;
import org.infrastructureProvider.DevicesProvider;
import org.infrastructureProvider.DevicesProviderSimple;
import org.infrastructureProvider.entities.NetworkDevice;
import org.serviceProvider.ServiceProvider;
import org.serviceProvider.capacities.*;
import org.serviceProvider.services.ServiceStage;
import org.serviceProvider.services.Servicechain;

import java.util.*;

public class TestExample {
    public static void main(String[] args) {
        Log.printLine("Starting ServiceSim Simulation...");

        try {
            // Default Setup for CloudSim
            cloudSimSetup();

            int experimentNum = 1;

            // simulation limit
            final double SIMULATION_LIMIT = 60;
            final String endUserSimuFile = "workloadTest1.csv";

            String workloadResult = "src//others//results//workloadResult" + String.valueOf(experimentNum) + ".csv";
            String cloudletExeDetail = "src//others//results//cloudletExeDetail" + String.valueOf(experimentNum) + ".csv";
            String cloudletStageDetail = "src//others//results//cloudletStagesDetail" + String.valueOf(experimentNum) + ".csv";

            String[] cloudletResultFile = new String[]{workloadResult,cloudletExeDetail,cloudletStageDetail};

            DevicesProvider devicesProvider = new DevicesProviderSimple();

            List<Servicechain> serviceChain = createdServicechains();

            Map<Integer, LoadAdmission> initLoadAdmission = new HashMap<>();
            Map<Integer, RequestDispatchingRule> initRequestDispatching = new HashMap<>();
            Map<Integer, LoadBalance> initLoadBalance = new HashMap<>();

            for (NetworkDevice device : devicesProvider.getDevices()){
                initLoadAdmission.put(device.getId(),new NonLoadAdmission());
                initLoadBalance.put(device.getId(),new RoundRobin());
            }
            ArrayList<NetworkDevice> networkDevices0 = new ArrayList<>();
            networkDevices0.add(devicesProvider.getDevices().get(0));
            networkDevices0.add(devicesProvider.getDevices().get(1));

            ArrayList<NetworkDevice> networkDevices1 = new ArrayList<>();
            networkDevices1.add(devicesProvider.getDevices().get(1));

            ArrayList<NetworkDevice> networkDevices2 = new ArrayList<>();
            networkDevices1.add(devicesProvider.getDevices().get(2));

            ArrayList<NetworkDevice> networkDevices3 = new ArrayList<>();
            networkDevices1.add(devicesProvider.getDevices().get(3));

            initRequestDispatching.put(devicesProvider.getDevices().get(0).getId(),new RequestDispatchingSimple(networkDevices0));
            initRequestDispatching.put(devicesProvider.getDevices().get(1).getId(),new RequestDispatchingSimple(networkDevices1));
            initRequestDispatching.put(devicesProvider.getDevices().get(2).getId(),new RequestDispatchingSimple(networkDevices2));
            initRequestDispatching.put(devicesProvider.getDevices().get(3).getId(),new RequestDispatchingSimple(networkDevices3));


            Map<Integer, Map<Integer, Map<Integer,Integer>>> initInstance = new HashMap<>();
            Map<Integer, Map<Integer,Integer>> serviceToNum = new HashMap<>();
            Map<Integer, Map<Integer,Integer>> serviceToNum1 = new HashMap<>();
            Map<Integer,Integer>  typeToNum0 = new HashMap<>();
            typeToNum0.put(0,1);
            Map<Integer,Integer>  typeToNum1 = new HashMap<>();
            typeToNum1.put(0,2);
            Map<Integer,Integer>  typeToNum2 = new HashMap<>();
            typeToNum2.put(0,4);
            serviceToNum.put(0,typeToNum0);
            serviceToNum.put(1,typeToNum1);
            serviceToNum.put(2,typeToNum1);
            serviceToNum.put(3,typeToNum2);
            serviceToNum.put(4,typeToNum1);
            serviceToNum.put(5,typeToNum1);

            serviceToNum1.put(0,typeToNum0);
            initInstance.put(devicesProvider.getDevices().get(0).getId(),serviceToNum);
            initInstance.put(devicesProvider.getDevices().get(1).getId(),serviceToNum1);
            initInstance.put(devicesProvider.getDevices().get(2).getId(),serviceToNum);
            initInstance.put(devicesProvider.getDevices().get(3).getId(),serviceToNum);

            ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",serviceChain,devicesProvider,initLoadAdmission,initLoadBalance,initRequestDispatching,initInstance,cloudletResultFile);
            EndUser endUser = new EndUser("endUser",endUserSimuFile, serviceProvider.getId(),
                    SIMULATION_LIMIT);

            //Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }
    /**
     * Sets basic parameters for simulation
     */
    private static void cloudSimSetup(){
        // First step: Initialize the CloudSim package. It should be called before creating any entities.
        int num_BSP = 1;   // The number of Application Service Providers
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;  // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_BSP, calendar, trace_flag);
    }

    /**
     * Sets service chains
     */
    private static List<Servicechain> createdServicechains(){

        ArrayList<Servicechain> servicechains = new ArrayList<>();
        /* create the first service chain */
        Servicechain servicechain1 = new Servicechain(0);
        servicechain1.getMicroserIds().add(0);
        servicechain1.getMicroserIds().add(1);
        servicechain1.getMicroserIds().add(2);
        servicechain1.getMicroserIds().add(3);

        /* services cloudletLength*/
        double a = 0.2; // 0.2 MI
        double b = 0.4; // 0.3 MI
        double data = 1000;
        servicechain1.getCloudletLengthList().put(0,0.0);
        servicechain1.getCloudletLengthList().put(1,a*2);
        servicechain1.getCloudletLengthList().put(2,b);
        servicechain1.getCloudletLengthList().put(3,b); //service 3 function1

        servicechain1.getPesNumberList().put(0,1);
        servicechain1.getPesNumberList().put(1,1);
        servicechain1.getPesNumberList().put(2,1);
        servicechain1.getPesNumberList().put(3,1);

        servicechain1.getMemList().put(0,10);
        servicechain1.getMemList().put(1,100);
        servicechain1.getMemList().put(2,100);
        servicechain1.getMemList().put(3,100);

        /* service1 stage */
        ArrayList<ServiceStage> Service0Stages = new ArrayList<ServiceStage>();
        Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,0,1,0,data));
        Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV,1,1,0,data));
        //servicechain1.getServiceStageMap().put(0,Service0Stages);

        Map<Integer, ArrayList<ServiceStage>> service0Stages = new HashMap<>();
        service0Stages.put(-1,Service0Stages);
        servicechain1.getServiceStagesMap().put(0,service0Stages);

        ArrayList<ServiceStage> Service1Stages = new ArrayList<ServiceStage>();
        Service1Stages.add(new ServiceStage(NetworkConstants.EXECUTION,0,1,a,0));
        Service1Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,1,2,0,data));
        Service1Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,2,3,0,data));
        Service1Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV,3,2,0,data));
        Service1Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV,4,3,0,data));
        Service1Stages.add(new ServiceStage(NetworkConstants.EXECUTION,5,1,a,0));
        Service1Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,6,0,0,data));

        //servicechain1.getServiceStageMap().put(1,Service1Stages);

        Map<Integer, ArrayList<ServiceStage>> service1Stages = new HashMap<>();
        service1Stages.put(0,Service1Stages);
        servicechain1.getServiceStagesMap().put(1,service1Stages);


        /* service2 stage */
        ArrayList<ServiceStage> Service2Stages = new ArrayList<ServiceStage>();
        Service2Stages.add(new ServiceStage(NetworkConstants.EXECUTION,0,2,b,0));
        Service2Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,1,1,0,data));

        //servicechain1.getServiceStageMap().put(2,Service2Stages);
        Map<Integer, ArrayList<ServiceStage>> service2Stages = new HashMap<>();
        service2Stages.put(1,Service2Stages);
        servicechain1.getServiceStagesMap().put(2,service2Stages);

        /* service3 stage */
        ArrayList<ServiceStage> Service3Stages = new ArrayList<ServiceStage>();
        Service3Stages.add(new ServiceStage(NetworkConstants.EXECUTION,0,3,b,0));
        Service3Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,1,1,0,data));

        //servicechain1.getServiceStageMap().put(3,Service3Stages);
        Map<Integer, ArrayList<ServiceStage>> service3Stages = new HashMap<>();
        service3Stages.put(1,Service3Stages);
        servicechain1.getServiceStagesMap().put(3,service3Stages);

        /* create the second service chain */
        Servicechain servicechain2 = new Servicechain(1);
        servicechain2.getMicroserIds().add(0);
        servicechain2.getMicroserIds().add(4);
        servicechain2.getMicroserIds().add(3);
        servicechain2.getMicroserIds().add(5);

        /* services cloudletLength*/
        servicechain2.getCloudletLengthList().put(0,0.0);
        servicechain2.getCloudletLengthList().put(4,a*2);
        servicechain2.getCloudletLengthList().put(3,a*2); //service 3 function2
        servicechain2.getCloudletLengthList().put(5,b);

        servicechain2.getPesNumberList().put(0,1);
        servicechain2.getPesNumberList().put(4,1);
        servicechain2.getPesNumberList().put(3,1);
        servicechain2.getPesNumberList().put(5,1);

        servicechain2.getMemList().put(0,10);
        servicechain2.getMemList().put(4,100);
        servicechain2.getMemList().put(3,100);
        servicechain2.getMemList().put(5,100);


        /* service4 stage */
        ArrayList<ServiceStage> chain2Service0Stages = new ArrayList<ServiceStage>();
        chain2Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,0,4,0,data));
        chain2Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV,1,4,0,data));
        //servicechain2.getServiceStageMap().put(0,chain2Service0Stages);
        Map<Integer, ArrayList<ServiceStage>> chain2service0Stages = new HashMap<>();
        chain2service0Stages.put(-1,chain2Service0Stages);
        servicechain2.getServiceStagesMap().put(0,chain2service0Stages);

        ArrayList<ServiceStage> Service4Stages = new ArrayList<ServiceStage>();
        Service4Stages.add(new ServiceStage(NetworkConstants.EXECUTION,0,4,a,0));
        Service4Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,1,3,0,data));
        Service4Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV,2,3,0,data));
        Service4Stages.add(new ServiceStage(NetworkConstants.EXECUTION,3,4,a,0));
        Service4Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,4,0,0,data));

        //servicechain2.getServiceStageMap().put(4,Service4Stages);
        Map<Integer, ArrayList<ServiceStage>> chain2service4Stages = new HashMap<>();
        chain2service4Stages.put(0,Service4Stages);
        servicechain2.getServiceStagesMap().put(4,chain2service4Stages);

        /* chain 2 service3 stage */
        ArrayList<ServiceStage> chain2Service3Stages = new ArrayList<ServiceStage>();
        chain2Service3Stages.add(new ServiceStage(NetworkConstants.EXECUTION,0,3,a,0));
        chain2Service3Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,1,5,0,data));
        chain2Service3Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV,2,5,0,data));
        chain2Service3Stages.add(new ServiceStage(NetworkConstants.EXECUTION,3,3,a,0));
        chain2Service3Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,4,4,0,data));

        //servicechain2.getServiceStageMap().put(3,chain2Service3Stages);
        Map<Integer, ArrayList<ServiceStage>> chain2service3Stages = new HashMap<>();
        chain2service3Stages.put(4,chain2Service3Stages);
        servicechain2.getServiceStagesMap().put(3,chain2service3Stages);

        /* service5 stage */
        ArrayList<ServiceStage> Service5Stages = new ArrayList<ServiceStage>();
        Service5Stages.add(new ServiceStage(NetworkConstants.EXECUTION,0,5,b,0));
        Service5Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,1,3,0,data));

        //servicechain2.getServiceStageMap().put(5,Service5Stages);
        Map<Integer, ArrayList<ServiceStage>> chain2service5Stages = new HashMap<>();
        chain2service5Stages.put(3,Service5Stages);
        servicechain2.getServiceStagesMap().put(5,chain2service5Stages);

        servicechains.add(servicechain1);
        servicechains.add(servicechain2);

        return servicechains;
    }
}
