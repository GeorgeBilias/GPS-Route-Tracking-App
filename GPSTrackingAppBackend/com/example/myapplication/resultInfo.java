package com.example.myapplication;

import java.io.Serializable;

public class resultInfo implements Serializable {
    private double TotalDistance;
    private double AverageSpeed;
    private double Totalelevation;
    private double TotalTime;
    private String user;
    private int route;
    private boolean last_one;

    public resultInfo() {
        this.last_one = false;
    }
    public void set_route(int route) {
    	this.route = route;
    }
    public int get_route() {
    	return route;
    }

    public void set_last_one(boolean bl) {
        this.last_one = bl;
    }

    public boolean get_last_one() {
        return this.last_one;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getTotalDistance() {
        return this.TotalDistance;
    }

    public void setTotalDistance(double TotalDistance) {
        this.TotalDistance = TotalDistance;
    }

    public double getAverageSpeed() {
        return this.AverageSpeed;
    }

    public void setAverageSpeed(double AverageSpeed) {
        this.AverageSpeed = AverageSpeed;
    }

    public double getTotalelevation() {
        return this.Totalelevation;
    }

    public void setTotalelevation(double Totalelevation) {
        this.Totalelevation = Totalelevation;
    }

    public double getTotalTime() {
        return this.TotalTime;
    }

    public void setTotalTime(double TotalTime) {
        this.TotalTime = TotalTime;
    }

}
