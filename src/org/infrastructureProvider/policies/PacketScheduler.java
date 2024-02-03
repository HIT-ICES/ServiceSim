package org.infrastructureProvider.policies;

import org.enduser.networkPacket.NetworkPacket;

import java.util.ArrayList;
import java.util.List;


public abstract class PacketScheduler {

    private double previousTime;

    private double allocatedBandwidth;

    private List<NetworkPacket> packetFinishedList;

    public PacketScheduler() {
        previousTime = 0.0;
        packetFinishedList = new ArrayList<>();
    }

    public abstract double packetSubmit(double currentTime,double bandwidth, List<NetworkPacket> networkPacketList);

    public abstract double updatePacketSending(double currentTime, double bandwidth);


    public double getPreviousTime() {
        return previousTime;
    }

    public void setPreviousTime(double previousTime) {
        this.previousTime = previousTime;
    }

    public double getAllocatedBandwidth() {
        return allocatedBandwidth;
    }

    public void setAllocatedBandwidth(double allocatedBandwidth) {
        this.allocatedBandwidth = allocatedBandwidth;
    }

    public List<NetworkPacket> getPacketFinishedList() {
        return packetFinishedList;
    }

    public void setPacketFinishedList(List<NetworkPacket> packetFinishedList) {
        this.packetFinishedList = packetFinishedList;
    }
}
