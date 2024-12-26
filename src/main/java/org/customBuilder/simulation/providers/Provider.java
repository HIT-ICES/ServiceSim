package org.customBuilder.simulation.providers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.customBuilder.ConfigFactory;
import org.customBuilder.exception.ParserException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Provider {
    private String name;
    private ProviderTypeEnum type;
    private Object provider;
    @Builder.Default
    private int replica = 0;

    public static Map<String, ProviderRecord> providerTree = new HashMap<>();

    public static void addProviderRecord(String path, JSONObject jsonObject, Class<?> clazz, Provider provider) {
        providerTree.put(path, ProviderRecord.builder().jsonObject(jsonObject).clazz(clazz).provider(provider).build());
    }

    public static ProviderRecord getProviderRecord(String path) {
        return providerTree.get(path);
    }

    public Provider(JSONObject jsonObject, String path, Class<?> clazz) {
        if (jsonObject == null || jsonObject.isEmpty()) {
            throw new ParserException("Provider not found: " + path);
        }
        if (!jsonObject.containsKey("type")) {
            throw new ParserException("Provider type not found: " + path);
        }
        if (jsonObject.containsKey("name")) {
            this.name = jsonObject.getString("name");
        }
        this.type = ProviderTypeEnum.fromString(jsonObject.getString("type"));
        createProvider(jsonObject, path, clazz);
        Provider.addProviderRecord(path, jsonObject, clazz, this);
    }

    public Provider(JSONObject jsonObject, String path, Class<?> clazz, int replica) {
        if (jsonObject == null || jsonObject.isEmpty()) {
            throw new ParserException("Provider not found: " + path);
        }
        if (!jsonObject.containsKey("type")) {
            throw new ParserException("Provider type not found: " + path);
        }
        if (jsonObject.containsKey("name")) {
            this.name = jsonObject.getString("name");
        }
        this.type = ProviderTypeEnum.fromString(jsonObject.getString("type"));
        this.replica = replica;
        createProvider(jsonObject, path, clazz);
        Provider.addProviderRecord(path, jsonObject, clazz, this);
    }

    private void createProvider(JSONObject jsonObject, String path, Class<?> clazz) {
        switch (this.type) {
            case TEMPLATE -> {
                if (!jsonObject.containsKey("replica")) {
                    throw new ParserException("Provider replica not found: " + path);
                }
                if (!jsonObject.containsKey("spec")) {
                    throw new ParserException("Provider spec not found: " + path);
                }
                int replica = jsonObject.getInteger("replica");
                if (replica <= 0) {
                    throw new ParserException("Provider replica unexpected: " + path);
                }
                List<Object> providers = new ArrayList<>();
                for (int i = 0; i < replica; i++) {
                    providers.add(new Provider(jsonObject.getJSONObject("spec"), path + "[" + i + "]", clazz, i)
                            .getProvider());
                }
                this.provider = providers;
            }
            case BUILTIN -> {
                if (!jsonObject.containsKey("path")) {
                    throw new ParserException("Provider classpath not found: " + path);
                }
                String classpath = jsonObject.getString("path");
                if (classpath == null || classpath.isEmpty()) {
                    throw new ParserException("Provider classpath not found: " + path);
                }
                try {
                    Class<?> providerClass = Class.forName(classpath);
                    if (providerClass.isInterface()) {
                        if (jsonObject.containsKey("args")) {
                            this.provider = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
                                    new BuiltInHandler(classpath, jsonObject.getJSONArray("args"), path + ".args",
                                            this));
                        } else {
                            this.provider = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
                                    new BuiltInHandler(classpath));
                        }
                    } else {
                        if (jsonObject.containsKey("args")) {
                            this.provider = new BuiltInHandler(classpath, jsonObject.getJSONArray("args"),
                                    path + ".args",
                                    this).getProvider();
                        } else {
                            this.provider = new BuiltInHandler(classpath).getProvider();
                        }
                    }
                } catch (ParserException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ParserException("Provider not found: " + classpath + " in " + path);
                }

                if (jsonObject.containsKey("params")) {
                    setParams(jsonObject.getJSONArray("params"), path + ".params");
                }
            }
            case RESOURCE -> {
                if (!jsonObject.containsKey("user")) {
                    throw new ParserException("Provider user not found: " + path);
                }
                if (!jsonObject.containsKey("path")) {
                    throw new ParserException("Provider path not found: " + path);
                }
                String user = jsonObject.getString("user");
                String resourcePath = jsonObject.getString("path");
                JSONObject resource = ConfigFactory.getUserConfig(user, resourcePath);
                if (resource == null || resource.isEmpty()) {
                    throw new ParserException("Provider resource not found: " + path);
                }
                this.provider = new Provider(resource, path, clazz).getProvider();
            }
            case REFRENCE -> {
                if (!jsonObject.containsKey("path")) {
                    throw new ParserException("Provider reference path not found: " + path);
                }
                String referencePath = jsonObject.getString("path");
                Provider reference = Provider.getProviderRecord(referencePath).getProvider();
                if (reference == null) {
                    throw new ParserException("Provider reference not found: " + path);
                }
                this.provider = reference.getProvider();
            }
            case CLONE -> {
                if (!jsonObject.containsKey("path")) {
                    throw new ParserException("Provider clone path not found: " + path);
                }
                String clonePath = jsonObject.getString("path");
                ProviderRecord record = Provider.getProviderRecord(clonePath);
                if (record == null) {
                    throw new ParserException("Provider clone not found: " + path);
                }
                Provider clone = new Provider(record.getJsonObject(), path, record.getClazz(),
                        record.getProvider().getReplica());
                this.provider = clone.getProvider();
            }
            case INLINE -> {
                if (!jsonObject.containsKey("spec")) {
                    throw new ParserException("Provider spec not found: " + path);
                }
                // 将spec转换为provider对象
                if (List.class.isAssignableFrom(clazz)) {
                    if (!jsonObject.containsKey("children")) {
                        throw new ParserException("Provider children definition not found: " + path);
                    }
                    try {
                        Class<?> childClazz = Class.forName(jsonObject.getString("children"));
                        this.provider = jsonObject.getJSONArray("spec").toJavaList(childClazz);
                    } catch (Exception e) {
                        throw new ParserException("Provider children class not found: " + path);
                    }
                } else {
                    this.provider = jsonObject.getJSONObject("spec").toJavaObject(clazz);
                }
            }
            default -> throw new ParserException("Provider type not found: " + path);
        }
    }

    private void setParams(JSONArray jsonArray, String path) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            throw new ParserException("Provider params not found: " + path);
        }
        Method[] methods = this.provider.getClass().getMethods();
        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject == null || jsonObject.isEmpty()) {
                throw new ParserException("Provider param not found: " + path + "[" + i + "]");
            }
            if (!jsonObject.containsKey("name")) {
                throw new ParserException("Provider param name not found: " + path + "[" + i + "]");
            }
            if (!jsonObject.containsKey("value")) {
                throw new ParserException("Provider param value not found: " + path + "[" + i + "]");
            }
            String name = jsonObject.getString("name");
            // 根据name获取setter方法
            try {
                String setter = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                Method method = null;
                Class<?> paramType = null;
                for (Method m : methods) {
                    if (m.getName().equals(setter)) {
                        // 获取参数类型
                        Class<?>[] parameterTypes = m.getParameterTypes();
                        if (parameterTypes.length == 1) {
                            paramType = parameterTypes[0];
                            method = m;
                            break;
                        }
                    }
                }
                if (method == null) {
                    throw new ParserException("Provider param setter not found: " + path + "." + name);
                }
                // 设置参数
                Object param = Provider.parseParams(jsonObject, path, name, paramType, this);
                method.invoke(this.provider, param);
            } catch (ParserException e) {
                throw e;
            } catch (Exception e) {
                throw new ParserException("Provider param setter not found: " + path);
            }
        }
    }

    public static Object parseParams(JSONObject jsonObject, String path, String name, Class<?> clazz,
            Provider provider) {
        try {
            if (jsonObject.getJSONObject("value") != null && !jsonObject.getJSONObject("value").isEmpty()) {
                Provider param = new Provider(jsonObject.getJSONObject("value"), path + "." + name, clazz);
                return param.getProvider();
            }
        } catch (JSONException | com.alibaba.fastjson2.JSONException e) {
            // do nothing
        }
        try {
            if (jsonObject.getJSONArray("value") != null && !jsonObject.getJSONArray("value").isEmpty()) {
                List<Object> params = new ArrayList<>();
                for (int j = 0; j < jsonObject.getJSONArray("value").size(); j++) {
                    Provider param = new Provider(jsonObject.getJSONArray("value").getJSONObject(j),
                            path + "." + name + "[" + j + "]", clazz);
                    params.add(param.getProvider());
                }
                // 判断一下是List还是原生数组
                if (List.class.isAssignableFrom(clazz)) {
                    return params;
                }
                return params.toArray();
            }
        } catch (JSONException | com.alibaba.fastjson2.JSONException e) {
            // do nothing
        }
        Object value = jsonObject.get("value");
        if (value instanceof String) {
            String str = (String) value;
            switch (str) {
                case "${autoint}" -> {
                    value = provider.getReplica();
                }
                default -> {
                    value = str;
                }
            }
        }
        if (clazz == int.class) {
            value = Integer.parseInt(value.toString());
        } else if (clazz == long.class) {
            value = Long.parseLong(value.toString());
        } else if (clazz == float.class) {
            value = Float.parseFloat(value.toString());
        } else if (clazz == double.class) {
            value = Double.parseDouble(value.toString());
        } else if (clazz == boolean.class) {
            value = Boolean.parseBoolean(value.toString());
        }
        return value;
    }
}

