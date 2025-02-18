package org.serviceProvider.services;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServiceChainTest {
    @Test
    public void testConstructorWithConfig() {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest", "ServiceChainTest.yaml");
        new ProfileFactory(config);
        ServiceChain serviceChain = new ServiceChain(config);
        assertNotNull(serviceChain);
        System.out.println(serviceChain);
    }
}