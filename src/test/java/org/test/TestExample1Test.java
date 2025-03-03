package org.test;

import com.alibaba.fastjson.JSONObject;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.enduser.EndUser;
import org.infrastructureProvider.DevicesProvider;
import org.infrastructureProvider.DevicesProviderSimple1;
import org.infrastructureProvider.entities.NetworkDevice;
import org.serviceProvider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TestExample1Test {
    private static final Logger logger = LoggerFactory.getLogger(TestExample1Test.class);

    public static void main(String[] args) {
        Log.printLine("Starting ServiceSim Simulation...");
        JSONObject config = ConfigFactory.getUserConfig("TestExample1", "TestExample1.yaml");
        try {
            int num_BSP = config.getInteger("num_BSP");
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = config.getBoolean("trace_flag");

            CloudSim.init(num_BSP, calendar, trace_flag);

            // 下面读取配置进行构造
            ProfileFactory factory = new ProfileFactory(config);
            DevicesProvider devicesProvider = (DevicesProvider) factory.getInstance(config, "DevicesProvider", DevicesProviderSimple1.class);
            ServiceProvider serviceProvider = (ServiceProvider) factory.getInstance(config, "ServiceProvider", ServiceProvider.class);

            // cloud
            // TODO: 这里可能需要写在配置里 如果这样则删除getInitInstance()
            int deployNum_cloud = 3;
            int type = 1;
            Map<Integer, Integer> typeToNum0 = new HashMap<>();
            typeToNum0.put(type, deployNum_cloud);
            Map<Integer, Map<Integer, Integer>> serviceToNum = new HashMap<>();
            for (int i = 1; i < 11; i++) {
                serviceToNum.put(i, typeToNum0);
            }
            int cloudId = findCloud((ArrayList<NetworkDevice>) devicesProvider.getDevices());
            serviceProvider.getInitInstance().put(cloudId, serviceToNum);

            // TODO: 由于有多个构造方法,暂时这么写
            JSONObject userConfig = config.getJSONObject("EndUser");
            EndUser endUser = new EndUser(
                    userConfig.getString("name"),
                    serviceProvider.getId(),
                    userConfig.getDouble("simLimited"),
                    userConfig.getInteger("low"),
                    userConfig.getInteger("high"),
                    userConfig.getInteger("timeUnit"),
                    userConfig.getDouble("timeInterval"),
                    userConfig.getInteger("serviceChainRange"),
                    userConfig.getInteger("blockRange"));

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

        } catch (Exception e) {
            logger.error("Unwanted errors happen", e);
            Log.printLine("Unwanted errors happen");
        }
    }

    // 来自TestExample1
    private static int findCloud(ArrayList<NetworkDevice> networkDevices) {
        if (Objects.equals(networkDevices.getLast().getIdentify(), "cloud")) {
            return networkDevices.getLast().getId();
        }
        return -1;
    }
}