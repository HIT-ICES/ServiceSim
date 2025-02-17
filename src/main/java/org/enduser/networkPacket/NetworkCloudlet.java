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

/**
 * NetworkCloudlet class extends Cloudlet to support simulation of complex applications. Each such
 * a network Cloudlet represents a task of the application. Each task consists of several stages.
 * <p>
 * <br/>Please refer to the following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modeling Parallel Applications in Cloud
 * Simulations, Proceedings of the fourth IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 *
 * @author Saurabh Kumar Garg
 * @todo Attributes should be private
 * @todo The different cloudlet classes should have a class hierarchy, by means
 * of a super class and/or interface.
 * @since CloudSim Toolkit 1.0
 */
@SuppressWarnings({"NullableProblems"})
public class NetworkCloudlet extends Cloudlet implements Comparable<Object> {

    // appId is not enough. "serviceChainId, endUserId, endUserLevel" for routing.
    public int appId;

    public double firstSubmissionTime;

    //public double execStartTime; Already in Cloudlet class.

    public double failTime;

    /* The time when this Cloudlet completes. */
    // Public double finishTime; Already in Cloudlet class.

    /**
     * Current stage of cloudlet execution.
     */
    public int currStageNum;

    /**
     * Star time of the current stage.
     */
    public double timeToStartStage;

    /**
     * Time spent in the current stage.
     */
    public double timeSpentInStage;

    /**
     * All stages which cloudlet execution.
     */
    public ArrayList<TaskStage> stages;

    public int memory; // for allocated

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

        currStageNum = -1;
        //mai: this.appId = appId
        this.appId = appId;
        this.memory = memory;
        stages = new ArrayList<>();
    }

    @Override
    public int compareTo(Object arg0) {
        return 0;
    }


    /**
     * delay
     *
     * @return delay
     */
    public double getTotalWaitingTime(int cloudletSchedulerName, double vmMips, int vmPEs) {

        if (cloudletSchedulerName == PolicyConstants.TimeShared || cloudletSchedulerName == PolicyConstants.TimeSharedWithLimit) {
            // get the total execution time.
            double waitingTime = getWaitingTime(vmMips, vmPEs);
//                double waitingTime = finishTime - (cloudletLength / vmMips) - firstSubmissionTime;
            if (waitingTime < 0) // sth. finish cloudlet is sooner than actual finishing(about 0.001 ms)
                waitingTime = Math.ceil(waitingTime);

            return waitingTime;
        } else if (cloudletSchedulerName == PolicyConstants.SpaceShared) {
            return getExecStartTime() - getFirstSubmissionTime();
        } else {
            return 0;
        }
    }

    private double getWaitingTime(double vmMips, int vmPEs) {
        double totalExecutionTime = 0;
        double totalCloudletLength = 0;
        for (TaskStage taskStage : stages) {
            if (taskStage.type == NetworkConstants.EXECUTION) {
                totalExecutionTime += taskStage.time;
                totalCloudletLength += taskStage.stageCloudletLength;
            }
        }
        double processingTime = (totalCloudletLength * getNumberOfPes()) / (vmMips * vmPEs);
        return getExecStartTime() - getFirstSubmissionTime() + totalExecutionTime - processingTime;
    }

    public double getFirstSubmissionTime() {
        return firstSubmissionTime;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }
}
