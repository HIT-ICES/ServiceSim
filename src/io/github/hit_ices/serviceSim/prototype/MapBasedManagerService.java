package io.github.hit_ices.serviceSim.prototype;

import java.util.HashMap;
import java.util.Map;

public abstract class MapBasedManagerService<TEntity, TManager> implements ManagerService<TEntity, TManager> {
    private final Map<TEntity, TManager> schedulerMap = new HashMap<>();


    @Override
    public TManager getManager(TEntity managedEntity) {
        return schedulerMap.get(managedEntity);
    }

    @Override
    public void manage(TEntity managedEntity, TManager manager) {
        // Logic to initialize or associate a TManager with a TEntity
        // This could involve creating a new instance or modifying the existing one
        schedulerMap.put(managedEntity, manager);
    }

    @Override
    public void unManage(TEntity managedEntity) {
        schedulerMap.remove(managedEntity);
    }
}
