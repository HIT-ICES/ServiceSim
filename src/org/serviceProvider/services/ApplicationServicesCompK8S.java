package org.serviceProvider.services;


import org.enduser.networkPacket.NetworkConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApplicationServicesCompK8S extends ApplicationServices {

    Map<Integer, Map<Integer, ArrayList<Integer>>> serviceChains; // serviceChainId, dag (default: start service == 0)

    public ApplicationServicesCompK8S(Map<Integer, Map<Integer, ArrayList<Integer>>> serviceChains) {
        this.serviceChains = serviceChains;
        servicechainList = new HashMap<>();
        createApplicationServices();
    }

    @Override
    public void createApplicationServices() {

        double data = 10 * 1024;

        for (int servicechainId : serviceChains.keySet()){
            Servicechain servicechain = new Servicechain(servicechainId);
            servicechainList.put(servicechainId,servicechain);
            Map<Integer, ArrayList<Integer>> dia = serviceChains.get(servicechainId);

            /* service0 stage */
            ArrayList<ServiceStage> Service0Stages = new ArrayList<ServiceStage>();
            Map<Integer,ArrayList<ServiceStage>> service0Stages = new HashMap<>();
            for (int i = 0 ; i < dia.get(0).size();i++){
                Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND,i,dia.get(0).get(i),0,data));
            }
            for (int i = 0 ; i < dia.get(0).size();i++){
                Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV,dia.get(0).size()+i,dia.get(0).get(i),0,data));
            }
            service0Stages.put(-1,Service0Stages);
            servicechainList.get(servicechainId).getServiceStagesMap().put(0,service0Stages);
            supplyServiceChainInfo(servicechainId, 0);
            for (int i = 0 ; i < dia.get(0).size();i++){
                generateServiceChain(dia, dia.get(0).get(i),0,servicechainId);
            }
        }

    }

    public void generateServiceChain(Map<Integer, ArrayList<Integer>> dia, int startService,int previousService,int servicechainId){

        double a = 0.01; // 0.2 MI
        double data = 10*1024;
        ArrayList<ServiceStage> ServiceStages = new ArrayList<ServiceStage>();
        Map<Integer, ArrayList<ServiceStage>> serviceStages = new HashMap<>();

        if (dia.get(startService) == null){
            ServiceStages.add(new ServiceStage(NetworkConstants.EXECUTION,0,startService,a,0));
            ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_SEND,1,previousService,0,data));
            serviceStages.put(previousService,ServiceStages);
            servicechainList.get(servicechainId).getServiceStagesMap().put(startService,serviceStages);
            supplyServiceChainInfo(servicechainId, startService);
            return ;
        }
        ArrayList<Integer> edge = dia.get(startService);

        ServiceStages.add(new ServiceStage(NetworkConstants.EXECUTION,0,startService,a,0));
        for (int i = 0 ; i < edge.size();i++){
            ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_SEND,i+1,edge.get(i),0,data));
            //ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_RECV,i*2+2,edge.get(i),0,data));

        }
        for (int i = 0 ; i < edge.size();i++){
            ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_RECV,edge.size()+i+1,edge.get(i),0,data));
        }
//        ServiceStages.add(new ServiceStage(NetworkConstants.EXECUTION,edge.size()*2+1,startService,a,0));
        ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_SEND,edge.size()*2+2,previousService,0,data));
        serviceStages.put(previousService,ServiceStages);
        servicechainList.get(servicechainId).getServiceStagesMap().put(startService,serviceStages);
        supplyServiceChainInfo(servicechainId, startService);

        for (int i = 0 ; i < edge.size();i++){
            generateServiceChain(dia, edge.get(i),startService,servicechainId);
        }
        return ;
    }

    public void supplyServiceChainInfo(int servicechainId, int serviceId){

        int pesNum = 1;
        double cloudletLength = 0.0;
        int mem = 1;
        if (serviceId == 0){
            mem = 1;
        }
        servicechainList.get(servicechainId).getMicroserIds().add(serviceId);
        servicechainList.get(servicechainId).getPesNumberList().put(serviceId,pesNum);
        servicechainList.get(servicechainId).getMemList().put(serviceId,mem);
        servicechainList.get(servicechainId).getCloudletLengthList().put(serviceId,cloudletLength);
    }
}
