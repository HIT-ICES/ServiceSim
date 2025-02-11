package org.infrastructureProvider.entities;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkDeviceTest
{
    @Test
    public void testConstructorWithConfig() throws Exception
    {
        JSONObject config = ConfigFactory.getConfig("NetworkDeviceTest.yaml");
        NetworkDevice networkDevice = new NetworkDevice(config);
        assertNotNull(networkDevice);
    }
}