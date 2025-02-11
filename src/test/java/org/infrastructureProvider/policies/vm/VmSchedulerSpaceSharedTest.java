package org.infrastructureProvider.policies.vm;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VmSchedulerSpaceSharedTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("VmSchedulerSpaceSharedTest.yaml");
        VmScheduler vmScheduler = new VmSchedulerSpaceShared(config);
        assertNotNull(vmScheduler);
    }
}