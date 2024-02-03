package org.enduser.networkPacket;

import org.enduser.EndUserInformation;
import org.infrastructureProvider.entities.Vm;
import org.serviceProvider.services.Servicechain;

public class NetworkPacket {

    public int userId; // serviceProvider

    public int type; // request or response

    public double size; // packet size (in bytes)

    public double remainSize;

    public double getRemainSize() {
        return remainSize;
    }

    public void setRemainSize(double remainSize) {
        this.remainSize = remainSize;
    }

    public double data; // data size (in bytes)

    public int source; // source datacenter

    public int destination; // destination datacenter

    public int sourceVm; // source vm

    public int destinationVm; // destination vm

    public int sourceServiceId; // source service

    public int destinationServiceId; // destination service

    public int sourceCloudlet; // for response destination

    public int destinationCloudlet; // If the type is "Request", then it is not used. Else if the type is "Response", then it is used.

    /* high level matrics */
    /* "serviceChainId, endUserId, endUserLevel" for routing */
    public int appId;

    public Servicechain serviceChainInfo;
// public int serviceChainId;

    public  EndUserInformation endUserInfo;
//    public int endUserId;
//    public int endUserLevel;

    public double sendTime;
    public double recvTime;

    public NetworkPacket(int type, double size, double data,
                         int source, int destination, int sourceVm, int destinationVm, int sourceCloudlet, int destinationCloudlet,
                         int appId, Servicechain serviceChainInfo, EndUserInformation endUserInfo){
        this.type = type;
        this.size = size;
        this.data = data;
        this.source = source;
        this.destination = destination;
        this.sourceVm = sourceVm;
        this.destinationVm = destinationVm;
        this.sourceCloudlet = sourceCloudlet;
        this.destinationCloudlet = destinationCloudlet;
        this.appId = appId;
        this.serviceChainInfo = serviceChainInfo;
        this.endUserInfo = endUserInfo;
        //this.serviceChainId = serviceChainId;
        //this.endUserId = endUserId;
        //this.endUserLevel = endUserLevel;
    }

    public NetworkPacket(int userId,double data,int sourceVm,int destinationServiceId, int sourceCloudlet,int appId){
        this.userId = userId;
        this.data = data;
        this.sourceVm = sourceVm;
        this.destinationServiceId = destinationServiceId;
        this.sourceCloudlet = sourceCloudlet;
        this.appId = appId;
    }

    public NetworkPacket(int userId,int type,int appId,Servicechain serviceChainInfo,EndUserInformation endUserInfo){
        this.userId = userId;
        this.type = type;
        this.appId = appId;
        this.serviceChainInfo = serviceChainInfo;
        this.endUserInfo = endUserInfo;
    }

    public void addCompletedLength(double completed){
        remainSize -= completed;
        if (remainSize <= 0.0001) remainSize = 0;
    }

    public boolean isCompleted(){
        return remainSize == 0;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getSourceVm() {
        return sourceVm;
    }

    public void setSourceVm(int sourceVm) {
        this.sourceVm = sourceVm;
    }

    public int getDestinationVm() {
        return destinationVm;
    }

    public void setDestinationVm(int destinationVm) {
        this.destinationVm = destinationVm;
    }

    public int getDestinationServiceId() {
        return destinationServiceId;
    }

    public void setDestinationServiceId(int destinationServiceId) {
        this.destinationServiceId = destinationServiceId;
    }

    public int getSourceCloudlet() {
        return sourceCloudlet;
    }

    public void setSourceCloudlet(int sourceCloudlet) {
        this.sourceCloudlet = sourceCloudlet;
    }

    public int getDestinationCloudlet() {
        return destinationCloudlet;
    }

    public void setDestinationCloudlet(int destinationCloudlet) {
        this.destinationCloudlet = destinationCloudlet;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public Servicechain getServiceChainInfo() {
        return serviceChainInfo;
    }

    public void setServiceChainInfo(Servicechain serviceChainInfo) {
        this.serviceChainInfo = serviceChainInfo;
    }

    public EndUserInformation getEndUserInfo() {
        return endUserInfo;
    }

    public void setEndUserInfo(EndUserInformation endUserInfo) {
        this.endUserInfo = endUserInfo;
    }

    public int getSourceServiceId() {
        return sourceServiceId;
    }

    public void setSourceServiceId(int sourceServiceId) {
        this.sourceServiceId = sourceServiceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getSendTime() {
        return sendTime;
    }

    public void setSendTime(double sendTime) {
        this.sendTime = sendTime;
    }

    public double getRecvTime() {
        return recvTime;
    }

    public void setRecvTime(double recvTime) {
        this.recvTime = recvTime;
    }
}
