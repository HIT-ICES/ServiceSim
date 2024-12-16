package org.customBuilder.simulation.providers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.customBuilder.exception.ParserException;

import com.alibaba.fastjson.JSONArray;
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
    }

    private void createProvider(JSONObject jsonObject, String path, Class<?> clazz) {
        switch (this.type) {
            case BUILTIN -> {
                if (!jsonObject.containsKey("path")) {
                    throw new ParserException("Provider classpath not found: " + path);
                }
                String classpath = jsonObject.getString("path");
                this.provider = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz },
                        new BuiltInHandler(classpath, clazz));
                if (jsonObject.containsKey("params")) {
                    setParams(jsonObject.getJSONArray("params"), path + ".params");
                }
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
                        e.printStackTrace();
                        throw new ParserException("Provider children class not found: " + path);
                    }
                } else {
                    this.provider = jsonObject.getJSONObject("spec").toJavaObject(clazz);
                }
                System.out.println("provider: " + this.provider);
            }
            default -> throw new ParserException("Provider type not found: " + path);
        }
    }

    private void setParams(JSONArray jsonArray, String path) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            throw new ParserException("Provider params not found: " + path);
        }
        Method[] methods = this.provider.getClass().getDeclaredMethods();
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
                Provider param = new Provider(jsonObject.getJSONObject("value"), path + "." + name, paramType);
            } catch (Exception e) {
                throw new ParserException("Provider param setter not found: " + path);
            }
        }
    }

}

class BuiltInHandler implements InvocationHandler {
    private Object provider;

    public BuiltInHandler(String classpath, Class<?> clazz) {
        try {
            Class<?> providerClass = Class.forName(classpath);
            this.provider = providerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ParserException("Provider not found: " + classpath);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(provider, args);
    }

}
