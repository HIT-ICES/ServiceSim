package org.infrastructureProvider.policies;

import org.cloudbus.cloudsim.ResCloudlet;
import org.enduser.networkPacket.Cloudlet;
import org.enduser.networkPacket.NetworkCloudlet;
import org.enduser.networkPacket.NetworkPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class NetworkCloudletScheduler extends CloudletScheduler  {

    public Map<Integer, List<NetworkPacket>> pkttosend; // cloudletId to networkPackets

    public Map<Integer, List<NetworkPacket>> pktrecv; // cloudletId to networkPackets

    protected List<? extends ResCloudlet> cloudletFinishedList;

    /** The list of cloudlets being executed on the VM. */
    protected List<? extends ResCloudlet> cloudletExecList;

    public NetworkCloudletScheduler(){
        super();
        pkttosend = new HashMap<>();
        pktrecv = new HashMap<>();
        cloudletFinishedList = new ArrayList<>();
        cloudletExecList = new ArrayList<>();

    }

    public abstract double cloudletSubmit(List<NetworkCloudlet> gls);

    /**
     * Gets the cloudlet finished list.
     *
     * @param <T> the generic type
     * @return the cloudlet finished list
     */
    @SuppressWarnings("unchecked")
    public <T extends ResCloudlet> List<T> getCloudletFinishedList() {
        return (List<T>) cloudletFinishedList;
    }

    /**
     * Sets the cloudlet finished list.
     *
     * @param <T> the generic type
     * @param cloudletFinishedList the new cloudlet finished list
     */
    protected <T extends ResCloudlet> void setCloudletFinishedList(List<T> cloudletFinishedList) {
        this.cloudletFinishedList = cloudletFinishedList;
    }

    public Map<Integer, List<NetworkPacket>> getPkttosend() {
        return pkttosend;
    }

    public void setPkttosend(Map<Integer, List<NetworkPacket>> pkttosend) {
        this.pkttosend = pkttosend;
    }

    public Map<Integer, List<NetworkPacket>> getPktrecv() {
        return pktrecv;
    }

    public void setPktrecv(Map<Integer, List<NetworkPacket>> pktrecv) {
        this.pktrecv = pktrecv;
    }

    /**
     * Gets the cloudlet exec list.
     *
     * @param <T> the generic type
     * @return the cloudlet exec list
     */
    @SuppressWarnings("unchecked")
    public <T extends ResCloudlet> List<T> getCloudletExecList() {
        return (List<T>) cloudletExecList;
    }

    /**
     * Sets the cloudlet exec list.
     *
     * @param <T> the generic type
     * @param cloudletExecList the new cloudlet exec list
     */
    protected <T extends ResCloudlet> void setCloudletExecList(List<T> cloudletExecList) {
        this.cloudletExecList = cloudletExecList;
    }
}
