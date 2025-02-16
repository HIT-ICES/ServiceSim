package org.utils.lua;

import org.utils.FileUtilHelper;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.lua51.Lua51;
import party.iroiro.luajava.lua52.Lua52;
import party.iroiro.luajava.lua53.Lua53;
import party.iroiro.luajava.lua54.Lua54;
import party.iroiro.luajava.luajit.LuaJit;
import party.iroiro.luajava.value.LuaValue;

import static org.utils.lua.LuaVersion.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class LuaLoader implements Closeable {
    private static final String resourcePath = FileUtilHelper.getResourcePath("config");

    private String path;
    private Lua L;
    private Map<String, Object> params;

    private Lua getLua(LuaVersion version) {
        switch (version) {
            case LUA_5_1 -> {
                return new Lua51();
            }
            case LUA_5_2 -> {
                return new Lua52();
            }
            case LUA_5_3 -> {
                return new Lua53();
            }
            case LUA_5_4 -> {
                return new Lua54();
            }
            default -> {
                return new LuaJit();
            }
        }
    }

    private void setGlobal(String name, Object value) {
        getL().set(name, value);
    }

    private LuaValue getGlobal(String name) {
        return getL().get(name);
    }

    public void exec(String func) {
        Lua L = getL();
        L.run(func);
    }

    public void execute(String function) {
        exec(function + "()");
    }

    public void execute(String function, Object[] args) {
        // 随机生成一个参数名
        List<Map.Entry<String, Object>> params = new ArrayList<>();
        for (Object arg : args) {
            String uuid = "l" + UUID.randomUUID().toString().replaceAll("-", "");
            if (arg == null) {
                params.add(Map.entry(uuid, getL().fromNull()));
            } else {
                params.add(Map.entry(uuid, arg));
            }
            setGlobal(uuid, arg);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(function).append("(");
        for (Map.Entry<String, Object> param : params) {
            sb.append(param.getKey()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        exec(sb.toString());
    }

    public Map.Entry<String, LuaValue> executeWithReturn(String function) {
        String uuid = "l" + UUID.randomUUID().toString().replaceAll("-", "");
        exec(uuid + " = " + function + "()");
        return Map.entry(uuid, getGlobal(uuid));
    }

    public Map.Entry<String, LuaValue> executeWithReturn(String function, Object[] args) {
        // 随机生成一个参数名
        List<Map.Entry<String, Object>> params = new ArrayList<>();
        for (Object arg : args) {
            String uuid = "l" + UUID.randomUUID().toString().replaceAll("-", "");
            if (arg == null) {
                params.add(Map.entry(uuid, getL().fromNull()));
            } else {
                params.add(Map.entry(uuid, arg));
            }
            setGlobal(uuid, arg);
        }
        StringBuilder sb = new StringBuilder();
        String uuid = "l" + UUID.randomUUID().toString().replaceAll("-", "");
        sb.append(uuid).append(" = ").append(function).append("(");
        for (Map.Entry<String, Object> param : params) {
            sb.append(param.getKey()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        exec(sb.toString());
        return Map.entry(uuid, getGlobal(uuid));
    }

    public LuaLoader() {
        path = null;
        params = new HashMap<>();
        L = getLua(LUA_JIT);
    }

    public LuaLoader(LuaVersion version) {
        params = new HashMap<>();
        L = getLua(version);
    }

    public LuaLoader(String path) {
        this.path = path;
        params = new HashMap<>();
        L = getLua(LUA_JIT);
    }

    public LuaLoader(String path, LuaVersion version) {
        this.path = path;
        params = new HashMap<>();
        L = getLua(version);
    }

    public String getLuaCode(String fileName) {
        if (!fileName.endsWith(".lua")) {
            fileName += ".lua";
        }
        if (path == null) {
            return FileUtilHelper.readFileText(FileUtilHelper.append(resourcePath, fileName));
        } else {
            return FileUtilHelper.readFileText(FileUtilHelper.append(resourcePath, path, fileName));
        }
    }

    public void setParams(String key, Object value) {
        params.put(key, value);
    }

    public void openLibrary(String name) {
        L.openLibrary(name);
    }

    protected Lua getL() {
        return L;
    }

    @Override
    public void close() throws IOException {
        if (L != null) {
            L.close();
        }
    }
}
