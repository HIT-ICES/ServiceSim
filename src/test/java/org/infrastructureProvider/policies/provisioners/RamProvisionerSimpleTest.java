package org.infrastructureProvider.policies.provisioners;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RamProvisionerSimpleTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("RamProvisionerSimpleTest.yaml");
        RamProvisioner ramProvisioner = new RamProvisionerSimple(config);
        assertNotNull(ramProvisioner);
    }
}