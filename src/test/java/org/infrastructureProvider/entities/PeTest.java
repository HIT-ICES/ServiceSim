package org.infrastructureProvider.entities;

import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.customBuilder.factory.ProfileFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PeTest {
    @Test
    public void testConstructorWithConfig() {
        JSONObject config = ConfigFactory.getUserConfig("LoadTest", "PeTest.yaml");
        ProfileFactory profileFactory = new ProfileFactory(config);
        Pe pe = new Pe(config);
        assertNotNull(pe);
        System.out.println(pe.toString());
    }
}