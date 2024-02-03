package org.utils;

import org.omg.CORBA.PUBLIC_MEMBER;

public class ServiceSimEvents {
    private static final int BASE = 50;
    public static final int Packet_ARRIVAL = BASE + 1;
    public static final int Packet_SEND = BASE + 2;
    public static final int Cloudlet_UPDATE_FOR_RESPONSE = BASE + 3;
    public static final int Packet_SEND_CHECK = BASE + 4;
    public static final int Cloudlet_SUBMIT = BASE + 5;
    public static final int Cloudlet_PROCESS_UPDATE = BASE + 6;

    public static final int LoadAdmission_ALTER = BASE + 7;
    public static final int LoadBalance_ALTER = BASE + 8;
    public static final int RequestDispatching_ALTER = BASE + 9;
    public static final int Instance_GREATE = BASE + 10;
    public static final int Instance_DESTROY = BASE + 11;

    public static final int Service_DISCOVERY_ADD = BASE + 12;
    public static final int Service_DISCOVERY_DEL = BASE + 13;

    public static final int EndUserRequest_ARRIVAL = BASE + 14;
    public static final int EndUserRequest_SEND = BASE + 15;
    public static final int Workload_GENERATE = BASE + 16;
}
