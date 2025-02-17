package org.infrastructureProvider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.customBuilder.factory.ProfileFactory;
import org.infrastructureProvider.entities.NetworkDevice;

import java.util.*;

@SuppressWarnings("unused")
public abstract class DevicesProvider implements DeviceProviderInterface {

    private List<? extends NetworkDevice> devices;

    private Map<Integer, Map<Integer, Integer>> routingTable; // now deviceId -> destination deviceId, next deviceId

    public void init() {}

    public DevicesProvider() {

    }

    @SuppressWarnings("unchecked")
    public DevicesProvider(JSONObject config)
    {
        // 从配置中读取工厂
        ProfileFactory factory = config.getObject("$factory", ProfileFactory.class);

        // 利用工厂方法读取devices
        devices = (List<? extends NetworkDevice>)factory.getInstance(config,"devices",NetworkDevice.class);

        //读入routingTable：
        routingTable = new HashMap<>();
        JSONObject routingTableConfig = config.getJSONObject("routingTable");
        // 获取 routingTable 中的所有外层键
        for(String outKey: routingTableConfig.keySet())
        {
            JSONObject outConfig = routingTableConfig.getJSONObject(outKey);
            // 创建Map，构造
            Map<Integer, Integer> inMap = new HashMap<>();
            for(String inKey: outConfig.keySet())
            {
                inMap.put(Integer.parseInt(inKey), Integer.parseInt(outConfig.getString(inKey)));
            }
            routingTable.put(Integer.parseInt(outKey), inMap);
        }
    }

    public abstract void createDevices();
    // create NetworkDevices

    // create routingTable

    // add routingTable to the devices

    @SuppressWarnings("unchecked")
    public <T extends NetworkDevice> List<T> getDevices() {
        return (List<T>) devices;
    }

    public <T extends NetworkDevice> void setDevices(List<T> devices) {
        this.devices = devices;
    }


    public Map<Integer, Map<Integer, Integer>> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(Map<Integer, Map<Integer, Integer>> routingTable) {
        this.routingTable = routingTable;
    }


}
