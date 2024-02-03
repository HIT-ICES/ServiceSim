/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.enduser.networkPacket;

import org.cloudbus.cloudsim.UtilizationModel;
import org.utils.PolicyConstants;

import java.util.ArrayList;
import java.util.Map;

/**
 * NetworkCloudlet class extends Cloudlet to support simulation of complex applications. Each such
 * a network Cloudlet represents a task of the application. Each task consists of several stages.
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
 * @todo Attributes should be private
 * @todo The different cloudlet classes should have a class hierarchy, by means
 * of a super class and/or interface.
 */
public class NetworkCloudlet extends Cloudlet implements Comparable<Object> {

	// appId is not enough. "serviceChainId, endUserId, endUserLevel" for routing.
	public int appId;

	public double firstSubmissionTime;

	//public double execStartTime; Already in Cloudlet class.

	public double failTime;

	/** The time where this Cloudlet completes. */
	//public double finishTime; Already in Cloudlet class.

	/** Current stage of cloudlet execution. */
	public int currStagenum; 

	/** Star time of the current stage. */
	public double timetostartStage;

	/** Time spent in the current stage. */
	public double timespentInStage; 

	/** All stages which cloudlet execution. */
	public ArrayList<TaskStage> stages; 

	public int  memory; // for allocated

	public NetworkCloudlet(
            int cloudletId,
            int appId,
            long cloudletLength,
            int pesNumber,
            long cloudletFileSize,
            long cloudletOutputSize,
            int memory,
            UtilizationModel utilizationModelCpu,
            UtilizationModel utilizationModelRam,
            UtilizationModel utilizationModelBw) {
		super(
				cloudletId,
				cloudletLength,
				pesNumber,
				cloudletFileSize,
				cloudletOutputSize,
				utilizationModelCpu,
				utilizationModelRam,
				utilizationModelBw);

		currStagenum = -1;
		//mai: this.appId = appId
		this.appId = appId;
		this.memory = memory;
		stages = new ArrayList<TaskStage>();
	}

	@Override
	public int compareTo(Object arg0) {
		return 0;
	}



	/**
	 * delay
	 * @return
	 */
	public double getTotalWaitingTime(int cloudletSchedulerName, double vmMips, int vmPEs) {

		if(cloudletSchedulerName == PolicyConstants.TimeShared ||  cloudletSchedulerName == PolicyConstants.TimeSharedWithLimit){
			// get the total execution time.
			double totalExecutonTime = 0;
			double totalCloudletLength = 0;
			for (TaskStage taskStage : stages){
				if (taskStage.type == NetworkConstants.EXECUTION){
					totalExecutonTime += taskStage.time;
					totalCloudletLength += taskStage.stageCloudletLength;
				}
			}
			double processingTime = (totalCloudletLength * getNumberOfPes()) / (vmMips * vmPEs);
			double waitingtime = getExecStartTime() - getFirstSubmissionTime() + totalExecutonTime - processingTime;
//                double waitingtime = finishTime - (cloudletLength / vmMips) - firstSubmissionTime;
			if (waitingtime < 0) // Sth. finish cloudlet is sooner than actual finishing(about 0.001 ms)
				waitingtime = Math.ceil(waitingtime);

			return waitingtime;
		}else if (cloudletSchedulerName == PolicyConstants.SpaceShared){
			return getExecStartTime() - getFirstSubmissionTime();
		}else{
			return 0;
		}
	}

	public double getFirstSubmissionTime(){
		return firstSubmissionTime;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}
}
