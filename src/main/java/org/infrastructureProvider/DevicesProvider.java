package org.infrastructureProvider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.infrastructureProvider.entities.NetworkDevice;

import java.util.*;

@SuppressWarnings("unused")
public abstract class DevicesProvider implements DeviceProviderInterface {

    private List<? extends NetworkDevice> devices;

    private Map<Integer, Map<Integer, Integer>> routingTable; // now deviceId -> destination deviceId, next deviceId

    public void init() {}

    public DevicesProvider() {

    }

    public DevicesProvider(JSONObject config)
    {
        devices = new ArrayList<>();
        routingTable = new HashMap<>();

        // 读入devices：遍历devices数组，将读取到的每个deviceConfig传给NetworkDevice的构造方法
        JSONArray devicesArray = config.getJSONArray("devices");
        for (int i = 0; i < devicesArray.size(); i++) {
            JSONObject deviceConfig = devicesArray.getJSONObject(i);
            try
            {
                getDevices().add(new NetworkDevice(deviceConfig));
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        //读入routingTable：
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
