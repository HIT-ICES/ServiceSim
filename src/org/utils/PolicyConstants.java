package org.utils;

public class PolicyConstants {

    /* cloudlet scheduler policies */
    public final static int SpaceShared = 0;
    public final static int TimeShared = 1;
    public final static int TimeSharedWithLimit = 2;
    public final static int TimeSharedWithFixShare = 3;

    /* vm configuration and purchase policies */
    // Vm details
    public final static double [] VM_MIPS	= { 20, 40, 20, 20, 100 };
    public final static int[] VM_PES	= { 1, 1, 2, 4, 1 }; // Number of CPUs
    public final static int[] VM_RAM	= { 128, 512, 1024, 2048, 100 }; //Vm memory (MB)
    public final static long VM_BW		= 1000; // 1 Mbit/s
    public final static long VM_SIZE	= 250; // 0.25 GB, image size (MB)
    public final static int VM_CldScheduler = TimeShared; // 之前是timeshared
    public final static double[] VM_DELAY	= { 0, 0, 0, 0, 0 }; //Vm memory (MB)

    // price: x - purchase type; y - configuration type
    public final static double[][] VM_PRICE = {
            {1, 1, 1, 1, 1},
            {2, 2, 2, 2, 1}
    };

    // vm status
    /* Reference AutoscaleSim */
    public static final int Requested = 0;
    public static final int Started = 1;
    public static final int Quarantined = 2; // for Instance migration or service replacement
    public static final int Destroyed = 3;

    /* relationship of clock() and minute */
    public static final int aMinute = 60;


    /* networkPacket arrival process */
//    public static final double PacketArrivalProcess = 0.0001; // process time 0.001s
//    public static final double RequestArrivalProcess = 0.0001; // process time 0.001s
//    public static final double ResponseDataArrivalProcess = 0.0001; // process time 0.001s

    // compK8S
    public static final double PacketArrivalProcess = 0.0001; // process time 0.001s
    public static final double RequestArrivalProcess = 0.0001; // process time 0.001s
    public static final double ResponseDataArrivalProcess = 0.0001; // process time 0.001s


    public static int vmIdNum = 0;

}
