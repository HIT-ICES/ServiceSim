package org.infrastructureProvider.policies.provisioners;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PeProvisionerSimpleTest
{
    @Test
    public void testConstructorWithConfig(){
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","PeProvisionerSimpleTest.yaml");
        PeProvisionerSimple peProvisioner = new PeProvisionerSimple(config);
        ProfileFactory profileFactory = new ProfileFactory(config);
        assertNotNull(peProvisioner);
        System.out.println(peProvisioner);
    }
}