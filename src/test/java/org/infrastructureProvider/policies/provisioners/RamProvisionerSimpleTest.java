package org.infrastructureProvider.policies.provisioners;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RamProvisionerSimpleTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","RamProvisionerSimpleTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        RamProvisioner ramProvisioner = new RamProvisionerSimple(config);
        assertNotNull(ramProvisioner);
        System.out.println(ramProvisioner);
    }
}