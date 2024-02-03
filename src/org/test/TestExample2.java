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
import org.serviceProvider.services.Servicechain;

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
public class TestExample2 {
    public static void main(String[] args) {
        Log.printLine("Starting ServiceSim Simulation...");
        try {
            // Default Setup for CloudSim
            cloudSimSetup();

            int experimentNum = 3;

            // simulation limit
            final double SIMULATION_LIMIT = 1;
            final String endUserSimuFile = "workloadTest1.csv";

            String workloadResult = "src//others//results//workloadResult" + String.valueOf(experimentNum) + ".csv";
            String cloudletExeDetail = "src//others//results//cloudletExeDetail" + String.valueOf(experimentNum) + ".csv";
            String cloudletStageDetail = "src//others//results//cloudletStagesDetail" + String.valueOf(experimentNum) + ".csv";

            String[] cloudletResultFile = new String[]{workloadResult,cloudletExeDetail,cloudletStageDetail};

            int smallBS = 32;
            int meBS = 4;
            int router = 1;

            DevicesProvider devicesProvider = new DevicesProviderSimple1(smallBS,meBS,router);

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
            int deploynum = 10;
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

            //
//            serviceToInstanceNum_edge.put(1,deploynum);
//            serviceToInstanceNum_edge.put(2,deploynum*3);
//            serviceToInstanceNum_edge.put(3,deploynum*3);
//            serviceToInstanceNum_edge.put(4,deploynum*3);
//            serviceToInstanceNum_edge.put(5,deploynum*2);
//            serviceToInstanceNum_edge.put(6,deploynum*2);
//            serviceToInstanceNum_edge.put(7,deploynum*3);
//            serviceToInstanceNum_edge.put(8,deploynum);
//            serviceToInstanceNum_edge.put(9,deploynum);
//            serviceToInstanceNum_edge.put(10,deploynum);
//            Map<Integer, Map<Integer, Map<Integer,Integer>>> initInstance = createInitDeployment(serviceToInstanceNum_edge, (ArrayList<NetworkDevice>)devicesProvider.getDevices(), smallBS);

            // edge deploy (according to request num)
            Map<Integer,Map<Integer,Double>> blockServicechainPro = new HashMap<>();
            blockServicechainPro.put(0,new HashMap<>());
            blockServicechainPro.put(1,new HashMap<>());
            blockServicechainPro.put(2,new HashMap<>());
            blockServicechainPro.put(3,new HashMap<>());// four group, not true block id
//            blockServicechainPro.get(0).put(0,0.2);
//            blockServicechainPro.get(0).put(-1,0.8);
//            blockServicechainPro.get(1).put(1,0.2);
//            blockServicechainPro.get(1).put(-1,0.8);
//            blockServicechainPro.get(2).put(2,0.2);
//            blockServicechainPro.get(2).put(-1,0.8);
//            blockServicechainPro.get(3).put(3,0.2);
//            blockServicechainPro.get(3).put(-1,0.8);

            blockServicechainPro.get(0).put(0,0.8);
            blockServicechainPro.get(0).put(-1,0.2);
            blockServicechainPro.get(1).put(0,0.8);
            blockServicechainPro.get(1).put(-1,0.2);
            blockServicechainPro.get(2).put(0,0.8);
            blockServicechainPro.get(2).put(-1,0.2);
            blockServicechainPro.get(3).put(0,0.8);
            blockServicechainPro.get(3).put(-1,0.2);

            Map<Integer,Map<Integer,Integer>> blockToServiceDeploynum = generateBlockToServiceDeploynumSimplePolicy(blockServicechainPro,600,600,serviceChain);
            Map<Integer, Map<Integer, Map<Integer,Integer>>> initInstance = createInitDeploymentSimplePolicy(blockToServiceDeploynum, (ArrayList<NetworkDevice>)devicesProvider.getDevices(), smallBS);

            // cloud
            int deploynum_cloud = 3;
            int type = 1;
            Map<Integer,Integer>  typeToNum0 = new HashMap<>();
            typeToNum0.put(type,deploynum_cloud);
            Map<Integer, Map<Integer,Integer>> serviceToNum = new HashMap<>();
            for (int i = 1 ; i < 11;i++){
                serviceToNum.put(i,typeToNum0);
            }
            int cloudid = findCloud((ArrayList<NetworkDevice>)devicesProvider.getDevices());
            initInstance.put(cloudid,serviceToNum);

            /* service provider */
            ServiceProvider serviceProvider = new ServiceProvider("serviceProvider",serviceChain,devicesProvider,initLoadAdmission,initLoadBalance,initRequestDispatching,initInstance,cloudletResultFile);

            EndUser endUser = new EndUser("endUser", serviceProvider.getId(), SIMULATION_LIMIT, 600, 600, 1,0.005,6,smallBS,blockServicechainPro);

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
        servicechain1.put(2,new ArrayList<>(Arrays.asList(7,5)));
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

        ApplicationServices applicationServices = new ApplicationServicesSimple(serviceChains);
        ArrayList<Servicechain> servicechains = new ArrayList<>();
        for (int key : applicationServices.getServicechainList().keySet()){
            servicechains.add(applicationServices.getServicechainList().get(key));
        }
        return servicechains;
    }

    private static Map<Integer, Map<Integer, Map<Integer,Integer>>> createInitDeployment(Map<Integer, Integer> serviceToInstanceNum, ArrayList<NetworkDevice> devices, int smallBS){
        Map<Integer, Map<Integer, Map<Integer,Integer>>> initDeploy = new HashMap<>();
        // gateway
        Map<Integer, Map<Integer,Integer>> deviceIdToServiceNum = new HashMap<>();

        for (int serviceId : serviceToInstanceNum.keySet()){
            for (int i = 0;i< serviceToInstanceNum.get(serviceId); i++){
                Random random = new Random();
                int index = random.nextInt(smallBS);
                int id = devices.get(index).getId();
                if (deviceIdToServiceNum.containsKey(id)){
                    if (deviceIdToServiceNum.get(id).containsKey(serviceId)){
                        deviceIdToServiceNum.get(id).put(serviceId,deviceIdToServiceNum.get(id).get(serviceId)+1);
                    }else{
                        deviceIdToServiceNum.get(id).put(serviceId,1);
                    }

                }else{
                    deviceIdToServiceNum.put(id,new HashMap<>());
                    deviceIdToServiceNum.get(id).put(serviceId,1);
                }

            }
        }
        for (int id :deviceIdToServiceNum.keySet()){
            Map<Integer,Integer>  typeToNum0 = new HashMap<>();
            typeToNum0.put(0,1);
            Map<Integer,Map<Integer,Integer>> service0To = new HashMap<>();
            service0To.put(0,typeToNum0);
            initDeploy.put(id,new HashMap<>());
            initDeploy.put(id,service0To);
            for(int serviceId : deviceIdToServiceNum.get(id).keySet())
            {
                Map<Integer,Integer>  typeToNum = new HashMap<>();
                typeToNum.put(0,deviceIdToServiceNum.get(id).get(serviceId));
                initDeploy.get(id).put(serviceId,typeToNum);

            }

        }

        for (int j = 0; j < smallBS; j++){
            if (!initDeploy.containsKey(devices.get(j).getId())){
                Map<Integer,Integer>  typeToNum0 = new HashMap<>();
                typeToNum0.put(0,1);
                Map<Integer,Map<Integer,Integer>> service0To = new HashMap<>();
                service0To.put(0,typeToNum0);
                initDeploy.put(devices.get(j).getId(),new HashMap<>());
                initDeploy.put(devices.get(j).getId(),service0To);
            }
        }

        return initDeploy;
    }

    private static Map<Integer,Map<Integer,Integer>> generateBlockToServiceDeploynumSimplePolicy(Map<Integer, Map<Integer, Double>> blockServicechainPro, int low, int high, List<Servicechain> servicechains){

        Map<Integer, ArrayList<Integer>> blockToOtherServicechains = new HashMap<>();
        for (int block : blockServicechainPro.keySet()){
            blockToOtherServicechains.put(block, new ArrayList<>());
            for (int i = 0 ;i< servicechains.size(); i++){
                if (!blockServicechainPro.get(block).keySet().contains(i)){
                    blockToOtherServicechains.get(block).add(i);
                }
            }
        }
        Map<Integer, Map<Integer, Double>> blockServicechainProComplete = new HashMap<>();
        for (int block : blockServicechainPro.keySet()){
            blockServicechainProComplete.put(block,new HashMap<>());
            for (int servicechain : blockServicechainPro.get(block).keySet()){
                if (servicechain!=-1){
                    blockServicechainProComplete.get(block).put(servicechain,blockServicechainPro.get(block).get(servicechain));
                }else{
                    double avgpro = blockServicechainPro.get(block).get(servicechain)/blockToOtherServicechains.get(block).size();
                    for (int i = 0; i < blockToOtherServicechains.get(block).size();i++){
                        blockServicechainProComplete.get(block).put(blockToOtherServicechains.get(block).get(i),avgpro);
                    }
                }
            }
        }



        Map<Integer,Map<Integer,Integer>> blockToServiceDeploynum = new HashMap<>();
        Map<Integer,Map<Integer,Double>> blockServiceNum = new HashMap<>(); // serviceid,

        Map<Integer, ArrayList<Integer>> serviceChainToSerIds = new HashMap<>();
        for (int i = 0 ; i< servicechains.size();i++){
            serviceChainToSerIds.put(servicechains.get(i).getServiceChainId(),servicechains.get(i).getMicroserIds());
        }
        int blocknum = blockServicechainPro.keySet().size();
        int blockRequestNum = (low + high)/2/blocknum;
        for (int i = 0; i< blocknum; i++){
            blockToServiceDeploynum.put(i,new HashMap<>());
            blockServiceNum.put(i,new HashMap<>());
        }
        for (int block : blockServicechainProComplete.keySet()){
            for (int serviceChain : blockServicechainProComplete.get(block).keySet()){

                for (int i = 0; i<serviceChainToSerIds.get(serviceChain).size();i++){
                    int serviceid = serviceChainToSerIds.get(serviceChain).get(i);
                    if (serviceid != 0){
                        double serReqnum = blockRequestNum * blockServicechainProComplete.get(block).get(serviceChain);
                        if (blockServiceNum.get(block).containsKey(serviceid)){
                            blockServiceNum.get(block).put(serviceid,blockServiceNum.get(block).get(serviceid) + serReqnum);
                        }else{
                            blockServiceNum.get(block).put(serviceid,serReqnum);
                        }
                    }
                }
            }
        }

        double totalnum = 0;
        for (int block : blockServiceNum.keySet()){
            for (int ser : blockServiceNum.get(block).keySet()){
                totalnum += blockServiceNum.get(block).get(ser);
            }
        }

        for (int block : blockServiceNum.keySet()){
            for (int ser : blockServiceNum.get(block).keySet()){
                double pro = blockServiceNum.get(block).get(ser)/totalnum;
                int num = (int)(200 * pro);
                blockToServiceDeploynum.get(block).put(ser,num);
            }
        }
        return blockToServiceDeploynum;

    }

    private static Map<Integer, Map<Integer, Map<Integer,Integer>>> createInitDeploymentSimplePolicy(Map<Integer, Map<Integer,Integer>> blockToServiceDeploynum, ArrayList<NetworkDevice> devices, int smallBS){
        Map<Integer, Map<Integer, Map<Integer,Integer>>> initDeploy = new HashMap<>(); // deviceid, serId, type, num
        // gateway
        Map<Integer, Map<Integer,Integer>> deviceIdToServiceNum = new HashMap<>();
        int groupnum = smallBS/blockToServiceDeploynum.keySet().size();
        for (int highLevelBlock : blockToServiceDeploynum.keySet()){
            Random random = new Random();
            for (int ser : blockToServiceDeploynum.get(highLevelBlock).keySet()){
                for (int i = 0; i < blockToServiceDeploynum.get(highLevelBlock).get(ser);i++){
                    int block = random.nextInt(groupnum) + highLevelBlock * groupnum;
                    int id = devices.get(block).getId();
                    if (deviceIdToServiceNum.containsKey(id)){
                        if (deviceIdToServiceNum.get(id).containsKey(ser)){
                            deviceIdToServiceNum.get(id).put(ser,deviceIdToServiceNum.get(id).get(ser)+1);
                        }else{
                            deviceIdToServiceNum.get(id).put(ser,1);
                        }

                    }else{
                        deviceIdToServiceNum.put(id,new HashMap<>());
                        deviceIdToServiceNum.get(id).put(ser,1);
                    }

                }
            }
        }

        for (int id :deviceIdToServiceNum.keySet()){
            Map<Integer,Integer>  typeToNum0 = new HashMap<>();
            typeToNum0.put(0,1);
            Map<Integer,Map<Integer,Integer>> service0To = new HashMap<>();
            service0To.put(0,typeToNum0);
            initDeploy.put(id,new HashMap<>());
            initDeploy.put(id,service0To);
            for(int serviceId : deviceIdToServiceNum.get(id).keySet())
            {
                Map<Integer,Integer>  typeToNum = new HashMap<>();
                typeToNum.put(0,deviceIdToServiceNum.get(id).get(serviceId));
                initDeploy.get(id).put(serviceId,typeToNum);

            }

        }

        for (int j = 0; j < smallBS; j++){
            if (!initDeploy.containsKey(devices.get(j).getId())){
                Map<Integer,Integer>  typeToNum0 = new HashMap<>();
                typeToNum0.put(0,1);
                Map<Integer,Map<Integer,Integer>> service0To = new HashMap<>();
                service0To.put(0,typeToNum0);
                initDeploy.put(devices.get(j).getId(),new HashMap<>());
                initDeploy.put(devices.get(j).getId(),service0To);
            }
        }

        return initDeploy;
    }


    public static int findCloud(ArrayList<NetworkDevice> networkDevices){
        if(networkDevices.get(networkDevices.size()-1).getIdentify() == "cloud"){
            return networkDevices.get(networkDevices.size()-1).getId();
        }
        return -1;
    }


}
