package org.infrastructureProvider.policies.vm;

import org.infrastructureProvider.InfraLuaLoader;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.policies.provisioners.PeProvisionerSimple;
import party.iroiro.luajava.Lua;
import party.iroiro.luajava.value.LuaValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VmLuaLoader extends InfraLuaLoader {
    private final String base = "main";
    private final String source;

    private void init() {
        getL().run(getLuaCode("main" + ".lua"));
    }

    public VmLuaLoader(String source) {
        super("vm");
        this.source = source;
        init();
    }

    public String init(String name, List<? extends Pe> peList) {
        Map.Entry<String, LuaValue> result = executeWithReturn(name + ":new", null, peList);
        return result.getKey();
    }

    public double getMaxAvailableMips(String vm) {
        Map.Entry<String, LuaValue> result = executeWithReturn(vm.toString() + ":getMaxAvailableMips");
        return result.getValue().toNumber();
    }

    public static void main(String[] args) {
        try (VmLuaLoader loader = new VmLuaLoader("main.lua")) {
            List<Pe> pes = new ArrayList<>();
            pes.add(new Pe(0, new PeProvisionerSimple(2142)));
            pes.add(new Pe(1, new PeProvisionerSimple(6480)));
            String vm = loader.init("VmScheduler", pes);
            double res = loader.getMaxAvailableMips(vm);
            System.out.println(res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
