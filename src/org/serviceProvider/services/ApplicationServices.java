package org.serviceProvider.services;

import java.util.Map;

public abstract class ApplicationServices  {

    Map<Integer, Servicechain> servicechainList;

    public ApplicationServices() {

    }
    public abstract void createApplicationServices();

    public Map<Integer, Servicechain> getServicechainList() {
        return servicechainList;
    }

    public void setServicechainList(Map<Integer, Servicechain> servicechainList) {
        this.servicechainList = servicechainList;
    }

}
