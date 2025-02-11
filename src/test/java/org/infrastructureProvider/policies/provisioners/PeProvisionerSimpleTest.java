package org.infrastructureProvider.policies.provisioners;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PeProvisionerSimpleTest
{
    @Test
    public void testConstructorWithConfig(){
        JSONObject config = ConfigFactory.getConfig("PeProvisionerSimpleTest.yaml");
        PeProvisionerSimple peProvisioner = new PeProvisionerSimple(config);
        assertNotNull(peProvisioner);
    }
}