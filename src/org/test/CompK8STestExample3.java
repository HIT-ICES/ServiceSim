package org.test;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.EndUser;
import org.infrastructureProvider.CompK8SDevicesProvider1;
import org.infrastructureProvider.DevicesProvider;
import org.infrastructureProvider.entities.NetworkDevice;
import org.serviceProvider.ServiceProvider;
import org.serviceProvider.capacities.*;
import org.serviceProvider.services.ApplicationServices;
import org.serviceProvider.services.ApplicationServicesCompK8S;
import org.serviceProvider.services.Servicechain;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// 4 K8s nodes
public class CompK8STestExample3 {

    public static void main(String[] args) {
        Long startTime = System.currentTimeMillis();
        Log.printLine("Starting ServiceSim Simulation...");
        try {
            // Default Setup for CloudSim
            cloudSimSetup();

            int experimentNum = 26;

            // simulation limit
            final double SIMULATION_LIMIT = 1582; // testRequest15  1582 1791 1514 1839 1253
            final String endUserSimuFile = "workloadTest1.csv";

            String workloadResult = "src//others//results//compK8Snode4a1//workloadResult" + String.valueOf(experimentNum) + ".csv";
            String cloudletExeDetail = "src//others//results//compK8Snode4a1//cloudletExeDetail" + String.valueOf(experimentNum) + ".csv";
            String cloudletStageDetail = "src//others//results//compK8Snode4a1//cloudletStagesDetail" + String.valueOf(experimentNum) + ".csv";

            String[] cloudletResultFile = new String[]{workloadResult,cloudletExeDetail,cloudletStageDetail};

            int nodeNum = 4;

            DevicesProvider devicesProvider = new CompK8SDevicesProvider1(nodeNum);

            List<Servicechain> serviceChain = createdServicechains();

            Map<Integer, LoadAdmission> initLoadAdmission = new HashMap<>();
            Map<Integer, RequestDispatchingRule> initRequestDispatching = new HashMap<>();
            Map<Integer, LoadBalance> initLoadBalance = new HashMap<>();

            /* load admission and load balance */
            for (NetworkDevice device : devicesProvider.getDevices()){
                initLoadAdmission.put(device.getId(),new NonLoadAdmission());
                initLoadBalance.put(device.getId(),new RoundRobin());
            }

            /* request dispatching policy */
            // Full collaboration
            for (NetworkDevice device : devicesProvider.getDevices()){
                initRequestDispatching.put(device.getId(),new RequestDispatchingSimple((ArrayList<NetworkDevice>)devicesProvider.getDevices()));
            }

            /* deploy policy */

            // edge (random and single level deploy)
            Map<Integer, Integer> serviceToInstanceNum_edge = new HashMap<>();
            int deploynum = 1;
            serviceToInstanceNum_edge.put(1,deploynum);
            serviceToInstanceNum_edge.put(2,deploynum);
            serviceToInstanceNum_edge.put(3,deploynum);
            serviceToInstanceNum_edge.put(4,deploynum);
            serviceToInstanceNum_edge.put(5,deploynum*3);
            serviceToInstanceNum_edge.put(6,deploynum*3);
            serviceToInstanceNum_edge.put(7,deploynum*2);
            serviceToInstanceNum_edge.put(8,deploynum*3);
            serviceToInstanceNum_edge.put(9,deploynum*3);
            serviceToInstanceNum_edge.put(10,deploynum*2);

            Map<Integer,Integer>  type4ToNum1 = new HashMap<>();
            Map<Integer,Integer>  type4ToNum2 = new HashMap<>();
            Map<Integer,Integer>  type4ToNum3 = new HashMap<>();
            Map<Integer,Integer>  type0ToNum1 = new HashMap<>();
            Map<Integer,Integer>  type0ToNum10 = new HashMap<>();
            type4ToNum1.put(4,1);
            type4ToNum2.put(4,2);
            type4ToNum3.put(4,3);
            type0ToNum1.put(0,1);

            type0ToNum10.put(0,10);

            Map<Integer,Integer>  chainToNum = new HashMap<>();
            chainToNum.put(0,1);
            chainToNum.put(1,1);
            chainToNum.put(2,1);
            chainToNum.put(3,1);
//            chainToNum.put(4,3);
//            chainToNum.put(5,3);
            chainToNum.put(4,1);
            chainToNum.put(5,1);


            Map<Integer, Map<Integer,Integer>> serviceToInstance = new HashMap<>();
            serviceToInstance.put(0,type0ToNum1);
            serviceToInstance.put(1,type4ToNum1);
            serviceToInstance.put(5,type4ToNum3);
            serviceToInstance.put(7,type4ToNum2);


            Map<Integer, Map<Integer,Integer>> serviceToInstance1 = new HashMap<>();
            serviceToInstance1.put(0,type0ToNum1);
            serviceToInstance1.put(2,type4ToNum1);
            serviceToInstance1.put(6,type4ToNum3);
            serviceToInstance1.put(10,type4ToNum2);

            Map<Integer, Map<Integer,Integer>> serviceToInstance2 = new HashMap<>();
            serviceToInstance2.put(0,type0ToNum1);
            serviceToInstance2.put(3,type4ToNum1);
            serviceToInstance2.put(8,type4ToNum3);

            Map<Integer, Map<Integer,Integer>> serviceToInstance3 = new HashMap<>();
            serviceToInstance3.put(0,type0ToNum1);
            serviceToInstance3.put(4,type4ToNum1);
            serviceToInstance3.put(9,type4ToNum3);

            Map<Integer, Map<Integer,Integer>> serviceToInstance4 = new HashMap<>();
            //serviceToInstance3.put(0,type0ToNum1);
            serviceToInstance4.put(0,type0ToNum1);


            Map<Integer, Map<Integer, Map<Integer,Integer>>> initInstance = new HashMap<>();

            initInstance.put(devicesProvider.getDevices().get(0).getId(),serviceToInstance);
            initInstance.put(devicesProvider.getDevices().get(1).getId(),serviceToInstance1);
            initInstance.put(devicesProvider.getDevices().get(2).getId(),serviceToInstance2);
            initInstance.put(devicesProvider.getDevices().get(3).getId(),serviceToInstance3);
            //initInstance.put(devicesProvider.getDevices().get(4).getId(),serviceToInstance4);


            /* service provider */
            ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",serviceChain,devicesProvider,initLoadAdmission,initLoadBalance,initRequestDispatching,initInstance,cloudletResultFile);

            Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelay =  getWorkloadForAccDelay("src//org//test//workloadGenerator//k8snode4-1//serRequest15//");

            EndUser endUser = new EndUser("endUser", serviceProvider.getId(), SIMULATION_LIMIT, 1, 0.001, 1,requestToDelay,7);

            //Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();
            Long endTime = System.currentTimeMillis();
            Long time = endTime - startTime;
            Log.printLine("test example execute time："+time);

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

    private static List<Servicechain> createdServicechains(){
        Map<Integer, Map<Integer, ArrayList<Integer>>> serviceChains = new HashMap<>();
        Map<Integer, ArrayList<Integer>> servicechain0 = new HashMap<>();
        servicechain0.put(0,new ArrayList<>(Arrays.asList(1)));
        servicechain0.put(1,new ArrayList<>(Arrays.asList(7)));
        servicechain0.put(7,new ArrayList<>(Arrays.asList(6)));
        servicechain0.put(6,new ArrayList<>(Arrays.asList(9)));
        serviceChains.put(0,servicechain0);

        Map<Integer, ArrayList<Integer>> servicechain1 = new HashMap<>();
        servicechain1.put(0,new ArrayList<>(Arrays.asList(2)));
        servicechain1.put(2,new ArrayList<>(Arrays.asList(5,7)));
        servicechain1.put(5,new ArrayList<>(Arrays.asList(8)));
        serviceChains.put(1,servicechain1);

        Map<Integer, ArrayList<Integer>> servicechain2 = new HashMap<>();
        servicechain2.put(0,new ArrayList<>(Arrays.asList(3)));
        servicechain2.put(3,new ArrayList<>(Arrays.asList(5)));
        servicechain2.put(5,new ArrayList<>(Arrays.asList(8)));
        serviceChains.put(2,servicechain2);

        Map<Integer, ArrayList<Integer>> servicechain3 = new HashMap<>();
        servicechain3.put(0,new ArrayList<>(Arrays.asList(4)));
        servicechain3.put(4,new ArrayList<>(Arrays.asList(6)));
        servicechain3.put(6,new ArrayList<>(Arrays.asList(9,10)));
        serviceChains.put(3,servicechain3);

        Map<Integer, ArrayList<Integer>> servicechain4 = new HashMap<>();
        servicechain4.put(0,new ArrayList<>(Arrays.asList(5)));
        servicechain4.put(5,new ArrayList<>(Arrays.asList(8)));
        serviceChains.put(4,servicechain4);

        Map<Integer, ArrayList<Integer>> servicechain5 = new HashMap<>();
        servicechain5.put(0,new ArrayList<>(Arrays.asList(6)));
        servicechain5.put(6,new ArrayList<>(Arrays.asList(9,10)));
        serviceChains.put(5,servicechain5);

        ApplicationServices applicationServices = new ApplicationServicesCompK8S(serviceChains);
        ArrayList<Servicechain> servicechains = new ArrayList<>();
        for (int key : applicationServices.getServicechainList().keySet()){
            servicechains.add(applicationServices.getServicechainList().get(key));
        }
        return servicechains;
    }

    private static Map<Double,ArrayList<Pair<Integer,Double>>> getWorkload(String filename, double startTime){

        Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelay = new HashMap<>();
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(filename));
            try {
                String line;
                while ( (line = fileReader.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    String url = rowStr[0];
                    String sc = String.valueOf(url.charAt(25));
                    int servicechainId = Integer.valueOf(sc) - 1;
                    double delay = Double.valueOf(rowStr[1]) - startTime;
                    double time = (int) delay;
                    Pair<Integer,Double> reDealy = new Pair<>(servicechainId,delay-time);
                    if (requestToDelay.containsKey(time)){
                        requestToDelay.get(time).add(reDealy);
                    }else{
                        requestToDelay.put(time,new ArrayList<>());
                        requestToDelay.get(time).add(reDealy);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return requestToDelay;
    }

    private static Map<Double,ArrayList<Pair<Integer,Double>>> getWorkloadForAccDelay(String path){

        //Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelay = getWorkload(filename,startTime);
        String file1 = path + "ser1.txt";
        String file2 = path + "ser2.txt";
        String file3 = path + "ser3.txt";
        String file4 = path + "ser4.txt";
        String file5 = path + "ser5.txt";
        String file6 = path + "ser6.txt";

        Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelayNew = new HashMap<>();

        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(file1));
            try {
                String line;
                while ( (line = fileReader.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    int num = Integer.valueOf(rowStr[2]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    for (int i = 0; i< num;i++){
                        Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }


        BufferedReader fileReader1;
        try {
            fileReader1 = new BufferedReader(new FileReader(file2));
            try {
                String line;
                while ( (line = fileReader1.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    int num = Integer.valueOf(rowStr[2]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    for (int i = 0; i< num;i++){
                        Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        BufferedReader fileReader2;
        try {
            fileReader2 = new BufferedReader(new FileReader(file3));
            try {
                String line;
                while ( (line = fileReader2.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    int num = Integer.valueOf(rowStr[2]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    for (int i = 0; i< num;i++){
                        Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }


        BufferedReader fileReader3;
        try {
            fileReader3 = new BufferedReader(new FileReader(file4));
            try {
                String line;
                while ( (line = fileReader3.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    int num = Integer.valueOf(rowStr[2]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    for (int i = 0; i< num;i++){
                        Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        BufferedReader fileReader4;
        try {
            fileReader4 = new BufferedReader(new FileReader(file5));
            try {
                String line;
                while ( (line = fileReader4.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    int num = Integer.valueOf(rowStr[2]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (serchain!=4){
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    for (int i = 0; i< num;i++){
                        Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                        requestToDelayNew.get(time).add(pair);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }


        BufferedReader fileReader5;
        try {
            fileReader5 = new BufferedReader(new FileReader(file6));
            try {
                String line;
                while ( (line = fileReader5.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    int num = Integer.valueOf(rowStr[2]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (serchain!=5){
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }
                    for (int i = 0; i< num;i++){
                        Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                        requestToDelayNew.get(time).add(pair);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        return requestToDelayNew;
    }

}
