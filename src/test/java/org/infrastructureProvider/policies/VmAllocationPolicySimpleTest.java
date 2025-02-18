package org.infrastructureProvider.policies;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VmAllocationPolicySimpleTest {
    @Test
    public void testConstructorWithConfig() {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest", "VmAllocationPolicySimpleTest.yaml");
        new ProfileFactory(config);
        VmAllocationPolicy vmAllocationPolicy = new VmAllocationPolicySimple(config);
        assertNotNull(vmAllocationPolicy);
        System.out.println(vmAllocationPolicy);
    }
}