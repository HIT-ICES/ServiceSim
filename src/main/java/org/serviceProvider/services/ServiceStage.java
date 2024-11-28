package org.serviceProvider.services;

@SuppressWarnings("unused")
public class ServiceStage {
    /* stages type */
    public int type;

    /* stageId */
    public double stageId;

    /* peer serviceId */
    public int peer;

    /* stageCloudletLength */
    public double stageCloudletLength;

    /* data */
    public double data;

    public ServiceStage(int type, double stageId, int peer, double stageCloudletLength, double data) {
        super();
        this.type = type;
        this.stageId = stageId;
        this.peer = peer; // this represents the serviceId
        this.stageCloudletLength = stageCloudletLength;
        this.data = data; // data which need to transfer
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getStageId() {
        return stageId;
    }

    public void setStageId(double stageId) {
        this.stageId = stageId;
    }

    public int getPeer() {
        return peer;
    }

    public void setPeer(int peer) {
        this.peer = peer;
    }

    public double getStageCloudletLength() {
        return stageCloudletLength;
    }

    public void setStageCloudletLength(double stageCloudletLength) {
        this.stageCloudletLength = stageCloudletLength;
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }
}
