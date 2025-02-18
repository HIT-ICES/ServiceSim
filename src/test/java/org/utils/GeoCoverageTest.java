package org.utils;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GeoCoverageTest {
    @Test
    public void testConstructorWithConfig() {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest", "GeoCoverageTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        GeoCoverage geoCoverage = new GeoCoverage(config);
        assertNotNull(geoCoverage);
        System.out.println(geoCoverage);
    }
}