package org.infrastructureProvider.policies.vm;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VmSchedulerSpaceSharedTest {
    @Test
    public void testConstructorWithConfig() {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest", "VmSchedulerSpaceSharedTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        VmScheduler vmScheduler = new VmSchedulerSpaceShared(config);
        assertNotNull(vmScheduler);
        System.out.println(vmScheduler);
    }
}