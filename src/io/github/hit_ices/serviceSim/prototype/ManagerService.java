package io.github.hit_ices.serviceSim.prototype;

public interface ManagerService<TEntity, TManager> {
    TManager getManager(TEntity mangedEntity);

    void manage(TEntity mangedEntity, TManager manager);

    void unManage(TEntity managedEntity);
}
