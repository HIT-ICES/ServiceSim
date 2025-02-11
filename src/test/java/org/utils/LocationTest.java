package org.utils;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getConfig("LocationTest.yaml");
        Location location = new Location(config);
        assertNotNull(location);
    }
}