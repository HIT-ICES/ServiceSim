package org.utils;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeoCoverageTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("GeoCoverageTest.yaml");
        GeoCoverage geoCoverage = new GeoCoverage(config);
        assertNotNull(geoCoverage);
    }
}