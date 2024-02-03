/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.enduser.networkPacket;

public class NetworkConstants {

	/* Request or Response */
	public static int REQUEST = 0;
	public static int RESPONSE = 1;

	public static int currentCloudletId = 0;
	public static int currentAppId = 0;

	// stage type
	public static final int EXECUTION = 0; 
	public static final int WAIT_SEND = 1;
	public static final int WAIT_RECV = 2;
	public static final int FINISH = -2;


	public static final int FILE_SIZE = 300;
	public static final int OUTPUT_SIZE = 300;


}
