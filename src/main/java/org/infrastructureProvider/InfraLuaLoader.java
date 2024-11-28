package org.infrastructureProvider;

import org.utils.lua.BaseLuaLoader;
import org.utils.FileUtilHelper;
import org.utils.lua.LuaVersion;

public class InfraLuaLoader extends BaseLuaLoader {
    public InfraLuaLoader() {
        super("infra");
    }

    public InfraLuaLoader(LuaVersion version) {
        super("infra", version);
    }

    public InfraLuaLoader(String path) {
        super(FileUtilHelper.append("infra", path));
    }

    public InfraLuaLoader(String path, LuaVersion version) {
        super(FileUtilHelper.append("infra", path), version);
    }
}
