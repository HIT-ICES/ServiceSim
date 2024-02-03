package org.serviceProvider.services;

public class ServiceStage {
    /* stages type */
    public int type;

    /* stageid */
    public double stageid;

    /* peer serviceId */
    public int peer;

    /* stageCloudletLength */
    public double stageCloudletLength;

    /* data */
    public double data;

    public ServiceStage(int type, double stageid, int peer, double stageCloudletLength, double data) {
        super();
        this.type = type;
        this.stageid = stageid;
        this.peer = peer; // this represent the serviceId
        this.stageCloudletLength = stageCloudletLength;
        this.data = data; // data which need to transfer
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getStageid() {
        return stageid;
    }

    public void setStageid(double stageid) {
        this.stageid = stageid;
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
