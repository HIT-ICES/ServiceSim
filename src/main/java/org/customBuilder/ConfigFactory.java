package org.customBuilder;

import org.utils.FileUtilHelper;

import com.alibaba.fastjson.JSONObject;

public final class ConfigFactory {
    public static final String CONFIG_BASE_PATH = FileUtilHelper.getResourcePath("config");

    private static JSONObject getConfig(String path) {
        String config = FileUtilHelper.readFileText(FileUtilHelper.append(CONFIG_BASE_PATH, path));
        if (config == null || config.isEmpty()) {
            throw new IllegalArgumentException("Config file not found: " + path);
        }
        return JSONObject.parseObject(config);
    }

    public static Simulation getSimulation(String path) {
        return new Simulation(getConfig(path), "");
    }

    public static void main(String[] args) {
        Simulation simulation = ConfigFactory.getSimulation("template.json");
        System.out.println(simulation);
    }
}
