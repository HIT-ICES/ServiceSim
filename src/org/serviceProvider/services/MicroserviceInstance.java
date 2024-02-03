package org.serviceProvider.services;

import org.enduser.networkPacket.NetworkCloudlet;
import org.infrastructureProvider.entities.Vm;
import org.infrastructureProvider.policies.CloudletScheduler;
import org.utils.PolicyConstants;

import java.util.ArrayList;

public class MicroserviceInstance extends Vm {

    public int serviceId;

    /* Instance status */
//    Requested = 0;
//    Started = 1;
//    Quarantined = 2; // for Instance migration or service replacement
//    Destroyed = 3;

    public int status;

    public double requestTime;
    public double startTime;
    public double destroyTime;
    public double lifeTime; // for budget

    public double delayInStartUp;

    public int configurationType;
    public int purchaseType;
    public double price;
    public double bill;

    /* for cloudlets management */
    public ArrayList<NetworkCloudlet> cloudletList;


    public double lastUpateTime; // record last cloudlets processing time

    // for service0
    public int serviceChainId; // usually do not be used


    /**
     * Creates a new VMCharacteristics object.
     *
     * @param id                unique ID of the VM
     * @param userId            ID of the VM's owner
     * @param mips              the mips
     * @param numberOfPes       amount of CPUs
     * @param ram               amount of ram
     * @param bw                amount of bandwidth
     * @param size              amount of storage
     * @param vmm               virtual machine monitor
     * @param cloudletScheduler cloudletScheduler policy for cloudlets
     * @pre id >= 0
     * @pre userId >= 0
     * @pre size > 0
     * @pre ram > 0
     * @pre bw > 0
     * @pre cpus > 0
     * @pre priority >= 0
     * @pre cloudletScheduler != null
     * @post $none
     */
    public MicroserviceInstance(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler
                                ,double requestTime,
                                int configurationType,
                                int purchaseType,
                                double delayInStartUp,
                                int serviceId) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
        this.requestTime = requestTime;
        this.configurationType = configurationType;
        this.purchaseType = purchaseType;
        this.delayInStartUp = delayInStartUp;
        this.serviceId = serviceId;
        this.startTime = -1;
        this.destroyTime = -1;
        this.lifeTime = 0;
        this.price = PolicyConstants.VM_PRICE[purchaseType][configurationType];
        this.bill = 0;

        setStatus(-1);
        this.cloudletList = new ArrayList<>();
        lastUpateTime = 0;

    }


    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(double requestTime) {
        this.requestTime = requestTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getDestroyTime() {
        return destroyTime;
    }

    public void setDestroyTime(double destroyTime) {
        this.destroyTime = destroyTime;
    }

    public double getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(double lifeTime) {
        this.lifeTime = lifeTime;
    }

    public double getDelayInStartUp() {
        return delayInStartUp;
    }

    public void setDelayInStartUp(double delayInStartUp) {
        this.delayInStartUp = delayInStartUp;
    }

    public int getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(int configurationType) {
        this.configurationType = configurationType;
    }

    public int getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(int purchaseType) {
        this.purchaseType = purchaseType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getBill() {
        return bill;
    }

    public void setBill(double bill) {
        this.bill = bill;
    }

    public ArrayList<NetworkCloudlet> getCloudletList() {
        return cloudletList;
    }

    public void setCloudletList(ArrayList<NetworkCloudlet> cloudletList) {
        this.cloudletList = cloudletList;
    }

    public double getLastUpateTime() {
        return lastUpateTime;
    }

    public void setLastUpateTime(double lastUpateTime) {
        this.lastUpateTime = lastUpateTime;
    }
}
