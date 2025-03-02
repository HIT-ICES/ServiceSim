package org.serviceProvider;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.infrastructureProvider.DevicesProviderSimple;
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
}