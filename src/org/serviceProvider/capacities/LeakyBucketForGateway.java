package org.serviceProvider.capacities;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.enduser.networkPacket.NetworkPacket;
import org.utils.PolicyConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class LeakyBucketForGateway implements LoadAdmission {

    int deviceId;

    /* for algorithm */
    double lastTime;

    Map<Pair<Integer,Integer>,Double> tokens; // Pair<Integer,Integer>: <serviceChainId, userLevel>

    Map<Pair<Integer,Integer>,Integer> capacities;

    Map<Pair<Integer,Integer>,Integer> admissionRate;

    /* for record */

    int lastMinute; // Record once a minute.

    String leakybucketHistoryFile; // for LeakybucketExecutor history

    Map<Pair<Integer,Integer>,Integer> admissionNum; //serviceChainId, userLevel -> num

    public LeakyBucketForGateway(int deviceId, Map<Pair<Integer,Integer>,Double> tokens, Map<Pair<Integer,Integer>,Integer> capacities, Map<Pair<Integer,Integer>,Integer> admissionRate, String leakybucketHistoryFile){
        this.deviceId = deviceId;

        this.tokens = tokens;
        this.capacities = capacities;
        this.admissionRate = admissionRate;
        this.leakybucketHistoryFile = leakybucketHistoryFile;

        lastMinute = 0;

        lastTime = 0.0;

        admissionNum = capacities;
        initAdmissionNum();
    }

    /* return true or false */
    @Override
    public boolean isAdmission(NetworkPacket networkPacket) {

        if (networkPacket.destinationServiceId != 0){ // load admission only for gateway
            return true;
        }else{// serviceId = 0 equals service = "gateway";
            int thisMinute= (int)(CloudSim.clock() / PolicyConstants.aMinute);
            if (thisMinute != lastMinute){
                //getLoadAdmissionExecutorHistories().add(loadAdmissionExecutorHistory);
                // Write directly to file.
                writeAdmissionExecutorResult(thisMinute);
                initAdmissionNum();
                lastMinute = thisMinute;
            }

            updateTokens();
            int serviceChainId = networkPacket.getServiceChainInfo().getServiceChainId();
            int userLevel = networkPacket.getEndUserInfo().getUserLevel();
            Pair <Integer,Integer> key = new Pair<>(serviceChainId, userLevel);

            if (tokens.containsKey(key)){
                double t = tokens.get(key);
                if(t >= 1){
                    tokens.put(key, t-1);
                    addAdmissionNum(key);
                    return true;
                }
            }else{
                addAdmissionNum(key);
                return true; // If the threshold is not found, it is received directly.
            }
        }
        return false;
    }

    public  void initAdmissionNum(){
        for (Pair<Integer,Integer> key : admissionNum.keySet()){
            admissionNum.put(key, 0);
        }
    }

    // write Result to the file
    public void writeAdmissionExecutorResult(int thisMinute){
        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(leakybucketHistoryFile,true);
            //fileWriter.append('\n');
            // time,serviceChainId,UserlevelId,requestNum,admissionNum
            for (Pair<Integer, Integer> key : admissionNum.keySet()){
                fileWriter.append(String.valueOf(thisMinute));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(deviceId));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(key.getKey())); // serviceChainId,
                fileWriter.append(',');
                fileWriter.append(String.valueOf(key.getValue())); // userLevel
                fileWriter.append(',');
                fileWriter.append(String.valueOf(admissionNum.get(key)));
                fileWriter.append('\n');
            }

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTokens(){

        for (Pair<Integer,Integer> key : tokens.keySet()){
            if (admissionRate.containsKey(key) && capacities.containsKey(key)){
                // Rate is the number of tokens placed per minute.
                double updatetoken = min(capacities.get(key), tokens.get(key) + (CloudSim.clock()-lastTime) / PolicyConstants.aMinute * admissionRate.get(key));
                tokens.put(key, updatetoken);
                //lastTime = CloudSim.clock();
            }else{
                Log.printLine("error- Cannot find keys for tokens or capabilities.");
            }
        }
        lastTime = CloudSim.clock();
    }

    public double min(double a, double b){
        if (a < b){
            return a;
        }
        else return b;
    }

    public void addAdmissionNum(Pair<Integer,Integer> key){
        int num;
        if (admissionNum.containsKey(key)){
            num = admissionNum.get(key) + 1;
            admissionNum.put(key, num);
        }
    }


/* getter and setter */
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public double getLastTime() {
        return lastTime;
    }

    public void setLastTime(double lastTime) {
        this.lastTime = lastTime;
    }

    public Map<Pair<Integer, Integer>, Double> getTokens() {
        return tokens;
    }

    public void setTokens(Map<Pair<Integer, Integer>, Double> tokens) {
        this.tokens = tokens;
    }

    public Map<Pair<Integer, Integer>, Integer> getCapacities() {
        return capacities;
    }

    public void setCapacities(Map<Pair<Integer, Integer>, Integer> capacities) {
        this.capacities = capacities;
    }

    public Map<Pair<Integer, Integer>, Integer> getAdmissionRate() {
        return admissionRate;
    }

    public void setAdmissionRate(Map<Pair<Integer, Integer>, Integer> admissionRate) {
        this.admissionRate = admissionRate;
    }

    public int getLastMinute() {
        return lastMinute;
    }

    public void setLastMinute(int lastMinute) {
        this.lastMinute = lastMinute;
    }

    public String getLeakybucketHistoryFile() {
        return leakybucketHistoryFile;
    }

    public void setLeakybucketHistoryFile(String leakybucketHistoryFile) {
        this.leakybucketHistoryFile = leakybucketHistoryFile;
    }

    public Map<Pair<Integer, Integer>, Integer> getAdmissionNum() {
        return admissionNum;
    }

    public void setAdmissionNum(Map<Pair<Integer, Integer>, Integer> admissionNum) {
        this.admissionNum = admissionNum;
    }
}
