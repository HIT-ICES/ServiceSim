package org.customBuilder;

import org.customBuilder.exception.ParserException;
import org.utils.FileUtilHelper;

import com.alibaba.fastjson.JSONObject;

public final class ConfigFactory {
    public static final String CONFIG_BASE_PATH = FileUtilHelper.getResourcePath("config");

    public static JSONObject getConfig(String path) {
        String config = FileUtilHelper.readFileText(FileUtilHelper.append(CONFIG_BASE_PATH, path));

        if (config == null || config.isEmpty()) {
            throw new IllegalArgumentException("Config file not found: " + path);
        }
        if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            config = YamlToJson.convert(config);
        }
        return JSONObject.parseObject(config);
    }
    
    public static JSONObject getUserConfig(String user, String path) {
        String config = FileUtilHelper.readFileText(FileUtilHelper.append(CONFIG_BASE_PATH, "users", user, path));
        if (config == null || config.isEmpty()) {
            throw new ParserException("Config file not found: " + path);
        }
        if (path.endsWith(".yaml") || path.endsWith(".yml")) {
            config = YamlToJson.convert(config);
        }
        return JSONObject.parseObject(config);
    }

    public static Simulation getSimulation(String path) {
        return new Simulation(getConfig(path), "");
    }

    public static void main(String[] args) {
        Simulation simulation = ConfigFactory.getSimulation("template.yaml");
        System.out.println(simulation);
    }
}