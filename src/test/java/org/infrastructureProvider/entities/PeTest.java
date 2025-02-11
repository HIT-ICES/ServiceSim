package org.infrastructureProvider.entities;
import com.alibaba.fastjson.JSONObject;
import org.customBuilder.ConfigFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PeTest
{
    @Test
    public void testConstructorWithConfig(){
        JSONObject config = ConfigFactory.getConfig("PeTest.yaml");
        Pe pe = new Pe(config);
        assertNotNull(pe);
    }
}