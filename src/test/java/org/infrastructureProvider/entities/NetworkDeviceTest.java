package org.infrastructureProvider.entities;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkDeviceTest
{
    @Test
    public void testConstructorWithConfig() throws Exception
    {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","NetworkDeviceTest.yaml");
        ProfileFactory  profileFactory = new ProfileFactory(config);
        NetworkDevice networkDevice = new NetworkDevice(config);
        assertNotNull(networkDevice);
        System.out.println(networkDevice);
    }
}