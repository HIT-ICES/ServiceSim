package org.customBuilder;

import java.util.Map;

import org.customBuilder.exception.ParserException;
import org.yaml.snakeyaml.Yaml;   
import com.alibaba.fastjson.JSON;

public class YamlToJson {
    public static String convert(String yaml) {
        String jsonData;
        try {
            Yaml yamlParser = new Yaml();
            Map<String, Object> map = yamlParser.load(yaml);
            jsonData = JSON.toJSONString(map);               
        } catch (Exception e) {
            throw new ParserException("Error parsing YAML to JSON");
        }
        return jsonData;
    }
}
