package org.infrastructureProvider.policies;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VmAllocationPolicySimpleTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("VmAllocationPolicySimpleTest.yaml");
        VmAllocationPolicy vmAllocationPolicy = new VmAllocationPolicySimple(config);
        assertNotNull(vmAllocationPolicy);
    }
}