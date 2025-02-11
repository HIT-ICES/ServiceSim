package org.utils;

import com.alibaba.fastjson.JSONObject;

public class Location {

    public double latitude;
    public double longitude;
    public int block;

    public Location(double latitude, double longitude, int block) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.block = block;
    }

    public Location(JSONObject config)
    {
        this(config.getDouble("latitude"),
            config.getDouble("longitude"),
            config.getInteger("block"));
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

}
