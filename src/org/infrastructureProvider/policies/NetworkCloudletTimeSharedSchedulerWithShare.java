package org.infrastructureProvider.policies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.networkPacket.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetworkCloudletTimeSharedSchedulerWithShare extends NetworkCloudletScheduler{

    int limitNum;
    int currentNum;
    ArrayList waitingList;

    ArrayList holdingList;
    public NetworkCloudletTimeSharedSchedulerWithShare(int limitNum){

        super();
        this.limitNum = limitNum;
        currentNum = 0;
        waitingList = new ArrayList();
        holdingList = new ArrayList();
    }

    @Override
    public double cloudletSubmit(List<NetworkCloudlet> gls) {

        updateVmProcessing(CloudSim.clock(), getCurrentMipsShare());
        for (NetworkCloudlet networkCloudlet : gls){
            Cloudlet cloudlet = (Cloudlet) networkCloudlet;
            if (currentNum < limitNum){
                ResCloudlet rcl = new ResCloudlet(cloudlet);
                rcl.setCloudletStatus(Cloudlet.INEXEC);
                for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
                    rcl.setMachineAndPeId(0, i);
                }
                getCloudletExecList().add(rcl);
                currentNum ++;
            }else{
                waitingList.add(cloudlet);
            }
        }

        double nextCheckTime = updateVmProcessing(CloudSim.clock(), getCurrentMipsShare());

        return nextCheckTime;
    }

    @Override
    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {

        if (getCloudletExecList().size() == 0) {
            setPreviousTime(currentTime);
            setCurrentMipsShare(mipsShare);
            return 0.0;
        }

        double nextCheckInterval = Double.MAX_VALUE;
        double timeSpan = currentTime - getPreviousTime();

        for (ResCloudlet rcl : getCloudletExecList()){
            NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();

            if ((cl.currStagenum != -1)) {

                if (cl.currStagenum == NetworkConstants.FINISH) {
                    break;
                }
                TaskStage st = cl.stages.get(cl.currStagenum);
                if (st.type == NetworkConstants.EXECUTION) {

                    // mai: process start time
                    st.processStartTime = cl.timetostartStage;
                    //Log.printLine("ssssssssss"+cl.getCloudletId()+ "   " + st.processStartTime);

                    cl.timespentInStage = currentTime - cl.timetostartStage;
                    Log.printLine("zzzzzzzzzzzzz"+ "  "+ cl.getCloudletId() + " " + CloudSim.clock()+ "  " + cl.timetostartStage+ " "  + cl.timespentInStage);

                    // Judge whether the specified execution length is reached.
                    //long executedLength = (long) (getCapacity(mipsShare) * cl.timespentInStage * rcl.getNumberOfPes());
                    st.hasExeCloudletLength += getCapacity(getCurrentMipsShare()) * timeSpan * rcl.getNumberOfPes();

                    Log.printLine("rrrrr"+ " " + cl.getCloudletId() + " " + st.hasExeCloudletLength);
                    if ((st.hasExeCloudletLength + 0.0001) >= st.stageCloudletLength){ // now , st.time represents the cloudletLength in this stage.
                        st.time = cl.timespentInStage;
                        double time = changetonextstage(rcl,mipsShare);
                        if (time > 0 && time < nextCheckInterval) {
                            nextCheckInterval = time;
                        }
                    }else{
                        double time = (st.stageCloudletLength - st.hasExeCloudletLength) / (getCapacity(mipsShare) * rcl.getNumberOfPes());
                        if (time < nextCheckInterval) {
                            nextCheckInterval = time;
                        }
                    }

                }
                // mai: Now the acceptance stage can not be restricted by the sequence defined at the beginning.
                if (st.type == NetworkConstants.WAIT_RECV){
                    Log.printLine("cccccccccc"+ "  "+ cl.getCloudletId() + "  "+ CloudSim.clock());
                    double time = processPktReceive(rcl,mipsShare);
                    if (time > 0 && time < nextCheckInterval) {
                        nextCheckInterval = time;
                    }
                }

            } else {
                Log.printLine("aaaaaaaa"+ "  "+ cl.getCloudletId() + "  "+ CloudSim.clock());
                double time = changetonextstage(rcl,mipsShare);
                if (time > 0 && time < nextCheckInterval) {
                    nextCheckInterval = time;
                }
            }

        }

        List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
        for (ResCloudlet rcl : getCloudletExecList()) {
            // rounding issue...
            if (((NetworkCloudlet) (rcl.getCloudlet())).currStagenum == NetworkConstants.FINISH) {
                // stage is changed and packet to send
                rcl.getCloudlet().setFinishTime(CloudSim.clock());
                toRemove.add(rcl);
                cloudletFinish(rcl);
                currentNum --;
            }
        }
        getCloudletExecList().removeAll(toRemove);

        if (toRemove.size()>0){
            List<Object> toRemove1 = new ArrayList<Object>();
            int remainNum = limitNum-currentNum;
            if (remainNum > waitingList.size()){
                remainNum = waitingList.size();
            }
            for (int i = 0; i < remainNum;i++){
                Cloudlet cloudlet = (Cloudlet)waitingList.get(i);
                ResCloudlet rcl = new ResCloudlet(cloudlet);
                rcl.setCloudletStatus(Cloudlet.INEXEC);
                for (int j = 0; j < cloudlet.getNumberOfPes(); j++) {
                    rcl.setMachineAndPeId(0, j);
                }
                getCloudletExecList().add(rcl);
                currentNum ++;
                toRemove1.add(cloudlet);
            }
            waitingList.removeAll(toRemove1);
            if (toRemove1.size()>0){
                nextCheckInterval = updateVmProcessing(CloudSim.clock(), getCurrentMipsShare());
            }

        }

        setPreviousTime(currentTime);
        setCurrentMipsShare(mipsShare);

        return nextCheckInterval;
    }

    /**
     * Changes a cloudlet to the next stage.
     *
     * @todo It has to be corrected the method name case. Method too long
     * to understand what is its responsibility.*/
    private double changetonextstage(ResCloudlet rcl, List<Double> mipsShare) {
        NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();
        cl.timespentInStage = 0;
        cl.timetostartStage = CloudSim.clock();
        int currstage = cl.currStagenum;
        if (currstage >= (cl.stages.size() - 1)) {
            Log.printLine("eeeeeeeee "+cl.getCloudletId()+" "+ CloudSim.clock());
            cl.currStagenum = NetworkConstants.FINISH;

        } else {
            cl.currStagenum = currstage + 1;
            int i = 0;
            for (i = cl.currStagenum; i < cl.stages.size(); i++) {
                if (cl.stages.get(i).type == NetworkConstants.WAIT_SEND) {

                    // mai: process start time
                    cl.stages.get(i).processStartTime = CloudSim.clock();

                    NetworkPacket pkt = new NetworkPacket(cl.getUserId(),cl.stages.get(i).data,cl.getVmId(),cl.stages.get(i).speer,cl.getCloudletId(),cl.getAppId());

                    List<NetworkPacket> pktlist = pkttosend.get(cl.getCloudletId());
                    if (pktlist == null) {
                        pktlist = new ArrayList<NetworkPacket>();
                    }
                    pktlist.add(pkt);
                    pkttosend.put(cl.getCloudletId(), pktlist);

                } else {
                    break;
                }

            }
            if (i == cl.stages.size()) {
                cl.currStagenum = NetworkConstants.FINISH;
            } else {
                cl.currStagenum = i;

                if (cl.stages.get(i).type == NetworkConstants.WAIT_RECV){
                    Log.printLine("cccccccccc"+ "  "+ cl.getCloudletId() + "  "+ CloudSim.clock());
                    return processPktReceive(rcl,mipsShare);
                }

                if (cl.stages.get(i).type == NetworkConstants.EXECUTION) {

                    // mai: process start time
                    cl.stages.get(i).processStartTime = CloudSim.clock();

                    // Calculate the estimated execution completion time of this stage.
                    double time = cl.stages.get(i).stageCloudletLength  / (getCapacity(mipsShare) * rcl.getNumberOfPes());
                    Log.printLine("yyyyyyyyyyy"+ CloudSim.clock()+ " "+ getCapacity(mipsShare) +"  "+time);
                    return time;
                }

            }
        }
        return -1;

    }

    protected double processPktReceive(ResCloudlet rcl,List<Double> mipsShare){
        NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();
        int completeReceives = 0;
        // can write a loop
        int j;
        for (j = cl.currStagenum; j < cl.stages.size(); j++){
            if (cl.stages.get(j).type == NetworkConstants.WAIT_RECV){
                if (cl.stages.get(j).processStartTime == -1){
                    Log.printLine("ddddddddd"+"  "+ cl.getCloudletId() + "  "+ CloudSim.clock());
                    List<NetworkPacket> pktlist = pktrecv.get(cl.getCloudletId());
                    List<NetworkPacket> pkttoremove = new ArrayList<NetworkPacket>();
                    if (pktlist != null) {
                        Iterator<NetworkPacket> it = pktlist.iterator();
                        NetworkPacket pkt = null;
                        while (it.hasNext()) {
                            pkt = it.next();
                            // Asumption packet will not arrive in the same cycle
                            if (pkt.getDestinationCloudlet() == cl.getCloudletId()) {
                                // mai: process start time
                                cl.stages.get(j).processStartTime = CloudSim.clock();
                                Log.printLine("bbbbbbbbbbbbbb"+ "  "+ cl.getCloudletId() + "  "+ j +" " + cl.stages.get(j).processStartTime);
                                cl.stages.get(j).time = CloudSim.clock() - pkt.getSendTime();
                                pkttoremove.add(pkt);
                                completeReceives++;
                                break;
                            }
                        }
                        pktlist.removeAll(pkttoremove);
                        // if(pkt!=null)
                        // else wait for recieving the packet
                    }
                }else{
                    completeReceives++;
                }
            }else{
                if (completeReceives == (j - cl.currStagenum)){
                    cl.currStagenum = j - 1;
                    double time = changetonextstage(rcl,mipsShare);
                    return time;
                }
                break;
            }
        }
        if (completeReceives == (j - cl.currStagenum)){
            cl.currStagenum = j - 1;
            double time = changetonextstage(rcl,mipsShare);
            return time;
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

        if (pesInUse < limitNum){
            return capacity/limitNum;
        }

        if (pesInUse > cpus) {
            capacity /= pesInUse;
        } else {
            capacity /= cpus;
        }
        return capacity;
    }

    @Override
    public double cloudletSubmit(Cloudlet gl, double fileTransferTime) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public double cloudletSubmit(Cloudlet gl) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public Cloudlet cloudletCancel(int clId) {
        //@todo The method is not implemented, in fact
        return null;
    }

    @Override
    public boolean cloudletPause(int clId) {
        //@todo The method is not implemented, in fact
        return false;
    }

    @Override
    public double cloudletResume(int clId) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public void cloudletFinish(ResCloudlet rcl) {
        rcl.setCloudletStatus(Cloudlet.SUCCESS);
        rcl.finalizeCloudlet();
        getCloudletFinishedList().add(rcl);
    }

    @Override
    public int getCloudletStatus(int clId) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public boolean isFinishedCloudlets() {
        //@todo The method is not implemented, in fact
        return false;
    }

    @Override
    public Cloudlet getNextFinishedCloudlet() {
        //@todo The method is not implemented, in fact
        return null;
    }

    @Override
    public int runningCloudlets() {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public Cloudlet migrateCloudlet() {
        //@todo The method is not implemented, in fact
        return null;
    }

    @Override
    public double getTotalUtilizationOfCpu(double time) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public List<Double> getCurrentRequestedMips() {
        List<Double> mipsShare = new ArrayList<Double>();
        return mipsShare;
        //@todo The method is not implemented, in fact
    }

    @Override
    public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfRam() {
        //@todo The method is not implemented, in fact
        return 0;
    }

    @Override
    public double getCurrentRequestedUtilizationOfBw() {
        //@todo The method is not implemented, in fact
        return 0;
    }


}
