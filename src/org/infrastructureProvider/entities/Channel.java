package org.infrastructureProvider.entities;

import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.networkPacket.NetworkPacket;
import org.infrastructureProvider.policies.PacketScheduler;

import java.util.List;

public class Channel {

    private int from;

    private int to;

    private double bandwidth;

    private double latency;

    private PacketScheduler packetScheduler;

    public Channel(int from, int to, double bandwidth, double latency, PacketScheduler packetScheduler) {
        this.from = from;
        this.to = to;
        this.bandwidth = bandwidth;
        this.latency = latency;
        this.packetScheduler = packetScheduler;
    }

    public double packetSubmit(List<NetworkPacket> packets){
        double currentTime = CloudSim.clock();
        double nextCheckTime = getPacketScheduler().packetSubmit(currentTime, bandwidth, packets);
        return nextCheckTime;
    }

    public double updatePacketSending(){
        double currentTime = CloudSim.clock();
        double nextCheckTime = getPacketScheduler().updatePacketSending(currentTime, bandwidth);
        return nextCheckTime;
    }


    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    public double getLatency() {
        return latency;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public PacketScheduler getPacketScheduler() {
        return packetScheduler;
    }

    public void setPacketScheduler(PacketScheduler packetScheduler) {
        this.packetScheduler = packetScheduler;
    }
}
