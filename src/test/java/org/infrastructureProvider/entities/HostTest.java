package org.infrastructureProvider.entities;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HostTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","HostTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        Host host = new Host(config);
        assertNotNull(host);
        System.out.println(host);
    }
}