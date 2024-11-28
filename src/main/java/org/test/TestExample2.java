package org.test;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.EndUser;
import org.infrastructureProvider.DevicesProvider;
import org.infrastructureProvider.DevicesProviderSimple1;
import org.infrastructureProvider.entities.NetworkDevice;
import org.serviceProvider.ServiceProvider;
import org.serviceProvider.capacities.*;
import org.serviceProvider.services.ApplicationServices;
import org.serviceProvider.services.ApplicationServicesSimple;
import org.serviceProvider.services.ServiceChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*
service num: 10
service chain num: 6
device provider:
    level: 4
    level_1: small base station
    level_2: medium base station
    level_3: router
    level_4: cloud
* */
/*
for service placement policy
* */
@SuppressWarnings({"CommentedOutCode", "unused", "SameParameterValue"})
public class TestExample2 {
    private static final Logger logger = LoggerFactory.getLogger(TestExample2.class);

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Log.printLine("Starting ServiceSim Simulation...");
        try {
            // Default Setup for CloudSim
            cloudSimSetup();

            int experimentNum = 3;

            // simulation limit
            final double SIMULATION_LIMIT = 1;
            final String endUserSimFile = "workloadTest1.csv";

            String workloadResult = "src//others//results//workloadResult" + experimentNum + ".csv";
            String cloudletExeDetail = "src//others//results//cloudletExeDetail" + experimentNum + ".csv";
            String cloudletStageDetail = "src//others//results//cloudletStagesDetail" + experimentNum + ".csv";

            String[] cloudletResultFile = new String[]{workloadResult, cloudletExeDetail, cloudletStageDetail};

            int smallBS = 32;
            int meBS = 4;
            int router = 1;

            DevicesProvider devicesProvider = new DevicesProviderSimple1(smallBS, meBS, router);

            List<ServiceChain> serviceChain = createdServiceChains();

            Map<Integer, LoadAdmission> initLoadAdmission = new HashMap<>();
            Map<Integer, RequestDispatchingRule> initRequestDispatching = new HashMap<>();
            Map<Integer, LoadBalance> initLoadBalance = new HashMap<>();

            /* load admission and load balance */
            for (NetworkDevice device : devicesProvider.getDevices()) {
                initLoadAdmission.put(device.getId(), new NonLoadAdmission());
                initLoadBalance.put(device.getId(), new RoundRobin());
            }

            /* request dispatching policy */
            // Full collaboration
            for (NetworkDevice device : devicesProvider.getDevices()) {
                initRequestDispatching.put(device.getId(), new RequestDispatchingSimple((ArrayList<NetworkDevice>) devicesProvider.getDevices()));
            }

            /* deploy policy */

            // edge (random and single level deploy)
            //noinspection MismatchedQueryAndUpdateOfCollection
            Map<Integer, Integer> serviceToInstanceNum_edge = new HashMap<>();
            int deployNum = 10;
            serviceToInstanceNum_edge.put(1, deployNum);
            serviceToInstanceNum_edge.put(2, deployNum);
            serviceToInstanceNum_edge.put(3, deployNum);
            serviceToInstanceNum_edge.put(4, deployNum);
            serviceToInstanceNum_edge.put(5, deployNum * 3);
            serviceToInstanceNum_edge.put(6, deployNum * 3);
            serviceToInstanceNum_edge.put(7, deployNum * 2);
            serviceToInstanceNum_edge.put(8, deployNum * 3);
            serviceToInstanceNum_edge.put(9, deployNum * 3);
            serviceToInstanceNum_edge.put(10, deployNum * 2);

            //
//            serviceToInstanceNum_edge.put(1,deployNum);
//            serviceToInstanceNum_edge.put(2,deployNum*3);
//            serviceToInstanceNum_edge.put(3,deployNum*3);
//            serviceToInstanceNum_edge.put(4,deployNum*3);
//            serviceToInstanceNum_edge.put(5,deployNum*2);
//            serviceToInstanceNum_edge.put(6,deployNum*2);
//            serviceToInstanceNum_edge.put(7,deployNum*3);
//            serviceToInstanceNum_edge.put(8,deployNum);
//            serviceToInstanceNum_edge.put(9,deployNum);
//            serviceToInstanceNum_edge.put(10,deployNum);
//            Map<Integer, Map<Integer, Map<Integer,Integer>>> initInstance = createInitDeployment(serviceToInstanceNum_edge, (ArrayList<NetworkDevice>)devicesProvider.getDevices(), smallBS);

            // edge deploy (according to request num)
            Map<Integer, Map<Integer, Double>> blockServiceChainPro = new HashMap<>();
            blockServiceChainPro.put(0, new HashMap<>());
            blockServiceChainPro.put(1, new HashMap<>());
            blockServiceChainPro.put(2, new HashMap<>());
            blockServiceChainPro.put(3, new HashMap<>());// four groups, not true block id
//            blockServiceChainPro.get(0).put(0,0.2);
//            blockServiceChainPro.get(0).put(-1,0.8);
//            blockServiceChainPro.get(1).put(1,0.2);
//            blockServiceChainPro.get(1).put(-1,0.8);
//            blockServiceChainPro.get(2).put(2,0.2);
//            blockServiceChainPro.get(2).put(-1,0.8);
//            blockServiceChainPro.get(3).put(3,0.2);
//            blockServiceChainPro.get(3).put(-1,0.8);

            blockServiceChainPro.get(0).put(0, 0.8);
            blockServiceChainPro.get(0).put(-1, 0.2);
            blockServiceChainPro.get(1).put(0, 0.8);
            blockServiceChainPro.get(1).put(-1, 0.2);
            blockServiceChainPro.get(2).put(0, 0.8);
            blockServiceChainPro.get(2).put(-1, 0.2);
            blockServiceChainPro.get(3).put(0, 0.8);
            blockServiceChainPro.get(3).put(-1, 0.2);

            Map<Integer, Map<Integer, Integer>> blockToServiceDeployNum = generateBlockToServiceDeployNumSimplePolicy(blockServiceChainPro, 600, 600, serviceChain);
            Map<Integer, Map<Integer, Map<Integer, Integer>>> initInstance = createInitDeploymentSimplePolicy(blockToServiceDeployNum, (ArrayList<NetworkDevice>) devicesProvider.getDevices(), smallBS);

            // cloud
            int deployNum_cloud = 3;
            int type = 1;
            Map<Integer, Integer> typeToNum0 = new HashMap<>();
            typeToNum0.put(type, deployNum_cloud);
            Map<Integer, Map<Integer, Integer>> serviceToNum = new HashMap<>();
            for (int i = 1; i < 11; i++) {
                serviceToNum.put(i, typeToNum0);
            }
            int cloudId = findCloud((ArrayList<NetworkDevice>) devicesProvider.getDevices());
            initInstance.put(cloudId, serviceToNum);

            /* service provider */
            ServiceProvider serviceProvider = new ServiceProvider("serviceProvider", serviceChain, devicesProvider, initLoadAdmission, initLoadBalance, initRequestDispatching, initInstance, cloudletResultFile);

            EndUser endUser = new EndUser("endUser", serviceProvider.getId(), SIMULATION_LIMIT, 600, 600, 1, 0.005, 6, smallBS, blockServiceChainPro);

            //Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

        } catch (Exception e) {
            logger.error("Unwanted errors happen", e);
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Sets basic parameters for simulation
     */
    private static void cloudSimSetup() {
        // First step: Initialize the CloudSim package. It should be called before creating any entities.
        int num_BSP = 1;   // The number of Application Service Providers
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;  // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_BSP, calendar, trace_flag);
    }

    private static List<ServiceChain> createdServiceChains() {
        Map<Integer, Map<Integer, ArrayList<Integer>>> serviceChains = new HashMap<>();
        Map<Integer, ArrayList<Integer>> serviceChain0 = new HashMap<>();
        serviceChain0.put(0, new ArrayList<>(Collections.singletonList(1)));
        serviceChain0.put(1, new ArrayList<>(Collections.singletonList(7)));
        serviceChain0.put(7, new ArrayList<>(Collections.singletonList(6)));
        serviceChain0.put(6, new ArrayList<>(Collections.singletonList(9)));
        serviceChains.put(0, serviceChain0);

        Map<Integer, ArrayList<Integer>> serviceChain1 = new HashMap<>();
        serviceChain1.put(0, new ArrayList<>(Collections.singletonList(2)));
        serviceChain1.put(2, new ArrayList<>(Arrays.asList(7, 5)));
        serviceChain1.put(5, new ArrayList<>(Collections.singletonList(8)));
        serviceChains.put(1, serviceChain1);

        Map<Integer, ArrayList<Integer>> serviceChain2 = new HashMap<>();
        serviceChain2.put(0, new ArrayList<>(Collections.singletonList(3)));
        serviceChain2.put(3, new ArrayList<>(Collections.singletonList(5)));
        serviceChain2.put(5, new ArrayList<>(Collections.singletonList(8)));
        serviceChains.put(2, serviceChain2);

        Map<Integer, ArrayList<Integer>> serviceChain3 = new HashMap<>();
        serviceChain3.put(0, new ArrayList<>(Collections.singletonList(4)));
        serviceChain3.put(4, new ArrayList<>(Collections.singletonList(6)));
        serviceChain3.put(6, new ArrayList<>(Arrays.asList(9, 10)));
        serviceChains.put(3, serviceChain3);

        Map<Integer, ArrayList<Integer>> serviceChain4 = new HashMap<>();
        serviceChain4.put(0, new ArrayList<>(Collections.singletonList(5)));
        serviceChain4.put(5, new ArrayList<>(Collections.singletonList(8)));
        serviceChains.put(4, serviceChain4);

        Map<Integer, ArrayList<Integer>> serviceChain5 = new HashMap<>();
        serviceChain5.put(0, new ArrayList<>(Collections.singletonList(6)));
        serviceChain5.put(6, new ArrayList<>(Arrays.asList(9, 10)));
        serviceChains.put(5, serviceChain5);

        ApplicationServices applicationServices = new ApplicationServicesSimple(serviceChains);
        ArrayList<ServiceChain> serviceChainList = new ArrayList<>();
        for (int key : applicationServices.getServiceChainList().keySet()) {
            serviceChainList.add(applicationServices.getServiceChainList().get(key));
        }
        return serviceChainList;
    }

    private static Map<Integer, Map<Integer, Map<Integer, Integer>>> createInitDeployment(Map<Integer, Integer> serviceToInstanceNum, ArrayList<NetworkDevice> devices, int smallBS) {
        Map<Integer, Map<Integer, Map<Integer, Integer>>> initDeploy = new HashMap<>();
        // gateway
        Map<Integer, Map<Integer, Integer>> deviceIdToServiceNum = new HashMap<>();

        for (int serviceId : serviceToInstanceNum.keySet()) {
            for (int i = 0; i < serviceToInstanceNum.get(serviceId); i++) {
                Random random = new Random();
                int index = random.nextInt(smallBS);
                int id = devices.get(index).getId();
                if (deviceIdToServiceNum.containsKey(id)) {
                    if (deviceIdToServiceNum.get(id).containsKey(serviceId)) {
                        deviceIdToServiceNum.get(id).put(serviceId, deviceIdToServiceNum.get(id).get(serviceId) + 1);
                    } else {
                        deviceIdToServiceNum.get(id).put(serviceId, 1);
                    }

                } else {
                    deviceIdToServiceNum.put(id, new HashMap<>());
                    deviceIdToServiceNum.get(id).put(serviceId, 1);
                }

            }
        }
        for (int id : deviceIdToServiceNum.keySet()) {
            Map<Integer, Integer> typeToNum0 = new HashMap<>();
            typeToNum0.put(0, 1);
            Map<Integer, Map<Integer, Integer>> service0To = new HashMap<>();
            service0To.put(0, typeToNum0);
            initDeploy.put(id, service0To);
            for (int serviceId : deviceIdToServiceNum.get(id).keySet()) {
                Map<Integer, Integer> typeToNum = new HashMap<>();
                typeToNum.put(0, deviceIdToServiceNum.get(id).get(serviceId));
                initDeploy.get(id).put(serviceId, typeToNum);

            }

        }

        for (int j = 0; j < smallBS; j++) {
            if (!initDeploy.containsKey(devices.get(j).getId())) {
                Map<Integer, Integer> typeToNum0 = new HashMap<>();
                typeToNum0.put(0, 1);
                Map<Integer, Map<Integer, Integer>> service0To = new HashMap<>();
                service0To.put(0, typeToNum0);
                initDeploy.put(devices.get(j).getId(), new HashMap<>());
                initDeploy.put(devices.get(j).getId(), service0To);
            }
        }

        return initDeploy;
    }

    private static Map<Integer, Map<Integer, Integer>> generateBlockToServiceDeployNumSimplePolicy(Map<Integer, Map<Integer, Double>> blockServiceChainPro, int low, int high, List<ServiceChain> serviceChains) {

        Map<Integer, ArrayList<Integer>> blockToOtherServiceChains = new HashMap<>();
        for (int block : blockServiceChainPro.keySet()) {
            blockToOtherServiceChains.put(block, new ArrayList<>());
            for (int i = 0; i < serviceChains.size(); i++) {
                if (!blockServiceChainPro.get(block).containsKey(i)) {
                    blockToOtherServiceChains.get(block).add(i);
                }
            }
        }
        Map<Integer, Map<Integer, Double>> blockServiceChainProComplete = new HashMap<>();
        for (int block : blockServiceChainPro.keySet()) {
            blockServiceChainProComplete.put(block, new HashMap<>());
            for (int serviceChain : blockServiceChainPro.get(block).keySet()) {
                if (serviceChain != -1) {
                    blockServiceChainProComplete.get(block).put(serviceChain, blockServiceChainPro.get(block).get(serviceChain));
                } else {
                    double avgPro = blockServiceChainPro.get(block).get(serviceChain) / blockToOtherServiceChains.get(block).size();
                    for (int i = 0; i < blockToOtherServiceChains.get(block).size(); i++) {
                        blockServiceChainProComplete.get(block).put(blockToOtherServiceChains.get(block).get(i), avgPro);
                    }
                }
            }
        }


        Map<Integer, Map<Integer, Integer>> blockToServiceDeployNum = new HashMap<>();
        Map<Integer, Map<Integer, Double>> blockServiceNum = new HashMap<>(); // serviceId,

        Map<Integer, ArrayList<Integer>> serviceChainToSerIds = new HashMap<>();
        for (ServiceChain serviceChain : serviceChains) {
            serviceChainToSerIds.put(serviceChain.getServiceChainId(), serviceChain.getMicroserviceIds());
        }
        int blockNum = blockServiceChainPro.size();
        int blockRequestNum = (low + high) / 2 / blockNum;
        for (int i = 0; i < blockNum; i++) {
            blockToServiceDeployNum.put(i, new HashMap<>());
            blockServiceNum.put(i, new HashMap<>());
        }
        for (int block : blockServiceChainProComplete.keySet()) {
            for (int serviceChain : blockServiceChainProComplete.get(block).keySet()) {

                for (int i = 0; i < serviceChainToSerIds.get(serviceChain).size(); i++) {
                    int serviceId = serviceChainToSerIds.get(serviceChain).get(i);
                    if (serviceId != 0) {
                        double serReqNum = blockRequestNum * blockServiceChainProComplete.get(block).get(serviceChain);
                        if (blockServiceNum.get(block).containsKey(serviceId)) {
                            blockServiceNum.get(block).put(serviceId, blockServiceNum.get(block).get(serviceId) + serReqNum);
                        } else {
                            blockServiceNum.get(block).put(serviceId, serReqNum);
                        }
                    }
                }
            }
        }

        double totalNum = 0;
        for (int block : blockServiceNum.keySet()) {
            for (int ser : blockServiceNum.get(block).keySet()) {
                totalNum += blockServiceNum.get(block).get(ser);
            }
        }

        for (int block : blockServiceNum.keySet()) {
            for (int ser : blockServiceNum.get(block).keySet()) {
                double pro = blockServiceNum.get(block).get(ser) / totalNum;
                int num = (int) (200 * pro);
                blockToServiceDeployNum.get(block).put(ser, num);
            }
        }
        return blockToServiceDeployNum;

    }

    private static Map<Integer, Map<Integer, Map<Integer, Integer>>> createInitDeploymentSimplePolicy(Map<Integer, Map<Integer, Integer>> blockToServiceDeployNum, ArrayList<NetworkDevice> devices, int smallBS) {
        Map<Integer, Map<Integer, Map<Integer, Integer>>> initDeploy = new HashMap<>(); // deviceId, serId, type, num
        // gateway
        Map<Integer, Map<Integer, Integer>> deviceIdToServiceNum = new HashMap<>();
        int groupNum = smallBS / blockToServiceDeployNum.size();
        for (int highLevelBlock : blockToServiceDeployNum.keySet()) {
            Random random = new Random();
            for (int ser : blockToServiceDeployNum.get(highLevelBlock).keySet()) {
                for (int i = 0; i < blockToServiceDeployNum.get(highLevelBlock).get(ser); i++) {
                    int block = random.nextInt(groupNum) + highLevelBlock * groupNum;
                    int id = devices.get(block).getId();
                    if (deviceIdToServiceNum.containsKey(id)) {
                        if (deviceIdToServiceNum.get(id).containsKey(ser)) {
                            deviceIdToServiceNum.get(id).put(ser, deviceIdToServiceNum.get(id).get(ser) + 1);
                        } else {
                            deviceIdToServiceNum.get(id).put(ser, 1);
                        }

                    } else {
                        deviceIdToServiceNum.put(id, new HashMap<>());
                        deviceIdToServiceNum.get(id).put(ser, 1);
                    }

                }
            }
        }

        for (int id : deviceIdToServiceNum.keySet()) {
            Map<Integer, Integer> typeToNum0 = new HashMap<>();
            typeToNum0.put(0, 1);
            Map<Integer, Map<Integer, Integer>> service0To = new HashMap<>();
            service0To.put(0, typeToNum0);
            initDeploy.put(id, service0To);
            for (int serviceId : deviceIdToServiceNum.get(id).keySet()) {
                Map<Integer, Integer> typeToNum = new HashMap<>();
                typeToNum.put(0, deviceIdToServiceNum.get(id).get(serviceId));
                initDeploy.get(id).put(serviceId, typeToNum);

            }

        }

        for (int j = 0; j < smallBS; j++) {
            if (!initDeploy.containsKey(devices.get(j).getId())) {
                Map<Integer, Integer> typeToNum0 = new HashMap<>();
                typeToNum0.put(0, 1);
                Map<Integer, Map<Integer, Integer>> service0To = new HashMap<>();
                service0To.put(0, typeToNum0);
                initDeploy.put(devices.get(j).getId(), new HashMap<>());
                initDeploy.put(devices.get(j).getId(), service0To);
            }
        }

        return initDeploy;
    }


    public static int findCloud(ArrayList<NetworkDevice> networkDevices) {
        return TestExample1.findCloud(networkDevices);
    }


}
