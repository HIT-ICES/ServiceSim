package org.serviceProvider.capacities;

import org.enduser.networkPacket.NetworkPacket;

public class NonLoadAdmission implements LoadAdmission {

    @Override
    public boolean isAdmission(NetworkPacket networkPacket) {
        return true;
    }
}
