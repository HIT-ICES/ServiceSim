package org.infrastructureProvider.policies.vm;

import java.util.ArrayList;
import java.util.List;

import org.infrastructureProvider.InfraLuaImplFactory;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.policies.provisioners.PeProvisionerSimple;
import org.utils.FileUtilHelper;
import org.utils.lua.LuaImpl;
import org.utils.lua.LuaImplFactory;
import org.utils.lua.LuaVersion;

public class VmLuaImplFactory extends LuaImplFactory {
    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl) {
        return InfraLuaImplFactory.createLuaImpl(clazz, codes, impl, "vm");
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl, LuaVersion version) {
        return InfraLuaImplFactory.createLuaImpl(clazz, codes, impl, "vm", version);
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes, String impl, String path) {
        return InfraLuaImplFactory.createLuaImpl(clazz, codes, impl, FileUtilHelper.append("vm", path));
    }

    public static <T> LuaImpl<T> createLuaImpl(Class<T> clazz, List<String> codes,
            String impl, String path, LuaVersion version) {
        return InfraLuaImplFactory.createLuaImpl(clazz, codes, impl, FileUtilHelper.append("vm", path), version);
    }

    public static void main(String[] args) {
        LuaImpl<VmInterface> impl = VmLuaImplFactory.createLuaImpl(VmInterface.class, List.of("main"), "VmScheduler");
        List<Pe> pes = new ArrayList<>();
        pes.add(new Pe(0, new PeProvisionerSimple(2142)));
        pes.add(new Pe(1, new PeProvisionerSimple(6480)));
        impl.getLuaImpl().init(pes);
        System.out.println(impl.getLuaImpl().getMaxAvailableMips());
        System.out.println(LuaImplFactory.createLuaTemplate(VmInterface.class, "VmSchedulerSpaceShared", "VmScheduler"));
    }
}
