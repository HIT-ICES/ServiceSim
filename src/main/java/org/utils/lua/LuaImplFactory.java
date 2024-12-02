package org.utils.lua;

import java.util.List;

public class LuaImplFactory {
    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl) {
        return new LuaImpl<>(clazz, codes, impl);
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl, LuaVersion version) {
        return new LuaImpl<>(clazz, codes, impl, version);
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl, String path) {
        return new LuaImpl<>(clazz, codes, impl, path);
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes,
            String impl, String path, LuaVersion version) {
        return new LuaImpl<>(clazz, codes, impl, path, version);
    }

    public static String createLuaTemplate(Class<?> clazz, String name, String impl) {
        return LuaImpl.createLuaTemplate(clazz, name, impl);
    }
}
