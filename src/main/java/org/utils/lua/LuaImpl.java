package org.utils.lua;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class LuaImpl<T> {
    private final T luaImpl;
    private final LuaHandler luaHandler;

    public static String createLuaTemplate(Class<?> clazz, String name, String impl) {
        StringBuilder sb = new StringBuilder();
        Method[] methods = clazz.getDeclaredMethods();
        boolean hasImpl = impl != null && !impl.isEmpty();
        Method newMethod = null;
        List<Method> classMethods = new ArrayList<>();
        for (Method method : methods) {
            if (method.getName().equals("init")) {
                newMethod = method;
            } else {
                classMethods.add(method);
            }
        }

        // Meta Class
        sb.append("-- Meta Class\n");
        if (!hasImpl) {
            sb.append(name).append(" = {\n");
            sb.append("-- properties\n");
            sb.append("}\n");
        } else {
            sb.append(name).append(" = ").append(impl).append(":new()\n");
        }

        // class method new
        sb.append("\n-- class method new\n");
        // get the parameters of the new method
        List<String> paramNames = new ArrayList<>();
        paramNames.add("o");
        if (newMethod != null) {
            Parameter[] parameters = newMethod.getParameters();
            for (Parameter parameter : parameters) {
                paramNames.add(parameter.getName());
            }
        }
        
        sb.append("function ").append(name).append(":new(");
        sb.append(String.join(", ", paramNames));
        sb.append(")\n");
        sb.append("\to = o or ");
        if (!hasImpl) {
            sb.append("{}\n");
        } else {
            sb.append(impl).append(":new(");
            sb.append(String.join(", ", paramNames));
            sb.append(")\n");
        }
        sb.append("\tsetmetatable(o, self)\n");
        sb.append("\tself.__index = self\n");
        sb.append("\t-- initialize the object\n");
        sb.append("\treturn o\n");
        sb.append("end\n");

        // class other methods
        for (Method method : classMethods) {
            sb.append("\n-- class method ").append(method.getName()).append("\n");
            sb.append("function ").append(name).append(":").append(method.getName()).append("(");
            Parameter[] parameters = method.getParameters();
            List<String> paramList = new ArrayList<>();
            for (Parameter parameter : parameters) {
                paramList.add(parameter.getName());
            }
            sb.append(String.join(", ", paramList));
            sb.append(")\n");
            sb.append("\t-- TODO: implement the method\n");
            sb.append("end\n");
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public LuaImpl(Class<T> clazz, List<String> codes, String impl) {
        luaHandler = new LuaHandler(codes, impl);
        luaImpl = (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[] { clazz },
                luaHandler);
    }
    
    @SuppressWarnings("unchecked")
    public LuaImpl(Class<T> clazz, List<String> codes, String impl, LuaVersion version) {
        luaHandler = new LuaHandler(codes, impl, version);
        luaImpl = (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[] { clazz },
                luaHandler);
    }

    @SuppressWarnings("unchecked")
    public LuaImpl(Class<T> clazz, List<String> codes, String impl, String path) {
        luaHandler = new LuaHandler(codes, impl, path);
        luaImpl = (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[] { clazz },
                luaHandler);
    }
    
    @SuppressWarnings("unchecked")
    public LuaImpl(Class<T> clazz, List<String> codes, String impl, String path, LuaVersion version) {
        luaHandler = new LuaHandler(codes, impl, path, version);
        luaImpl = (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[] { clazz },
                luaHandler);
    }

    public T getLuaImpl() {
        return luaImpl;
    }
}

class LuaHandler implements InvocationHandler {
    private final LuaLoader luaLoader;
    private final List<String> codes;
    private final String impl;
    private String obj;

    public LuaHandler(List<String> codes, String impl) {
        this.luaLoader = new LuaLoader();
        this.codes = codes;
        this.impl = impl;
    }

    public LuaHandler(List<String> codes, String impl, LuaVersion version) {
        this.luaLoader = new LuaLoader(version);
        this.codes = codes;
        this.impl = impl;
    }

    public LuaHandler(List<String> codes, String impl, String path) {
        this.luaLoader = new LuaLoader(path);
        this.codes = codes;
        this.impl = impl;
    }

    public LuaHandler(List<String> codes, String impl, String path, LuaVersion version) {
        this.luaLoader = new LuaLoader(path, version);
        this.codes = codes;
        this.impl = impl;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        System.out.println("Invoking impl: " + impl);
        System.out.println("Invoking method: " + methodName);
        if (methodName.equals("init")) {
            for (String code : codes) {
                luaLoader.exec(luaLoader.getLuaCode(code));
            }
            Object[] params = new Object[args.length + 1];
            params[0] = null;
            System.arraycopy(args, 0, params, 1, args.length);
            obj = luaLoader.executeWithReturn(impl + ":new", params).getKey();
            return null;
        }
        if (obj == null) {
            throw new RuntimeException("Lua object is not initialized");
        }
        // 判断是否有返回值
        if (method.getReturnType().equals(void.class)) {
            if (args == null) {
                luaLoader.execute(obj + ":" + methodName);
                return null;
            }
            luaLoader.execute(obj + ":" + methodName, args);
            return null;
        }
        if (args == null) {
            return luaLoader.executeWithReturn(obj + ":" + methodName).getValue().toJavaObject();
        }
        return luaLoader.executeWithReturn(obj + ":" + methodName, args).getValue().toJavaObject();
    }
}
