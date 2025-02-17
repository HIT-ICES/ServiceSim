package org.customBuilder;

import org.customBuilder.exception.ParserException;
import org.customBuilder.factory.ProfileFactory;
import org.infrastructureProvider.entities.Host;
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

    public static void main(String[] args) {
        ProfileFactory factory = new ProfileFactory();
        JSONObject profile = ConfigFactory.getConfig("template.yaml");
        ProfileFactory.setContext(profile, "");
        ProfileFactory.setFactory(profile, factory);
        Host host = new Host(profile);
        System.out.println(host);
    }
}