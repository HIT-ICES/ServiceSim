package org.infrastructureProvider.policies;

import org.enduser.networkPacket.NetworkPacket;

import java.util.ArrayList;
import java.util.List;

public class PacketSchedulerTimeShared extends PacketScheduler {

    private List<NetworkPacket> packetSendingList;

    public PacketSchedulerTimeShared() {
        super();
        packetSendingList = new ArrayList<>();
    }


    @Override
    public double packetSubmit(double currentTime, double bandwidth, List<NetworkPacket> networkPacketList) {
        updatePacketSending(currentTime, bandwidth);
        packetSendingList.addAll(networkPacketList);
        if (packetSendingList.size() == 0){
            setPreviousTime(currentTime);
            return -1;
        }
        double bandwidthPerPacket1 = getAllocatedBandwidth() / packetSendingList.size();
        double min = packetSendingList.get(0).getRemainSize();
        for (NetworkPacket networkPacket : packetSendingList){
            if (networkPacket.getRemainSize() < min){
                min = networkPacket.getRemainSize();
            }
        }
        setPreviousTime(currentTime);
        return min / bandwidthPerPacket1; // next send interval;

    }

    @Override
    public double updatePacketSending(double currentTime, double bandwidth) {
        setAllocatedBandwidth(bandwidth);

        double timeSpan = currentTime - getPreviousTime();

        if (packetSendingList.size() == 0){
            setPreviousTime(currentTime);
            return -1;
        }
        double bandwidthPerPacket = getAllocatedBandwidth() / packetSendingList.size();

        List<NetworkPacket> toMove = new ArrayList<>();
        for (NetworkPacket networkPacket : packetSendingList){
            double sendLength = bandwidthPerPacket * timeSpan;
            networkPacket.addCompletedLength(sendLength);
            if (networkPacket.getRemainSize() == 0){
                getPacketFinishedList().add(networkPacket);
                toMove.add(networkPacket);
            }
        }

        packetSendingList.removeAll(toMove);
        if (packetSendingList.size() == 0){
            setPreviousTime(currentTime);
            return -1;
        }
        double bandwidthPerPacket1 = getAllocatedBandwidth() / packetSendingList.size();
        double min = packetSendingList.get(0).getRemainSize();
        for (NetworkPacket networkPacket : packetSendingList){
            if (networkPacket.getRemainSize() < min){
                min = networkPacket.getRemainSize();
            }
        }
        setPreviousTime(currentTime);
        return min / bandwidthPerPacket1; // next send interval;
    }
}
