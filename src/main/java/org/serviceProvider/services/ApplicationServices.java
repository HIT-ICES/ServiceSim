package org.serviceProvider.services;

import java.util.Map;

public abstract class ApplicationServices {

    Map<Integer, ServiceChain> serviceChainList;

    public ApplicationServices() {

    }

    public abstract void createApplicationServices();

    public Map<Integer, ServiceChain> getServiceChainList() {
        return serviceChainList;
    }

    public void setServiceChainList(Map<Integer, ServiceChain> serviceChainList) {
        this.serviceChainList = serviceChainList;
    }

}
