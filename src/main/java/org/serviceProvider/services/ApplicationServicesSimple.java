package org.serviceProvider.services;


import org.enduser.networkPacket.NetworkConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApplicationServicesSimple extends ApplicationServices {

    Map<Integer, Map<Integer, ArrayList<Integer>>> serviceChains; // serviceChainId, dag (default: start service == 0)

    public ApplicationServicesSimple(Map<Integer, Map<Integer, ArrayList<Integer>>> serviceChains) {
        this.serviceChains = serviceChains;
        serviceChainList = new HashMap<>();
        createApplicationServices();
    }

    @Override
    public void createApplicationServices() {

        double data = 1000;

        for (int serviceChainId : serviceChains.keySet()) {
            ServiceChain servicechain = new ServiceChain(serviceChainId);
            serviceChainList.put(serviceChainId, servicechain);
            Map<Integer, ArrayList<Integer>> dia = serviceChains.get(serviceChainId);

            /* service0 stage */
            ArrayList<ServiceStage> Service0Stages = new ArrayList<>();
            Map<Integer, ArrayList<ServiceStage>> service0Stages = new HashMap<>();
            for (int i = 0; i < dia.get(0).size(); i++) {
                Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_SEND, i, dia.get(0).get(i), 0, data));
            }
            for (int i = 0; i < dia.get(0).size(); i++) {
                Service0Stages.add(new ServiceStage(NetworkConstants.WAIT_RECV, dia.get(0).size() + i, dia.get(0).get(i), 0, data));
            }
            service0Stages.put(-1, Service0Stages);
            serviceChainList.get(serviceChainId).getServiceStagesMap().put(0, service0Stages);
            supplyServiceChainInfo(serviceChainId, 0);
            for (int i = 0; i < dia.get(0).size(); i++) {
                generateServiceChain(dia, dia.get(0).get(i), 0, serviceChainId);
            }
        }

    }

    public void generateServiceChain(Map<Integer, ArrayList<Integer>> dia, int startService, int previousService, int serviceChainId) {

        double a = 0.2; // 0.2 MI
        double data = 1000;
        ArrayList<ServiceStage> ServiceStages = new ArrayList<>();
        Map<Integer, ArrayList<ServiceStage>> serviceStages = new HashMap<>();

        if (dia.get(startService) == null) {
            ServiceStages.add(new ServiceStage(NetworkConstants.EXECUTION, 0, startService, a, 0));
            ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_SEND, 1, previousService, 0, data));
            serviceStages.put(previousService, ServiceStages);
            serviceChainList.get(serviceChainId).getServiceStagesMap().put(startService, serviceStages);
            supplyServiceChainInfo(serviceChainId, startService);
            return;
        }
        ArrayList<Integer> edge = dia.get(startService);

        ServiceStages.add(new ServiceStage(NetworkConstants.EXECUTION, 0, startService, a, 0));
        for (int i = 0; i < edge.size(); i++) {
            ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_SEND, i + 1, edge.get(i), 0, data));
        }
        for (int i = 0; i < edge.size(); i++) {
            ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_RECV, edge.size() + i + 1, edge.get(i), 0, data));
        }
        ServiceStages.add(new ServiceStage(NetworkConstants.EXECUTION, edge.size() * 2 + 1, startService, a, 0));
        ServiceStages.add(new ServiceStage(NetworkConstants.WAIT_SEND, edge.size() * 2 + 2, previousService, 0, data));
        serviceStages.put(previousService, ServiceStages);
        serviceChainList.get(serviceChainId).getServiceStagesMap().put(startService, serviceStages);
        supplyServiceChainInfo(serviceChainId, startService);

        for (Integer integer : edge) {
            generateServiceChain(dia, integer, startService, serviceChainId);
        }
    }

    public void supplyServiceChainInfo(int serviceChainId, int serviceId) {

        int pesNum = 1;
        double cloudletLength = 0.0;
        int mem = 100;
        if (serviceId == 0) {
            mem = 10;
        }
        serviceChainList.get(serviceChainId).getMicroserviceIds().add(serviceId);
        serviceChainList.get(serviceChainId).getPesNumberList().put(serviceId, pesNum);
        serviceChainList.get(serviceChainId).getMemList().put(serviceId, mem);
        serviceChainList.get(serviceChainId).getCloudletLengthList().put(serviceId, cloudletLength);
    }
}
