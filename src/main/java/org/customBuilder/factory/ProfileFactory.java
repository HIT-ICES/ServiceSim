package org.customBuilder.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.customBuilder.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utils.FileUtilHelper;
import org.utils.lua.LuaImpl;
import org.utils.lua.LuaImplFactory;
import org.utils.lua.LuaInterface;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ProfileFactory {
    private static final Logger logger = LoggerFactory.getLogger(ProfileFactory.class);

    private BuildTree buildTree;

    public ProfileFactory() {
        buildTree = new BuildTree();
    }

    /**
     * 初始化配置工厂
     * 用于第一次创建工厂，将其与配置文件绑定
     * @param config 配置信息
     */
    public ProfileFactory(JSONObject config) {
        buildTree = new BuildTree();
        ProfileFactory.setContext(config, "");
        ProfileFactory.setFactory(config, this);
    }

    /**
     * 获取上下文
     * @param profile 配置信息
     * @return 上下文 即本配置信息对应节点在构造树中的位置描述(父节点上下文+name) 上下文在构造时由父节点注入
     */
    public static String getContext(JSONObject profile) {
        String context = profile.getString("$context");
        if (context == null) {
            return "";
        }
        return context;
    }

    /**
     * 设置上下文
     * @param profile 配置信息
     * @param context 上下文
     */
    public static void setContext(JSONObject profile, String context) {
        profile.put("$context", context);
    }

    /**
     * 获取BuildType
     * @param profile 配置信息
     * @return BuildType
     */
    public static BuildType getType(JSONObject profile) {
        String type = profile.getString("$type");
        if (type == null) {
            return BuildType.BUILTIN;
        }
        return BuildType.valueOf(type.toUpperCase());
    }

    /**
     * 获取路由
     * @param context 上下文
     * @param ref (相对)路径
     *          /xxx.xxx.xxx 从根节点开始
     *          ./xxx.xxx.xxx 与当前节点同级
     *          xxx.xxx.xxx 与当前节点同级
     *          ../xxx.xxx.xxx 向上一级
     *          ../../xxx.xxx.xxx 向上两级
     * @return 根据上下文和引用得到的绝对路径
     */
    public static String getRoute(String context, String ref) {

        String route = null;
        if (ref.startsWith("/")) {
            route = ref.substring(1);
        } else if (ref.startsWith("./")) {
            route = context + "." + ref.substring(2);
        } else if (ref.startsWith("../")) {
            // 计算../的数量
            int parentCount = 0;
            while (ref.startsWith("../")) {
                parentCount++;
                ref = ref.substring(3);
            }
            String[] contextParts = context.split("\\.");
            if (parentCount > contextParts.length) {
                logger.error("引用路径超出范围: {}, 位于: {}", ref, context);
                throw new IllegalArgumentException("引用路径超出范围: " + ref);
            }
            StringBuilder newContext = new StringBuilder();
            for (int i = 0; i < contextParts.length - parentCount; i++) {
                newContext.append(contextParts[i]);
                if (i < contextParts.length - parentCount - 1) {
                    newContext.append(".");
                }
            }
            route = newContext + "." + ref;
        } else {
            route = context + "." + ref;
        }
        return route;
    }

    /**
     * 设置工厂
     * @param profile 配置信息
     * @param factory 工厂，该配置将由指定的工厂来构建
     */
    public static void setFactory(JSONObject profile, ProfileFactory factory) {
        profile.put("$factory", factory);
    }

    /**
     * 从配置信息中创建实例
     * @param self 父节点的配置信息
     * @param childName 子节点名称
     * @param defaultClass 默认类
     * @return 目标实例
     */
    public Object getInstance(JSONObject self, String childName, Class<?> defaultClass) {
        String context = getContext(self);
        Object child = self.get(childName);
        Object instance = null;
        if (child instanceof JSONObject) {
            instance =  fromJSONObject((JSONObject) child, context, childName, defaultClass);
        } else if (child instanceof JSONArray) {
            instance = fromJSONArray((JSONArray) child, context, childName, defaultClass);
        } else {
            logger.error("子节点不是JSONObject或JSONArray: {}, 位于: {}", childName, context);
            throw new IllegalArgumentException("子节点不是JSONObject或JSONArray: " + childName);
        }
        return instance;
    }

    // 从JSONObject中获取实例
    private Object fromJSONObject(JSONObject profile, String context, String name, Class<?> defaultClass) {
        BuildType type = getType(profile);
        Object instance = null;

        // 注入上下文
        if (name.startsWith("[") && name.endsWith("]")) {
            setContext(profile, context + name);
        } else {
            setContext(profile, context + "." + name);
        }
        // 注入工厂
        setFactory(profile, this);

        switch (type) {
            case BUILTIN -> instance = fromBuiltin(profile, context, name, defaultClass);
            case LUA -> instance = fromLua(profile, context, name, defaultClass);
            case RESOURCE -> instance = fromResource(profile, context, name, defaultClass);
            case TEMPLATE -> instance = fromTemplate(profile, context, name, defaultClass);
            case REFERENCE -> instance = fromRefrence(profile, context, name, defaultClass);
            case CLONE -> instance = fromClone(profile, context, name, defaultClass);
            default -> {
                logger.error("无法识别的类型: {}, 位于: {}", type, context + "." + name);
                throw new IllegalArgumentException("无法识别的类型: " + type);
            }
        }

        if (name.startsWith("[") && name.endsWith("]")) {
            buildTree.set(context + name, profile, instance);
        } else {
            buildTree.set(context + "." + name, profile, instance);
        }

        return instance;
    }

    // 从JSONArray中获取实例
    private Object fromJSONArray(JSONArray profile, String context, String name, Class<?> defaultClass) {
        List<Object> instances = new ArrayList<>();
        for (int i = 0; i < profile.size(); i++) {
            JSONObject child = profile.getJSONObject(i);
            instances.add(fromJSONObject(child, context + "." + name, "[" + i + "]", defaultClass));
        }

        buildTree.set(context + "." + name, profile, instances);

        return instances;
    }

    // java内部类
    private Object fromBuiltin(JSONObject profile, String context, String name, Class<?> defaultClass) {
        String className = profile.getString("$path");
        Class<?> clazz = null;

        String nowContext = "";
        if (name.startsWith("[") && name.endsWith("]")) {
            nowContext = context + name;
        } else {
            nowContext = context + "." + name;
        }

        // 获取类
        if (className != null) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                logger.error("无法找到类: {}, 位于: {}", className, nowContext);
                throw new IllegalArgumentException("无法找到类: " + className);
            }
        } else {
            clazz = defaultClass;
        }
        
        // 构造实例
        try {
            Constructor<?> constructor = clazz.getConstructor(JSONObject.class);
            return constructor.newInstance(profile);
        } catch (NoSuchMethodException e) {
            logger.error("无法找到JSONObject构造函数: {}, 位于: {}", clazz.getName(), nowContext);
            throw new IllegalArgumentException("无法找到构造函数: " + clazz.getName());
        } catch (InstantiationException e) {
            logger.error("无法实例化类: {}, 位于: {}", clazz.getName(), nowContext);
            throw new IllegalArgumentException("无法实例化类: " + clazz.getName());
        } catch (IllegalAccessException e) {
            logger.error("无法访问类: {}, 位于: {}", clazz.getName(), nowContext);
            throw new IllegalArgumentException("无法访问类: " + clazz.getName());
        } catch (IllegalArgumentException e) {
            logger.error("参数错误: {}, 位于: {}", clazz.getName(), nowContext);
            throw new IllegalArgumentException("参数错误: " + clazz.getName());
        } catch (InvocationTargetException e) {
            logger.error("调用目标错误: {}, 位于: {}", clazz.getName(), nowContext);
            throw new IllegalArgumentException("调用目标错误: " + clazz.getName());
        }
    }

    // lua脚本
    @SuppressWarnings("unchecked")
    private Object fromLua(JSONObject profile, String context, String name, Class<?> defaultClass) {
        String user = profile.getString("$user");
        String path = profile.getString("$path");
        String impl = profile.getString("$impl");
        String implName = profile.getString("$name");
        if (implName == null) {
            implName = path;
        }
        Class<? extends LuaInterface> clazz = null;
        try {
            clazz = (Class<? extends LuaInterface>) Class.forName(impl);
        } catch (ClassNotFoundException e) {
            logger.error("无法找到类: {}, 位于: {}", path, context + "." + name);
            throw new IllegalArgumentException("无法找到类: " + path);
        }
        LuaImpl<? extends LuaInterface> luaImpl = LuaImplFactory.createLuaImpl(clazz, List.of(path), implName, FileUtilHelper.append("users", user));
        luaImpl.getLuaImpl().init(profile);
        return luaImpl.getLuaImpl();
    }

    // 资源文件
    private Object fromResource(JSONObject profile, String context, String name, Class<?> defaultClass) {
        String user = profile.getString("$user");
        String path = profile.getString("$path");
        JSONObject resource = ConfigFactory.getUserConfig(user, path);

        // 注入一些额外信息
        for (String key : profile.keySet()) {
            if (!key.startsWith("$")) {
                resource.put(key, profile.get(key));
            }
        }

        return fromJSONObject(resource, context, name, defaultClass);
    }

    // 模板文件
    private Object fromTemplate(JSONObject profile, String context, String name, Class<?> defaultClass) {
        int count = profile.getIntValue("$count");
        JSONObject template = profile.getJSONObject("$template");
        JSONArray ids = profile.getJSONArray("$ids");
        String idType = profile.getString("$idType");
        if (idType == null) {
            idType = "auto";
        }
        List<Object> instances = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JSONObject child = (JSONObject) template.clone();
            if (ids != null) {
                switch (idType) {
                    case "uuid" -> {
                        String uuid = UUID.randomUUID().toString();
                        for (int j = 0; j < ids.size(); j++) {
                            child.put(ids.getString(j), uuid);
                        }
                    }
                    case "replace" -> {
                        Integer start = profile.getInteger("$start");
                        Integer step = profile.getInteger("$step");
                        if (start == null) start = 0;
                        if (step == null) step = 1;
                        String uuid = UUID.randomUUID().toString();
                        for (int j = 0; j < ids.size(); j++) {
                            JSONObject id = ids.getJSONObject(j);
                            Object key = id.get("$key");
                            List<String> keys = new ArrayList<>();
                            if (key != null) {
                                if (key instanceof String) {
                                    keys.add((String) key);
                                } else if (key instanceof JSONArray) {
                                    JSONArray keyArray = (JSONArray) key;
                                    for (int k = 0; k < keyArray.size(); k++) {
                                        keys.add(keyArray.getString(k));
                                    }
                                }
                            }
                            String replace = id.getString("$replace");
                            replace = replace.replace("{id}", String.valueOf(start + i * step));
                            replace = replace.replace("{uuid}", uuid);
                            for (String k : keys) {
                                child.put(k, replace);
                            }
                        }
                    }
                    default -> { // "auto"
                        Integer start = profile.getInteger("$start");
                        Integer step = profile.getInteger("$step");
                        if (start == null) start = 0;
                        if (step == null) step = 1;
                        for (int j = 0; j < ids.size(); j++) {
                            child.put(ids.getString(j), start + i * step);
                        }
                    }
                }
            }
            instances.add(fromJSONObject(child, context + "." + name, "[" + i + "]", defaultClass));
        }
        return instances;
    }

    // 引用
    private Object fromRefrence(JSONObject profile, String context, String name, Class<?> defaultClass) {
        String ref = profile.getString("$path");
        String route = getRoute(context, ref);
        Node node = buildTree.get(route);
        if (node == null) {
            logger.error("无法找到引用: {}, 位于: {}", ref, context + "." + name);
            throw new IllegalArgumentException("无法找到引用: " + ref);
        }
        Object instance = node.getInstance();
        if (instance == null) {
            logger.error("引用未实例化: {}, 位于: {}", ref, context + "." + name);
            throw new IllegalArgumentException("引用未实例化: " + ref);
        }
        return instance;
    }

    // 克隆
    private Object fromClone(JSONObject profile, String context, String name, Class<?> defaultClass) {
        String clone = profile.getString("$path");
        String route = getRoute(context, clone);
        Node node = buildTree.get(route);
        if (node == null) {
            logger.error("无法找到克隆对象: {}, 位于: {}", clone, context + "." + name);
            throw new IllegalArgumentException("无法找到克隆对象: " + clone);
        }
        Object cloneProfile = node.getProfile();
        if (cloneProfile == null) {
            logger.error("无法获取克隆对象配置: {}, 位于: {}", clone, context + "." + name);
            throw new IllegalArgumentException("无法获取克隆对象配置: " + clone);
        }
        Object instance = null;
        if (cloneProfile instanceof JSONObject) {
            JSONObject cloneProfileObject = (JSONObject) cloneProfile;
            instance = fromJSONObject(cloneProfileObject, context, name, defaultClass);
        } else if (cloneProfile instanceof JSONArray) {
            JSONArray cloneProfileArray = (JSONArray) cloneProfile;
            instance = fromJSONArray(cloneProfileArray, context, name, defaultClass);
        } else {
            logger.error("克隆对象不是JSONObject或JSONArray: {}, 位于: {}", clone, context + "." + name);
            throw new IllegalArgumentException("克隆对象不是JSONObject或JSONArray: " + clone);
        }
        return instance;
    }
}

