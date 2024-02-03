package org.serviceProvider.capacities;

import org.enduser.networkPacket.NetworkPacket;

public interface LoadAdmission {

    boolean isAdmission(NetworkPacket networkPacket);
}
