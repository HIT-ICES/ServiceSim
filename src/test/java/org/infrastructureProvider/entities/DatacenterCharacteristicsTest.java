package org.infrastructureProvider.entities;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatacenterCharacteristicsTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","DatacenterCharacteristicsTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(config);
        assertNotNull(characteristics);
        System.out.println(characteristics);
    }
}