/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.enduser.networkPacket;

/**
 * TaskStage represents various stages a {@link NetworkCloudlet} can have during execution. 
 * Four stage types which are possible: {@link NetworkConstants#EXECUTION},
 * {@link NetworkConstants#WAIT_SEND}, {@link NetworkConstants#WAIT_RECV}, 
 * {@link NetworkConstants#FINISH}.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 * @todo Attributes should be defined as private.
 */
public class TaskStage {

        /**
         * The task type, either {@link NetworkConstants#EXECUTION}, 
         * {@link NetworkConstants#WAIT_SEND} or {@link NetworkConstants#WAIT_RECV}.
         * @todo It would be used enum instead of int constants.
         */
	public int type;

	// mai: for execution
	public double stageCloudletLength;
	// mai: for execution and for timesharedCloudletScheduler.
	public double hasExeCloudletLength;
        /**
         * The data length generated for the task (in bytes).
        */
	public double data;

		/** mai: send/receive/execution start time. */

	public double processStartTime;

        /** Execution time for this stage. */
	public double time;

        /** Stage (task) id. */
	public double stageid;



		/** From whom data needed to be sent. */
	public int speer;

	public TaskStage(int type, double stageCloudletLength, double data, double stageid, int speer) {
		this.type = type;
		this.stageCloudletLength = stageCloudletLength;
		this.hasExeCloudletLength = 0;
		this.data = data;
		this.processStartTime = -1; // - 1 when the phase has not yet started
		this.time = 0; //  - 1 when the phase has not yet started
		this.stageid = stageid;
		this.speer = speer;
	}

	public int getType() {
		return type;
	}

	public double getTime() {
		return time;
	}

	public void setProcessStartTime(double processStartTime) {
		this.processStartTime = processStartTime;
	}

	public double getProcessStartTime() {
		return processStartTime;
	}


}
