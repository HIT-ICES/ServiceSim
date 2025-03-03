package org.infrastructureProvider;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DevicesProviderSimple1Test {
    @Test
    public void testConstructorWithConfig() {
        JSONObject config = ConfigFactory.getUserConfig("TestExample1", "DevicesProvider.yaml");
        new ProfileFactory(config);
        DevicesProvider devicesProvider = new DevicesProviderSimple1(config);
        assertNotNull(devicesProvider);
        System.out.println(devicesProvider);
    }
}