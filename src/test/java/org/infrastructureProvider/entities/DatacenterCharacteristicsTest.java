package org.infrastructureProvider.entities;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatacenterCharacteristicsTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("DatacenterCharacteristicsTest.yaml");
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(config);
        assertNotNull(characteristics);
    }
}