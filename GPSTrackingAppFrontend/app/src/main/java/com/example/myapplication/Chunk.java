package com.example.myapplication;

import java.io.Serializable;

public class Chunk implements Serializable {
    private Information info1;
    private Information info2;
    private Information info3;
    private int routeNumber;
    private String user;
    private boolean last_one;

    public void set_last_one(boolean bl) {
        this.last_one = bl;
    }

    public boolean get_last_one() {
        return this.last_one;
    }

    public void set_User(String u) {
        this.user = u;
    }

    public String get_User() {
        return this.user;
    }

    public Information getInfo1() {
        return this.info1;
    }

    public void setInfo1(Information info1) {
        this.info1 = info1;
    }

    public Information getInfo2() {
        return this.info2;
    }

    public void setInfo2(Information info2) {
        this.info2 = info2;
    }

    public Information getInfo3() {
        return this.info3;
    }

    public void setInfo3(Information info3) {
        this.info3 = info3;
    }

    public int getRouteNumber() {
        return this.routeNumber;
    }

    public void setRouteNumber(int routeNumber) {
        this.routeNumber = routeNumber;
    }

    public Chunk() {
        this.last_one = false;
    }

    public void set_info1(Information i1) {
        this.info1 = i1;
    }

    public void set_info2(Information i2) {
        this.info2 = i2;
    }

    public void set_info3(Information i3) {
        this.info3 = i3;
    }

    public Information get_info1() {
        return this.info1;
    }

    public Information get_info2() {
        return this.info2;
    }

    public Information get_info3() {
        return this.info3;
    }

    // public String get_user() {
    // return this.get_info1().getUser();
    // }
    // elenxos gia null times sta chunks
    public void nullCheck() {
        int papaNotNull = 0, papaNull = 0;
        if (this.get_info1() != null) {
            papaNotNull++;
        } else {
            papaNull++;
        }

        if (this.get_info2() != null) {
            papaNotNull++;
        } else {
            papaNull++;
        }

        if (this.get_info3() != null) {
            papaNotNull++;
        } else {
            papaNull++;
        }

        System.out.println(papaNotNull + " " + papaNull + " ");

    }

    public String toString() {
        String str = "";
        if (this.get_info1() != null && this.get_info2() != null && this.get_info3() != null) {
            str = "for route number: " + this.getRouteNumber() + " " + this.get_info1().toString()
                    + this.get_info2().toString()
                    + this.get_info3().toString();

        }

        else if (this.get_info1() != null && this.get_info2() != null && this.get_info3() == null) {
            str = "for route number: " + this.getRouteNumber() + " " + this.get_info1().toString()
                    + this.get_info2().toString();

        }

        else if (this.get_info1() != null && this.get_info2() == null) {
            str = "for route number: " + this.getRouteNumber() + " " + this.get_info1().toString();

        }

        return str;

    }
}
