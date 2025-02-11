package org.infrastructureProvider;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.infrastructureProvider.entities.Host;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DevicesProviderSimpleTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("DevicesProviderSimpleTest.yaml");
        DevicesProvider devicesProvider = new DevicesProviderSimple();
        assertNotNull(devicesProvider);
    }
}