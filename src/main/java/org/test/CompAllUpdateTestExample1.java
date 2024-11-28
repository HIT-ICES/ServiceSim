package org.test;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.EndUser;
import org.infrastructureProvider.CompAllUpdateDevicesProvider2;
import org.infrastructureProvider.DevicesProvider;
import org.infrastructureProvider.entities.NetworkDevice;
import org.serviceProvider.ServiceProvider;
import org.serviceProvider.capacities.*;
import org.serviceProvider.services.ApplicationServices;
import org.serviceProvider.services.ApplicationServicesCompK8S;
import org.serviceProvider.services.ServiceChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"CommentedOutCode", "unused", "SameParameterValue"})
public class CompAllUpdateTestExample1 {
    private static final Logger logger = LoggerFactory.getLogger(CompAllUpdateTestExample1.class);

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public static void main(String[] args) {
        Long startTime = System.currentTimeMillis();
        Log.printLine("Starting ServiceSim Simulation...");
        try {
            // Default Setup for CloudSim
            cloudSimSetup();

            int experimentNum = 4;

            // simulation limit
            final double SIMULATION_LIMIT = 1791; // testRequest15 1582 1791 1514 1839 // 10 minutes.
            // 929 1144 1041 1010 1255

            String workloadResult = "src//others//results//allUpdate//workloadResult" + experimentNum + ".csv";
            String cloudletExeDetail = "src//others//results//allUpdate//cloudletExeDetail" + experimentNum + ".csv";
            String cloudletStageDetail = "src//others//results//allUpdate//cloudletStagesDetail" + experimentNum + ".csv";

            String[] cloudletResultFile = new String[]{workloadResult, cloudletExeDetail, cloudletStageDetail};

            int nodeNum = 4;

            DevicesProvider devicesProvider = new CompAllUpdateDevicesProvider2(nodeNum);

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
            Map<Integer, Map<Integer, Map<Integer, Integer>>> initInstance = getMapMap(devicesProvider);
            //initInstance.put(devicesProvider.getDevices().get(4).getId(),serviceToInstance4);


            /* service provider */
            ServiceProvider serviceProvider = new ServiceProvider("serviceProvider", serviceChain, devicesProvider, initLoadAdmission, initLoadBalance, initRequestDispatching, initInstance, cloudletResultFile);

            Map<Double, ArrayList<Pair<Integer, Double>>> requestToDelay = getWorkloadForAccDelay("src//org//test//workloadGenerator//k8snode4-1//serRequest17//");

            EndUser endUser = new EndUser("endUser", serviceProvider.getId(), SIMULATION_LIMIT, 1, 0.001, 1, requestToDelay, 7);

            //Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();
            Long endTime = System.currentTimeMillis();
            long time = endTime - startTime;
            Log.printLine("test example execute time：" + time);

        } catch (Exception e) {
            logger.error(e.getMessage());
            Log.printLine("Unwanted errors happen");
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static Map<Integer, Map<Integer, Map<Integer, Integer>>> getMapMap(DevicesProvider devicesProvider) {
        Map<Integer, Integer> serviceToInstanceNum_edge = new HashMap<>();
        int deployNum = 1;
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

        Map<Integer, Integer> type4ToNum1 = new HashMap<>();
        Map<Integer, Integer> type4ToNum2 = new HashMap<>();
        Map<Integer, Integer> type4ToNum3 = new HashMap<>();
        Map<Integer, Integer> type0ToNum1 = new HashMap<>();
        Map<Integer, Integer> type0ToNum10 = new HashMap<>();
        type4ToNum1.put(4, 1);
        type4ToNum2.put(4, 2);
        type4ToNum3.put(4, 3);
        type0ToNum1.put(0, 1);

        type0ToNum10.put(0, 10);

        Map<Integer, Integer> chainToNum = new HashMap<>();
        chainToNum.put(0, 1);
        chainToNum.put(1, 1);
        chainToNum.put(2, 1);
        chainToNum.put(3, 1);
//            chainToNum.put(4,3);
//            chainToNum.put(5,3);
        chainToNum.put(4, 1);
        chainToNum.put(5, 1);


        Map<Integer, Map<Integer, Integer>> serviceToInstance = new HashMap<>();
        serviceToInstance.put(0, type0ToNum1);
        serviceToInstance.put(1, type4ToNum1);
        serviceToInstance.put(5, type4ToNum3);
        serviceToInstance.put(7, type4ToNum2);


        Map<Integer, Map<Integer, Integer>> serviceToInstance1 = new HashMap<>();
        serviceToInstance1.put(0, type0ToNum1);
        serviceToInstance1.put(2, type4ToNum1);
        serviceToInstance1.put(6, type4ToNum3);
        serviceToInstance1.put(10, type4ToNum2);

        Map<Integer, Map<Integer, Integer>> serviceToInstance2 = new HashMap<>();
        serviceToInstance2.put(0, type0ToNum1);
        serviceToInstance2.put(3, type4ToNum1);
        serviceToInstance2.put(8, type4ToNum3);

        Map<Integer, Map<Integer, Integer>> serviceToInstance3 = new HashMap<>();
        serviceToInstance3.put(0, type0ToNum1);
        serviceToInstance3.put(4, type4ToNum1);
        serviceToInstance3.put(9, type4ToNum3);

        Map<Integer, Map<Integer, Integer>> serviceToInstance4 = new HashMap<>();
        //serviceToInstance3.put(0,type0ToNum1);
        serviceToInstance4.put(0, type0ToNum1);


        Map<Integer, Map<Integer, Map<Integer, Integer>>> initInstance = new HashMap<>();

        initInstance.put(devicesProvider.getDevices().get(0).getId(), serviceToInstance);
        initInstance.put(devicesProvider.getDevices().get(1).getId(), serviceToInstance1);
        initInstance.put(devicesProvider.getDevices().get(2).getId(), serviceToInstance2);
        initInstance.put(devicesProvider.getDevices().get(3).getId(), serviceToInstance3);
        return initInstance;
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
        serviceChain1.put(2, new ArrayList<>(Arrays.asList(5, 7)));
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

        ApplicationServices applicationServices = new ApplicationServicesCompK8S(serviceChains);
        ArrayList<ServiceChain> serviceChainList = new ArrayList<>();
        for (int key : applicationServices.getServiceChainList().keySet()) {
            serviceChainList.add(applicationServices.getServiceChainList().get(key));
        }
        return serviceChainList;
    }

    private static Map<Double, ArrayList<Pair<Integer, Double>>> getWorkload(String filename, double startTime) {

        Map<Double, ArrayList<Pair<Integer, Double>>> requestToDelay = new HashMap<>();
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(filename));
            try {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    String url = rowStr[0];
                    String sc = String.valueOf(url.charAt(25));
                    int serviceChainId = Integer.parseInt(sc) - 1;
                    double delay = Double.parseDouble(rowStr[1]) - startTime;
                    double time = (int) delay;
                    Pair<Integer, Double> reDelay = new Pair<>(serviceChainId, delay - time);
                    if (requestToDelay.containsKey(time)) {
                        requestToDelay.get(time).add(reDelay);
                    } else {
                        requestToDelay.put(time, new ArrayList<>());
                        requestToDelay.get(time).add(reDelay);
                    }
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        return requestToDelay;
    }

    private static Map<Double, ArrayList<Pair<Integer, Double>>> getWorkloadForAccDelay(String path) {

        //Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelay = getWorkload(filename,startTime);
        String file1 = path + "ser1.txt";
        String file2 = path + "ser2.txt";
        String file3 = path + "ser3.txt";
        String file4 = path + "ser4.txt";
        String file5 = path + "ser5.txt";
        String file6 = path + "ser6.txt";

        Map<Double, ArrayList<Pair<Integer, Double>>> requestToDelayNew = new HashMap<>();

        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(file1));
            try {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    int num = Integer.parseInt(rowStr[2]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    for (int i = 0; i < num; i++) {
                        Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }


        BufferedReader fileReader1;
        try {
            fileReader1 = new BufferedReader(new FileReader(file2));
            try {
                String line;
                while ((line = fileReader1.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    int num = Integer.parseInt(rowStr[2]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    for (int i = 0; i < num; i++) {
                        Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }

        BufferedReader fileReader2;
        try {
            fileReader2 = new BufferedReader(new FileReader(file3));
            try {
                String line;
                while ((line = fileReader2.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    int num = Integer.parseInt(rowStr[2]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    for (int i = 0; i < num; i++) {
                        Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }


        BufferedReader fileReader3;
        try {
            fileReader3 = new BufferedReader(new FileReader(file4));
            try {
                String line;
                while ((line = fileReader3.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    int num = Integer.parseInt(rowStr[2]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    for (int i = 0; i < num; i++) {
                        Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }

        BufferedReader fileReader4;
        try {
            fileReader4 = new BufferedReader(new FileReader(file5));
            try {
                String line;
                while ((line = fileReader4.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    int num = Integer.parseInt(rowStr[2]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (serChain != 4) {
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    for (int i = 0; i < num; i++) {
                        Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }


        BufferedReader fileReader5;
        try {
            fileReader5 = new BufferedReader(new FileReader(file6));
            try {
                String line;
                while ((line = fileReader5.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    int num = Integer.parseInt(rowStr[2]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (serChain != 5) {
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }
                    for (int i = 0; i < num; i++) {
                        Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                        requestToDelayNew.get(time).add(pair);
                    }

                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }

        return requestToDelayNew;
    }

}
