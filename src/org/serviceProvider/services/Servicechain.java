package org.serviceProvider.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Servicechain {
    private int serviceChainId;

    /* a servicechain is a abstract of microservice chain */
    private ArrayList<Integer> microserIds;

    /* microservices stages: each servicechain has a set of relationships of service invoke*/
   // private Map<Integer,ArrayList<ServiceStage>> serviceStageMap;

    // 2022.12.07 add
    private Map<Integer, Map<Integer,ArrayList<ServiceStage>> > serviceStagesMap; // <serviceId, <pre_serviceId, servicestages>>

    /* cloudletLength for each function */
    private Map<Integer, Double> cloudletLengthList;

    /* pesNumber for each service */
    private Map<Integer, Integer> pesNumberList;

    /* memory for each service */
    private Map<Integer, Integer> memList;

//    /* a servicechain may have many appCloudlet instances */
//    public ArrayList<AppCloudlet> appCloudlets;

    public Servicechain(int serviceChainId) {
        this.serviceChainId = serviceChainId;
        microserIds = new ArrayList<Integer>();
        cloudletLengthList = new HashMap<>();
        pesNumberList = new HashMap<>();
        memList = new HashMap<>();
        //serviceStageMap = new HashMap<>();
        serviceStagesMap = new HashMap<>();
    }

    public int getServiceChainId() {
        return serviceChainId;
    }

    public void setServiceChainId(int serviceChainId) {
        this.serviceChainId = serviceChainId;
    }

    public ArrayList<Integer> getMicroserIds() {
        return microserIds;
    }

    public void setMicroserIds(ArrayList<Integer> microserIds) {
        this.microserIds = microserIds;
    }


    public Map<Integer, Double> getCloudletLengthList() {
        return cloudletLengthList;
    }

    public void setCloudletLengthList(Map<Integer, Double> cloudletLengthList) {
        this.cloudletLengthList = cloudletLengthList;
    }

    public Map<Integer, Integer> getPesNumberList() {
        return pesNumberList;
    }

    public void setPesNumberList(Map<Integer, Integer> pesNumberList) {
        this.pesNumberList = pesNumberList;
    }

    public Map<Integer, Integer> getMemList() {
        return memList;
    }

    public void setMemList(Map<Integer, Integer> memList) {
        this.memList = memList;
    }

    public Map<Integer, Map<Integer, ArrayList<ServiceStage>>> getServiceStagesMap() {
        return serviceStagesMap;
    }

    public void setServiceStagesMap(Map<Integer, Map<Integer, ArrayList<ServiceStage>>> serviceStagesMap) {
        this.serviceStagesMap = serviceStagesMap;
    }
}
