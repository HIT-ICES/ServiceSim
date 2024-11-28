package org.infrastructureProvider.policies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.networkPacket.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/*@Todo
 * */

@SuppressWarnings({"unused", "unchecked", "DuplicatedCode"})
public class NetworkCloudletTimeSharedSchedulerAsync extends NetworkCloudletScheduler {

    protected List<? extends ResCloudlet> cloudletWaitingList;

    public NetworkCloudletTimeSharedSchedulerAsync() {
        super();
        cloudletWaitingList = new ArrayList<>();
    }

    @Override
    public double cloudletSubmit(List<NetworkCloudlet> gls) {
        updateVmProcessing(CloudSim.clock(), getCurrentMipsShare());
        for (NetworkCloudlet networkCloudlet : gls) {
            ResCloudlet rcl = new ResCloudlet(networkCloudlet);
            rcl.setCloudletStatus(Cloudlet.INEXEC);
            for (int i = 0; i < networkCloudlet.getNumberOfPes(); i++) {
                rcl.setMachineAndPeId(0, i);
            }
            getCloudletExecList().add(rcl);
        }

        return updateVmProcessing(CloudSim.clock(), getCurrentMipsShare());
    }

    @Override
    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
        if (getCloudletExecList().isEmpty()) {
            setPreviousTime(currentTime);
            setCurrentMipsShare(mipsShare);
            return 0.0;
        }

        double nextCheckInterval = Double.MAX_VALUE;
        double timeSpan = currentTime - getPreviousTime();

        for (ResCloudlet rcl : getCloudletExecList()) {
            NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();

            if ((cl.currStageNum != -1)) {

                if (cl.currStageNum == NetworkConstants.FINISH) {
                    break;
                }
                TaskStage st = cl.stages.get(cl.currStageNum);
                if (st.type == NetworkConstants.EXECUTION) {

                    // mai: process start time
                    st.processStartTime = cl.timeToStartStage;

                    cl.timeSpentInStage = currentTime - cl.timeToStartStage;
                    Log.printLine("------------------" + "  " + cl.getCloudletId() + " " + CloudSim.clock() + "  " + cl.timeToStartStage + " " + cl.timeSpentInStage);

                    // Judge whether the specified execution length is reached.
                    st.hasExeCloudletLength += getCapacity(getCurrentMipsShare()) * timeSpan * rcl.getNumberOfPes();

                    Log.printLine("------------------" + " " + cl.getCloudletId() + " " + st.hasExeCloudletLength);
                    if ((st.hasExeCloudletLength + 0.0001) >= st.stageCloudletLength) { // now,
                        // st.time represents the cloudletLength in this stage.
                        st.time = cl.timeSpentInStage;
                        double time = changeToNextStage(rcl, mipsShare);
                        if (time > 0 && time < nextCheckInterval) {
                            nextCheckInterval = time;
                        }
                    } else {
                        double time = (st.stageCloudletLength - st.hasExeCloudletLength) / (getCapacity(mipsShare) * rcl.getNumberOfPes());
                        if (time < nextCheckInterval) {
                            nextCheckInterval = time;
                        }
                    }

                }
                // mai: Now the acceptance stage cannot be restricted by the sequence defined at the beginning.
                if (st.type == NetworkConstants.WAIT_RECV) {
                    Log.printLine("------------------" + "  " + cl.getCloudletId() + "  " + CloudSim.clock());
                    double time = processPktReceive(rcl, mipsShare);
                    if (time > 0 && time < nextCheckInterval) {
                        nextCheckInterval = time;
                    }
                }

            } else {
                Log.printLine("------------------" + "  " + cl.getCloudletId() + "  " + CloudSim.clock());
                double time = changeToNextStage(rcl, mipsShare);
                if (time > 0 && time < nextCheckInterval) {
                    nextCheckInterval = time;
                }
            }

        }

        List<ResCloudlet> toRemove = new ArrayList<>();
        for (ResCloudlet rcl : getCloudletExecList()) {
            // rounding issue...
            if (((NetworkCloudlet) (rcl.getCloudlet())).currStageNum == NetworkConstants.FINISH) {
                // stage is changed and packet to send
                rcl.getCloudlet().setFinishTime(CloudSim.clock());
                toRemove.add(rcl);
                cloudletFinish(rcl);
            }
        }
        getCloudletExecList().removeAll(toRemove);

        setPreviousTime(currentTime);
        setCurrentMipsShare(mipsShare);

