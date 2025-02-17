package org.utils;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationTest
{
    @Test
    public void testConstructorWithConfig()
    {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest","LocationTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        Location location = new Location(config);
        assertNotNull(location);
        System.out.println(location);
    }
}