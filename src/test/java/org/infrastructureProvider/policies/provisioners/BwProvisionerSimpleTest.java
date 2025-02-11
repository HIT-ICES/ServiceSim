package org.infrastructureProvider.policies.provisioners;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BwProvisionerSimpleTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("BwProvisionerSimpleTest.yaml");
        BwProvisioner bwProvisioner = new BwProvisionerSimple(config);
        assertNotNull(bwProvisioner);
    }

}