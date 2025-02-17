package org.infrastructureProvider;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DevicesProviderSimpleTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","DevicesProviderSimpleTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        DevicesProvider devicesProvider = new DevicesProviderSimple(config);
        assertNotNull(devicesProvider);
        System.out.println(devicesProvider);
    }
}