@Data
class BuiltInHandler implements InvocationHandler {
    private Object provider;

    public BuiltInHandler(String classpath) {
        try {
            Class<?> providerClass = Class.forName(classpath);
            this.provider = providerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ParserException("Provider not found: " + classpath);
        }
    }

    public BuiltInHandler(String classpath, JSONArray args, String path, Provider provider) {
        try {
            // 获取构造函数
            Class<?> providerClass = Class.forName(classpath);
            Constructor<?>[] constructors = providerClass.getConstructors();

            // 获取构造函数参数
            Map<String, JSONObject> argsmap = new HashMap<>();
            for (int i = 0; i < args.size(); i++) {
                JSONObject arg = args.getJSONObject(i);
                if (arg == null || arg.isEmpty()) {
                    throw new ParserException("Provider arg not found: " + path + "[" + i + "]");
                }
                if (!arg.containsKey("name")) {
                    throw new ParserException("Provider arg name not found: " + path + "[" + i + "]");
                }
                argsmap.put(arg.getString("name"), arg);
            }

            // 获取构造函数
            Constructor<?> constructor = null;
            for (Constructor<?> c : constructors) {
                Class<?>[] parameterTypes = c.getParameterTypes();
                if (parameterTypes.length == args.size()) {
                    boolean flag = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        String paramName = c.getParameters()[i].getName();
                        if (!argsmap.containsKey(paramName)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        constructor = c;
                        break;
                    }
                }
            }

            // 构造函数参数
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                String paramName = constructor.getParameters()[i].getName();
                // if (paramName.equals("characteristics")) {
                // System.out.println("debug");
                // }
                JSONObject arg = argsmap.get(paramName);
                if (arg == null || arg.isEmpty()) {
                    throw new ParserException("Provider arg not found: " + path + "." + paramName);
                }
                Object param = Provider.parseParams(arg, path, paramName, constructor.getParameterTypes()[i], provider);
                params.add(param);
            }
            this.provider = constructor.newInstance(params.toArray());
        } catch (ParserException e) {
            throw e;
        } catch (Exception e) {
            throw new ParserException("Provider not found: " + classpath + " in " + path);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(provider, args);
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
class ProviderRecord {
    private JSONObject jsonObject;
    private Class<?> clazz;
    private Provider provider;
}