        return nextCheckInterval;
    }

    /**
     * Changes a cloudlet to the next stage.
     *
     * @todo It has to be corrected the method name case. Method too long
     * to understand what is its responsibility.
     */
    private double changeToNextStage(ResCloudlet rcl, List<Double> mipsShare) {
        NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();
        cl.timeSpentInStage = 0;
        cl.timeToStartStage = CloudSim.clock();
        int currStage = cl.currStageNum;
        if (currStage >= (cl.stages.size() - 1)) {
            Log.printLine("------------------ " + cl.getCloudletId() + " " + CloudSim.clock());
            cl.currStageNum = NetworkConstants.FINISH;

        } else {
            cl.currStageNum = currStage + 1;
            int i;
            for (i = cl.currStageNum; i < cl.stages.size(); i++) {
                if (cl.stages.get(i).type == NetworkConstants.WAIT_SEND) {

                    // mai: process start time
                    cl.stages.get(i).processStartTime = CloudSim.clock();

                    NetworkPacket pkt = new NetworkPacket(cl.getUserId(), cl.stages.get(i).data, cl.getVmId(), cl.stages.get(i).speer, cl.getCloudletId(), cl.getAppId());

                    List<NetworkPacket> pktlist = pktToSend.get(cl.getCloudletId());
                    if (pktlist == null) {
                        pktlist = new ArrayList<>();
                    }
                    pktlist.add(pkt);
                    pktToSend.put(cl.getCloudletId(), pktlist);

                } else {
                    break;
                }

            }
            if (i == cl.stages.size()) {
                cl.currStageNum = NetworkConstants.FINISH;
            } else {
                cl.currStageNum = i;

                if (cl.stages.get(i).type == NetworkConstants.WAIT_RECV) {
                    Log.printLine("------------------" + "  " + cl.getCloudletId() + "  " + CloudSim.clock());
                    return processPktReceive(rcl, mipsShare);
                }

                if (cl.stages.get(i).type == NetworkConstants.EXECUTION) {

                    // mai: process start time
                    cl.stages.get(i).processStartTime = CloudSim.clock();

                    // Calculate the estimated execution completion time of this stage.
                    double time = cl.stages.get(i).stageCloudletLength / (getCapacity(mipsShare) * rcl.getNumberOfPes());
                    Log.printLine("------------------" + CloudSim.clock() + " " + getCapacity(mipsShare) + "  " + time);
                    return time;
                }

            }
        }
        return -1;

    }

    protected double processPktReceive(ResCloudlet rcl, List<Double> mipsShare) {
        NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();
        int completeReceives = 0;
        // can write a loop
        int j;
        for (j = cl.currStageNum; j < cl.stages.size(); j++) {
            if (cl.stages.get(j).type == NetworkConstants.WAIT_RECV) {
                if (cl.stages.get(j).processStartTime == -1) {
                    Log.printLine("------------------" + "  " + cl.getCloudletId() + "  " + CloudSim.clock());
                    List<NetworkPacket> pktlist = pktRecv.get(cl.getCloudletId());
                    List<NetworkPacket> pktToRemove = new ArrayList<>();
                    if (pktlist != null) {
                        Iterator<NetworkPacket> it = pktlist.iterator();
                        NetworkPacket pkt;
                        while (it.hasNext()) {
                            pkt = it.next();
                            // Assumption packet will not arrive in the same cycle
                            if (pkt.getDestinationCloudlet() == cl.getCloudletId()) {
                                // mai: process start time
                                cl.stages.get(j).processStartTime = CloudSim.clock();
                                Log.printLine("------------------" + "  " + cl.getCloudletId() + "  " + j + " " + cl.stages.get(j).processStartTime);
                                cl.stages.get(j).time = CloudSim.clock() - pkt.getSendTime();
                                pktToRemove.add(pkt);
                                completeReceives++;
                                break;
                            }
                        }
                        pktlist.removeAll(pktToRemove);
                        // if(pkt!=null)
                        // else wait for receiving the packet
                    }
                } else {
                    completeReceives++;
                }
            } else {
                if (completeReceives == (j - cl.currStageNum)) {
                    cl.currStageNum = j - 1;
                    return changeToNextStage(rcl, mipsShare);
                }
                break;
            }
        }
        if (completeReceives == (j - cl.currStageNum)) {
            cl.currStageNum = j - 1;
            return changeToNextStage(rcl, mipsShare);
        }
        return -1;
    }


    protected double getCapacity(List<Double> mipsShare) {
        double capacity = 0.0;
        int cpus = 0;
        for (Double mips : mipsShare) {
            capacity += mips;
            if (mips > 0.0) {
                cpus++;
            }
        }

        int pesInUse = 0;
        for (ResCloudlet rcl : getCloudletExecList()) {
            pesInUse += rcl.getNumberOfPes();
        }

        capacity /= Math.max(pesInUse, cpus);
        return capacity;
    }

    @Override
    public double cloudletSubmit(Cloudlet gl, double fileTransferTime) {
        return 0;
    }

    @Override
    public double cloudletSubmit(Cloudlet gl) {
        return 0;
    }

    @Override
    public Cloudlet cloudletCancel(int clId) {
        return null;
    }

    @Override
    public boolean cloudletPause(int clId) {
        return false;
    }

    @Override
    public double cloudletResume(int clId) {
        return 0;
    }

    @Override
    public void cloudletFinish(ResCloudlet rcl) {

    }

    @Override
    public int getCloudletStatus(int clId) {
        return 0;
    }

    @Override
    public boolean isFinishedCloudlets() {
        return false;
    }

    @Override
    public Cloudlet getNextFinishedCloudlet() {
        return null;
    }

    @Override
    public int runningCloudlets() {
        return 0;
    }

    @Override
    public Cloudlet migrateCloudlet() {
        return null;
    }

    @Override
    public double getTotalUtilizationOfCpu(double time) {
        return 0;
    }

    @Override
    public List<Double> getCurrentRequestedMips() {
        return null;
    }

    @Override
    public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
        return 0;
    }

    @Override
    public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
        return 0;
    }

    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfRam() {
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfBw() {
        return 0;
    }

    public <T extends ResCloudlet> List<T> getCloudletWaitingList() {
        return (List<T>) cloudletWaitingList;
    }

    public <T extends ResCloudlet> void setCloudletWaitingList(List<T> cloudletWaitingList) {
        this.cloudletWaitingList = cloudletWaitingList;
    }
}
