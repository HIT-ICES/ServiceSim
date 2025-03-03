package org.serviceProvider;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.infrastructureProvider.DevicesProviderSimple;
import org.infrastructureProvider.DevicesProviderSimple1;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServiceProviderTest {
    @Test
    public void testConstructorWithConfig() {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest", "ServiceProviderTest.yaml");
        ProfileFactory factory = new ProfileFactory(config);
        factory.getInstance(config, "DevicesProvider", DevicesProviderSimple.class);
        ServiceProvider serviceProvider = (ServiceProvider) factory.getInstance(config, "ServiceProvider", ServiceProvider.class);
        assertNotNull(serviceProvider);
        System.out.println(serviceProvider);
        System.out.println(serviceProvider.getServiceChain().getFirst().getServiceStagesMap());
    }

    @Test
    public void testConstructorWithConfigAndStrategy() {
        JSONObject config = ConfigFactory.getUserConfig("TestExample1", "TestExample1.yaml");
        ProfileFactory factory = new ProfileFactory(config);
        factory.getInstance(config, "DevicesProvider", DevicesProviderSimple1.class);
        ServiceProvider serviceProvider = (ServiceProvider) factory.getInstance(config, "ServiceProvider", ServiceProvider.class);
        assertNotNull(serviceProvider);
        System.out.println(serviceProvider);
        System.out.println(serviceProvider.getServiceChain().getFirst().getServiceStagesMap());
    }
}