/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.enduser.networkPacket;

import java.util.HashMap;
import java.util.Map;

public class NetworkConstants {

    // stage type
    public static final int EXECUTION = 0;
    public static final int WAIT_SEND = 1;
    public static final int WAIT_RECV = 2;
    public static final int FINISH = -2;
    public static final int FILE_SIZE = 300;
    public static final int OUTPUT_SIZE = 300;
    /* Request or Response */
    public static int REQUEST = 0;
    public static int RESPONSE = 1;
    public static int currentCloudletId = 0;
    public static int currentAppId = 0;

    private static final Map<String, Integer> CONSTANTS_MAP = new HashMap<>();

    static {
        CONSTANTS_MAP.put("EXECUTION", 0);
        CONSTANTS_MAP.put("WAIT_SEND", 1);
        CONSTANTS_MAP.put("WAIT_RECV", 2);
        CONSTANTS_MAP.put("FINISH", -2);
        CONSTANTS_MAP.put("FILE_SIZE", 300);
        CONSTANTS_MAP.put("OUTPUT_SIZE", 300);
        CONSTANTS_MAP.put("REQUEST", 0);
        CONSTANTS_MAP.put("RESPONSE", 1);
    }

    public static Integer getValueByType(String type) {
        return CONSTANTS_MAP.get(type);
    }

}
