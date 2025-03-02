package org.serviceProvider.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.customBuilder.factory.ProfileFactory;
import org.enduser.networkPacket.NetworkConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"CommentedOutCode"})
public class ServiceChain {
    private int serviceChainId;

    /* a serviceChain is an abstract of microservice chain */
    private ArrayList<Integer> microserviceIds;

    /* microservices stages: each serviceChain has a set of relationships of service invoke*/
    // private Map<Integer, ArrayList<ServiceStage>> serviceStageMap;

    // 2022.12.07 add
    private Map<Integer, Map<Integer, ArrayList<ServiceStage>>> serviceStagesMap; // <serviceId, <pre_serviceId, serviceStages>>

    /* cloudletLength for each function */
    private Map<Integer, Double> cloudletLengthList;

    /* pesNumber for each service */
    private Map<Integer, Integer> pesNumberList;

    /* memory for each service */
    private Map<Integer, Integer> memList;

//    /* a serviceChain may have many appCloudlet instances */
//    public ArrayList<AppCloudlet> appCloudlets;

    public ServiceChain(int serviceChainId) {
        this.serviceChainId = serviceChainId;
        microserviceIds = new ArrayList<>();
        cloudletLengthList = new HashMap<>();
        pesNumberList = new HashMap<>();
        memList = new HashMap<>();
        //serviceStageMap = new HashMap<>();
        serviceStagesMap = new HashMap<>();
    }

    public ServiceChain(JSONObject config){
        ProfileFactory factory = config.getObject("$factory", ProfileFactory.class);
        this.serviceChainId = config.getInteger("id");
        microserviceIds = new ArrayList<>();
        cloudletLengthList = new HashMap<>();
        pesNumberList = new HashMap<>();
        memList = new HashMap<>();
        //serviceStageMap = new HashMap<>();
        serviceStagesMap = new HashMap<>();//服务id->前置服务id->服务阶段

        //下面读取ServiceChain的配置
        JSONArray microservices = config.getJSONArray("Microservices");
        for(int i = 0; i < microservices.size(); i++){
            // 每个微服务的基本配置
            JSONObject microservice = microservices.getJSONObject(i);
            int serviceId = microservice.getInteger("serviceId");
            microserviceIds.add(serviceId);
            cloudletLengthList.put(serviceId, microservice.getDouble("cloudletLength"));
            pesNumberList.put(serviceId, microservice.getInteger("pesNumber"));
            memList.put(serviceId, microservice.getInteger("mem"));
            int preServiceId = microservice.getInteger("preServiceId");
            Map<Integer, ArrayList<ServiceStage>> preToStagesMap = new HashMap<>();
            // 下面读取该微服务的服务阶段信息
            JSONArray stages = microservice.getJSONArray("serviceStages");
            ArrayList<ServiceStage> serviceStages = new ArrayList<>();
            for(int j = 0; j < stages.size(); j++){
                JSONObject stage = stages.getJSONObject(j);
                int type = NetworkConstants.getValueByType(stage.getString("type"));
                int peer = stage.getInteger("peer");
                double stageCloudletLength = stage.getDouble("stageCloudletLength");
                double data = stage.getDouble("data");
                serviceStages.add(new ServiceStage(j, type, peer, stageCloudletLength, data)); // 默认id从0开始
            }
            // 构建映射：服务id->前置服务id->服务阶段
            preToStagesMap.put(preServiceId,serviceStages);
            serviceStagesMap.put(serviceId,preToStagesMap);
        }
    }

    public int getServiceChainId() {
        return serviceChainId;
    }

    public void setServiceChainId(int serviceChainId) {
        this.serviceChainId = serviceChainId;
    }

    public ArrayList<Integer> getMicroserviceIds() {
        return microserviceIds;
    }

    public void setMicroserviceIds(ArrayList<Integer> microserviceIds) {
        this.microserviceIds = microserviceIds;
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
