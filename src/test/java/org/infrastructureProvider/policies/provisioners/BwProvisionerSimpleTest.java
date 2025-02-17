package org.infrastructureProvider.policies.provisioners;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BwProvisionerSimpleTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","BwProvisionerSimpleTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        BwProvisioner bwProvisioner = new BwProvisionerSimple(config);
        assertNotNull(bwProvisioner);
        System.out.println(profileFactory);
    }

}