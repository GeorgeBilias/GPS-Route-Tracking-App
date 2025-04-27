package com.example.myapplication;

import java.io.Serializable;

public class Information implements Serializable {

    private float lon;
    private float lat;
    private float ele;
    private String time;

    public Information(float lat, float lon, float ele, String time) {
        this.lat = lat;
        this.lon = lon;
        this.ele = ele;
        this.time = time;
        

    }

    public Information() {

    }

    public float getLat() {
        return this.lat;
    }

    public float getLon() {
        return this.lon;
    }

    public float getEle() {
        return this.ele;
    }

    public String getTime() {
        return this.time;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void setEle(float ele) {
        this.ele = ele;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String toString() {
        String infor =  "info: lat: " + this.getLat() + ", lon: "
                + this.getLon() + ", ele:"
                + this.getEle() + ", time: " + this.getTime();
        return infor;

    }

}