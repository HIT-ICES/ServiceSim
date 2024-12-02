package org.infrastructureProvider;

import java.util.List;

import org.utils.FileUtilHelper;
import org.utils.lua.LuaImpl;
import org.utils.lua.LuaImplFactory;
import org.utils.lua.LuaVersion;

public class InfraLuaImplFactory extends LuaImplFactory {
    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl) {
        return LuaImplFactory.createLuaImpl(clazz, codes, impl, "infra");
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl, LuaVersion version) {
        return LuaImplFactory.createLuaImpl(clazz, codes, impl, "infra", version);
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl, String path) {
        return LuaImplFactory.createLuaImpl(clazz, codes, impl, FileUtilHelper.append("infra", path));
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes,
            String impl, String path, LuaVersion version) {
        return LuaImplFactory.createLuaImpl(clazz, codes, impl, FileUtilHelper.append("infra", path), version);
    }
}
